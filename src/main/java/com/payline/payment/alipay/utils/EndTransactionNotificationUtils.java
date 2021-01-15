package com.payline.payment.alipay.utils;

import com.google.gson.Gson;
import com.payline.payment.alipay.bean.request.EndTransactionNotificationRequest;
import com.payline.payment.alipay.bean.response.NotificationMessage;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.payment.alipay.utils.constant.PartnerConfigurationKeys;
import com.payline.payment.alipay.utils.constant.RequestContextKeys;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.bean.refund.request.RefundRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EndTransactionNotificationUtils {
    private final DateFormat timeFormatter = new SimpleDateFormat("hhmmss");
    private final DateFormat dateFormatter = new SimpleDateFormat("MMDD");
    private final DateFormat yearFormatter = new SimpleDateFormat("yy");
    private final DateFormat fullFormatter = new SimpleDateFormat("yyyyMMDDhhmmss");

    private static final String TRS_PMT_CTX_CRDHLDR_PRES = "Y";
    private static final String ERT = "24";
    private static final String TAG_TRANSACTION_TYPE = "20";

    private static class Holder {
        private static final EndTransactionNotificationUtils instance = new EndTransactionNotificationUtils();
    }

    public static EndTransactionNotificationUtils getInstance() {
        return EndTransactionNotificationUtils.Holder.instance;
    }

    /**
     * Convert a EndTransactionNotificationRequest object to a JSON
     *
     * @param request
     * @return {@link String}
     */
    public static String toJson(EndTransactionNotificationRequest request) {
        Gson gson = new Gson();
        return gson.toJson(request);
    }

    /**
     * Create a EndTransactionNotificationRequest object from NotificationService
     *
     * @param notificationMessage
     * @param request*            @return {@link EndTransactionNotificationRequest}
     */
    public EndTransactionNotificationRequest createFromNotificationService(NotificationMessage notificationMessage, NotificationRequest request, PaymentResponse paymentResponse) {
        boolean success = PaymentResponseSuccess.class.equals(paymentResponse.getClass());

        String message = success ? "0236" : "0256";
        String tagAmountAuthorized = notificationMessage.getTotalFee().replace(".", "");
        String partnerCurrency = notificationMessage.getCurrency();
        String tagTransactionTime = timeFormatter.format(notificationMessage.getNotifyTime());
        String tagTransactionDate = dateFormatter.format(notificationMessage.getNotifyTime());
        String tagTransactionYear = yearFormatter.format(notificationMessage.getNotifyTime());
        String schemePaymentTime = fullFormatter.format(notificationMessage.getNotifyTime());
        String tagAcqIdentifier = createTagAcqIdentifier(request.getContractConfiguration());
        String scheme = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SCHEME);
        String schemeTransId = notificationMessage.getOutTradeNo();
        String partnerTransName = request.getContractConfiguration().getPaymentMethodIdentifier();
        String buyerId = success ? ((PaymentResponseSuccess) paymentResponse).getRequestContext().getRequestData().get(RequestContextKeys.BUYER_ID) : "unknown";
        String partnerId = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.PARTNER_ID);
        String merchantId = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.MERCHANT_ID);
        String mccCode = request.getContractConfiguration().getProperty(ContractConfigurationKeys.SECONDARY_MERCHANT_INDUSTRY).getValue();
        String storeId = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.STORE_ID);
        String messageSenderId = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.MESSAGE_SENDER_ID);
        String partnerTransId = createPartnerTransId(storeId);

        return EndTransactionNotificationRequest.Builder
                .anEndTransactionNotificationRequest()
                .withMessage(message)
                .withTagTransactionType(TAG_TRANSACTION_TYPE)
                .withTagAmountAuthorized(tagAmountAuthorized)
                .withPartnerCurrency(partnerCurrency)
                .withTagTransactionTime(tagTransactionTime)
                .withTagTransactionDate(tagTransactionDate)
                .withTagTransactionYear(tagTransactionYear)
                .withSchemePaymentTime(schemePaymentTime)
                .withTrsPmtCtxCrdhldrPres(TRS_PMT_CTX_CRDHLDR_PRES)
                .withTagAcqIdentifier(tagAcqIdentifier)
                .withErt(ERT)
                .withScheme(scheme)
                .withSchemeTransId(schemeTransId)
                .withPartnerTransId(partnerTransId)
                .withPartnerTransName(partnerTransName)
                .withBuyerId(buyerId)
                .withPartnerId(partnerId)
                .withMerchantId(merchantId)
                .withMccCode(mccCode)
                .withStoreId(storeId)
                .withMessageSenderId(messageSenderId)
                .build();
    }


    public EndTransactionNotificationRequest createFromRefundService(RefundRequest request) {
        String message = "0456";
        String tagAmountAuthorized = request.getAmount().getAmountInSmallestUnit().toString();
        String partnerCurrency = request.getAmount().getCurrency().getCurrencyCode();
        String tagTransactionTime = timeFormatter.format(request.getOrder().getDate());
        String tagTransactionDate = dateFormatter.format(request.getOrder().getDate());
        String tagTransactionYear = yearFormatter.format(request.getOrder().getDate());
        String schemePaymentTime = fullFormatter.format(request.getOrder().getDate());
        String tagAcqIdentifier = createTagAcqIdentifier(request.getContractConfiguration());
        String scheme = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.SCHEME);
        String schemeTransId = request.getTransactionId();
        String partnerTransName = request.getContractConfiguration().getPaymentMethodIdentifier();
        String buyerId = request.getRequestContext().getRequestData().get(RequestContextKeys.BUYER_ID);
        String partnerId = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.PARTNER_ID);
        String merchantId = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.MERCHANT_ID);
        String mccCode = request.getContractConfiguration().getProperty(ContractConfigurationKeys.SECONDARY_MERCHANT_INDUSTRY).getValue();
        String storeId = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.STORE_ID);
        String messageSenderId = request.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.MESSAGE_SENDER_ID);
        String partnerTransId = createPartnerTransId(storeId);

        return EndTransactionNotificationRequest.Builder
                .anEndTransactionNotificationRequest()
                .withMessage(message)
                .withTagTransactionType(TAG_TRANSACTION_TYPE)
                .withTagAmountAuthorized(tagAmountAuthorized)
                .withPartnerCurrency(partnerCurrency)
                .withTagTransactionTime(tagTransactionTime)
                .withTagTransactionDate(tagTransactionDate)
                .withTagTransactionYear(tagTransactionYear)
                .withSchemePaymentTime(schemePaymentTime)
                .withTrsPmtCtxCrdhldrPres(TRS_PMT_CTX_CRDHLDR_PRES)
                .withTagAcqIdentifier(tagAcqIdentifier)
                .withErt(ERT)
                .withScheme(scheme)
                .withSchemeTransId(schemeTransId)
                .withPartnerTransId(partnerTransId)
                .withPartnerTransName(partnerTransName)
                .withBuyerId(buyerId)
                .withPartnerId(partnerId)
                .withMerchantId(merchantId)
                .withMccCode(mccCode)
                .withStoreId(storeId)
                .withMessageSenderId(messageSenderId)
                .build();
    }


    private String createTagAcqIdentifier(ContractConfiguration configuration) {
        return configuration.getProperty(ContractConfigurationKeys.MERCHANT_BANK).getValue()
                + configuration.getProperty(ContractConfigurationKeys.MERCHANT_BANK_CODE).getValue();
    }


    private String createPartnerTransId(String storeId) {
        return storeId + fullFormatter.format(new Date());

    }


}
