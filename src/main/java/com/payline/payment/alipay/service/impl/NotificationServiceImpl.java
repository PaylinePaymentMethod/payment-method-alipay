package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.bean.object.Trade;
import com.payline.payment.alipay.bean.request.EndTransactionNotificationRequest;
import com.payline.payment.alipay.bean.request.NotifyVerify;
import com.payline.payment.alipay.bean.request.SingleTradeQuery;
import com.payline.payment.alipay.bean.response.APIResponse;
import com.payline.payment.alipay.bean.response.NotificationMessage;
import com.payline.payment.alipay.bean.response.NotifyResponse;
import com.payline.payment.alipay.exception.InvalidDataException;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.utils.EndTransactionNotificationUtils;
import com.payline.payment.alipay.utils.PluginUtils;
import com.payline.payment.alipay.utils.SignatureUtils;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.payment.alipay.utils.constant.RequestContextKeys;
import com.payline.payment.alipay.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.OnHoldCause;
import com.payline.pmapi.bean.common.TransactionCorrelationId;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.NotificationService;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.alipay.bean.object.ForexService.NOTIFY_VERIFY;
import static com.payline.payment.alipay.bean.object.ForexService.SINGLE_TRADE_QUERY;

public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LogManager.getLogger(NotificationServiceImpl.class);

    private HttpClient client = HttpClient.getInstance();
    private SignatureUtils signatureUtils = SignatureUtils.getInstance();

    @Override
    public NotificationResponse parse(NotificationRequest request) {
        RequestConfiguration configuration = null;
        PaymentResponse paymentResponse;
        NotificationMessage notificationMessage = null;
        String transactionId = "UNKNOWN";

        try {
            configuration = new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(), request.getPartnerConfiguration());
            String content = PluginUtils.inputStreamToString(request.getContent());

            Map<String, String> val = PluginUtils.createMapFromString(content);

            // verify signature
            if(!signatureUtils.getVerification(configuration, val)){
                throw new InvalidDataException("Invalid Alipay signature");
            }

            notificationMessage = NotificationMessage.fromMap(val);
            transactionId = notificationMessage.getOutTradeNo();
            String notificationId = notificationMessage.getNotify_id();

            if (PluginUtils.isEmpty(transactionId)) {
                // transactionId is set to unknown to create a valid TransactionCorrelationId at the end
                transactionId = "UNKNOWN";
                LOGGER.error("Bad content: {}", content);
                throw new PluginException("No transactionId in notification content");
            }

            if (PluginUtils.isEmpty(notificationId)) {
                LOGGER.error("Bad content: {}", content);
                throw new PluginException("No notificationId in notification content");
            }

            // create notify_verify request object
            NotifyVerify notifyVerify = NotifyVerify.NotifyVerifyBuilder
                    .aNotifyVerify()
                    .withNotifyId(notificationId)
                    .withPartner(request.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_PID).getValue())
                    .withService(NOTIFY_VERIFY)
                    .build();

            // call verify API
            NotifyResponse verificationResponse = client.notificationIsVerified(configuration, notifyVerify.getParametersList());
            switch (verificationResponse) {
                case TRUE:
                    //Notification is verified, so we can check transaction status
                    paymentResponse = getStatus(configuration, transactionId);

                    break;
                case FALSE:
                    //Notification can't be verified
                    //Return a paymentResponseFailure
                    paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                            .withFailureCause(FailureCause.REFUSED)
                            .build();
                    break;
                case INVALID:
                default:
                    //notify_verify call is invalid
                    //Return a paymentResponseFailure
                    paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                            .withFailureCause(FailureCause.INVALID_DATA)
                            .build();
                    break;
            }
        } catch (PluginException e) {
            LOGGER.error(e.getErrorCode(), e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withErrorCode(e.getErrorCode())
                    .withFailureCause(e.getFailureCause())
                    .build();
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }

        // notify Monext
        if (notificationMessage != null) {
            EndTransactionNotificationRequest endTransactionNotificationRequest = EndTransactionNotificationUtils.getInstance()
                    .createFromNotificationService(notificationMessage, request, paymentResponse);
            client.sendNotificationMonext(configuration, endTransactionNotificationRequest);
        }


        // return the NotificationResponse
        TransactionCorrelationId correlationId = TransactionCorrelationId.TransactionCorrelationIdBuilder
                .aCorrelationIdBuilder()
                .withType(TransactionCorrelationId.CorrelationIdType.PARTNER_TRANSACTION_ID)
                .withValue(transactionId)
                .build();

        return PaymentResponseByNotificationResponse.PaymentResponseByNotificationResponseBuilder
                .aPaymentResponseByNotificationResponseBuilder()
                .withPaymentResponse(paymentResponse)
                .withTransactionCorrelationId(correlationId)
                .withHttpStatus(200)
                .withHttpBody("SUCCESS")
                .build();
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        // does nothing
    }

    /**
     * create a single_trade_query request object, call API to get the transactionStatus and create a PaymentResponse from the received status
     *
     * @param configuration
     * @param transactionId
     * @return
     */
    private PaymentResponse getStatus(RequestConfiguration configuration, String transactionId) {
        PaymentResponse paymentResponse;

        // create single_trade_query request object
        SingleTradeQuery singleTradeQuery = SingleTradeQuery.SingleTradeQueryBuilder
                .aSingleTradeQuery()
                .withOutTradeNo(transactionId)
                .withPartner(configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_PID).getValue())
                .withService(SINGLE_TRADE_QUERY)
                .build();

        // call get API
        APIResponse response = client.get(configuration, singleTradeQuery.getParametersList());
        if (response.isSuccess()) {
            Trade transaction = response.getResponse().getTrade();
            Trade.TradeStatus status = transaction.getTradeStatus();

            switch (status) {
                case TRADE_FINISHED:
                    Email paymentId = PluginUtils.buildEmail(transaction.getBuyerEmail());

                    Map<String, String> data = new HashMap<>();
                    data.put(RequestContextKeys.BUYER_ID, paymentId.getEmail());
                    RequestContext context = RequestContext.RequestContextBuilder
                            .aRequestContext()
                            .withRequestData(data)
                            .build();

                    paymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                            .withPartnerTransactionId(transaction.getTradeNo())
                            .withStatusCode(status.name())
                            .withTransactionAdditionalData(transaction.getBuyerId())
                            .withTransactionDetails(paymentId)
                            .withRequestContext(context)
                            .build();
                    break;
                case WAIT_BUYER_PAY:
                    paymentResponse = PaymentResponseOnHold.PaymentResponseOnHoldBuilder.aPaymentResponseOnHold()
                            .withPartnerTransactionId(transaction.getTradeNo())
                            .withOnHoldCause(OnHoldCause.ASYNC_RETRY)
                            .build();
                    break;
                case TRADE_CLOSED:
                default:
                    paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                            .withErrorCode(status.name())
                            .withFailureCause(FailureCause.REFUSED)
                            .build();
                    break;
            }
        } else {
            //Response success is "F"
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withErrorCode(response.getError())
                    .withFailureCause(FailureCause.INVALID_DATA)
                    .build();
        }
        return paymentResponse;
    }
}
