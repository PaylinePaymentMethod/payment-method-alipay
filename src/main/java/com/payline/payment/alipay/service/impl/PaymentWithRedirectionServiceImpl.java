package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.bean.object.ForexService;
import com.payline.payment.alipay.bean.object.Trade;
import com.payline.payment.alipay.bean.request.SingleTradeQuery;
import com.payline.payment.alipay.bean.response.APIResponse;
import com.payline.payment.alipay.exception.InvalidDataException;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.service.AcquirerService;
import com.payline.payment.alipay.utils.PluginUtils;
import com.payline.payment.alipay.utils.SignatureUtils;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.payment.alipay.utils.constant.RequestContextKeys;
import com.payline.payment.alipay.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.OnHoldCause;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentWithRedirectionServiceImpl.class);

    // Variables non finales pour l'injection par mockito
    private HttpClient client = HttpClient.getInstance();
    private SignatureUtils signatureUtils = SignatureUtils.getInstance();
    private AcquirerService acquirerService = AcquirerService.getInstance();

    @Override
    public PaymentResponse finalizeRedirectionPayment(final RedirectionPaymentRequest request) {
        PaymentResponse paymentResponse;

        try {
            final RequestConfiguration configuration = new RequestConfiguration(request.getContractConfiguration(), request.getEnvironment(),
                    request.getPartnerConfiguration(), request.getPluginConfiguration());

            final Map<String, String> alipayReturnedParameters = new HashMap<>();
            if (request.getHttpRequestParametersMap() != null) {
                request.getHttpRequestParametersMap().entrySet().stream()
                        .filter(e -> e.getValue() != null && e.getValue().length == 1)
                        .forEach(e -> alipayReturnedParameters.put(e.getKey(), e.getValue()[0]));
            }

            // verify signature
            if (!signatureUtils.getVerification(configuration, alipayReturnedParameters)) {
                throw new InvalidDataException("Invalid Alipay signature");
            }

            final String transactionId = alipayReturnedParameters.get("out_trade_no");
            paymentResponse = retrieveTransactionStatus(configuration, transactionId);
        } catch (final PluginException e) {
            LOGGER.error(e.getErrorCode(), e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withErrorCode(e.getErrorCode())
                    .withFailureCause(e.getFailureCause())
                    .build();
        } catch (final RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }
        return paymentResponse;
    }

    @Override
    public PaymentResponse handleSessionExpired(final TransactionStatusRequest requestData) {
        return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(FailureCause.SESSION_EXPIRED)
                .withErrorCode("Session expired")
                .build();
    }

    /**
     * create a single_trade_query request object, call API to get the transactionStatus and create a PaymentResponse from the received status
     */
    protected PaymentResponse retrieveTransactionStatus(final RequestConfiguration configuration, final String transactionId) {
        final PaymentResponse paymentResponse;

        // create single_trade_query request object
        final String merchantPID = acquirerService.retrieveAcquirer(configuration.getPluginConfiguration(),
                configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.ACQUIRER_ID).getValue()).getMerchantPID();

        final SingleTradeQuery singleTradeQuery = SingleTradeQuery.SingleTradeQueryBuilder.aSingleTradeQuery()
                .withOutTradeNo(transactionId)
                .withPartner(merchantPID)
                .withService(ForexService.SINGLE_TRADE_QUERY)
                .build();

        // call get API
        final APIResponse response = client.get(configuration, singleTradeQuery.getParametersList());
        if (response.isSuccess()) {
            final Trade transaction = response.getResponse().getTrade();
            final Trade.TradeStatus status = transaction.getTradeStatus();

            switch (status) {
                case TRADE_FINISHED:
                    final Email paymentId = PluginUtils.buildEmail(transaction.getBuyerEmail());

                    final Map<String, String> data = new HashMap<>();
                    data.put(RequestContextKeys.BUYER_ID, paymentId.getEmail());
                    final RequestContext context = RequestContext.RequestContextBuilder.aRequestContext()
                            .withRequestData(data)
                            .build();

                    paymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                            .withPartnerTransactionId(transactionId)
                            .withStatusCode(status.name())
                            .withTransactionAdditionalData(transaction.getBuyerId())
                            .withTransactionDetails(paymentId)
                            .withRequestContext(context)
                            .build();
                    break;
                case WAIT_BUYER_PAY:
                    paymentResponse = PaymentResponseOnHold.PaymentResponseOnHoldBuilder.aPaymentResponseOnHold()
                            .withPartnerTransactionId(transactionId)
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
