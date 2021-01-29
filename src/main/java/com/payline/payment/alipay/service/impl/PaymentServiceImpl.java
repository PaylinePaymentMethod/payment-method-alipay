package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.bean.object.ForexService;
import com.payline.payment.alipay.bean.request.CreateForexTrade;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.utils.PluginUtils;
import com.payline.payment.alipay.utils.SignatureUtils;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.payment.alipay.utils.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentService;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.StringJoiner;

import static com.payline.payment.alipay.bean.object.ForexService.CREATE_FOREX_TRADE;
import static com.payline.payment.alipay.bean.object.ForexService.CREATE_FOREX_TRADE_WAP;

public class PaymentServiceImpl implements PaymentService {
    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);
    private SignatureUtils signatureUtils = SignatureUtils.getInstance();

    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {
        RequestConfiguration configuration = RequestConfiguration.build(paymentRequest);
        PaymentResponse paymentResponse;
        try {
            ForexService service;
            String productCode;

            if (PluginUtils.mobileUser(paymentRequest.getBrowser().getUserAgent())) {
                service = CREATE_FOREX_TRADE_WAP;
                productCode = "NEW_WAP_OVERSEAS_SELLER";
            } else {
                service = CREATE_FOREX_TRADE;
                productCode = "NEW_OVERSEAS_SELLER";
            }
            // create createForexTrade request object
            CreateForexTrade createForexTrade = CreateForexTrade.CreateForexTradeBuilder
                    .aCreateForexTrade()
                    .withCurrency(paymentRequest.getOrder().getAmount().getCurrency().getCurrencyCode())
                    .withNotifyUrl(paymentRequest.getEnvironment().getNotificationURL())
                    .withOutTradeNo(paymentRequest.getTransactionId())
                    .withPartner(paymentRequest.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_PID).getValue())
                    .withProductCode(productCode)
                    .withReferUrl(paymentRequest.getContractConfiguration().getProperty(ContractConfigurationKeys.MERCHANT_URL).getValue())
                    .withReturnUrl(paymentRequest.getEnvironment().getRedirectionReturnURL())
                    .withNotifyUrl(paymentRequest.getEnvironment().getNotificationURL())
                    .withService(service)
                    .withSubject(paymentRequest.getSoftDescriptor())
                    .withTotalFee(PluginUtils.createStringAmount(paymentRequest.getAmount()))
                    .build();

            // create the url to get
            Map<String, String> parameters = createForexTrade.getParametersList();
            Map<String, String> signedParameters = signatureUtils.getSignedParameters(configuration, parameters);
            Map<String, String> encodedSignedParameters = PluginUtils.encode(signedParameters);
            final StringJoiner urlParameters = new StringJoiner("&");
            for (final Map.Entry<String, String> entry : encodedSignedParameters.entrySet()) {
                urlParameters.add(entry.getKey() + "=" + entry.getValue());
            }
            final String baseUrl = paymentRequest.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.ALIPAY_URL);
            if (baseUrl == null) {
                throw new PluginException(PartnerConfigurationKeys.ALIPAY_URL + " is missing in the PartnerConfiguration");
            }
            final String url = baseUrl + "?" + urlParameters.toString();
            // return a PaymentResponseRedirect
            PaymentResponseRedirect.RedirectionRequest redirectionRequest = PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder.aRedirectionRequest()
                    .withRequestType(PaymentResponseRedirect.RedirectionRequest.RequestType.POST)
                    .withUrl(new URL(url))
                    .build();

            paymentResponse = PaymentResponseRedirect.PaymentResponseRedirectBuilder.aPaymentResponseRedirect()
                    .withPartnerTransactionId(paymentRequest.getTransactionId())
                    .withRedirectionRequest(redirectionRequest)
                    .build();

        } catch (MalformedURLException e) {
            String errorMessage = "PartnerConfig ALIPAY_URL is malformed";
            LOGGER.error(errorMessage, e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(errorMessage)
                    .withFailureCause(FailureCause.INVALID_DATA)
                    .build();

        } catch (PluginException e) {
            LOGGER.error(e.getErrorCode(), e);
            paymentResponse = e.toPaymentResponseFailureBuilder().build();

        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();

        }
        return paymentResponse;
    }
}
