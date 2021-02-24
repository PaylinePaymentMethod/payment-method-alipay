package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.bean.object.ForexService;
import com.payline.payment.alipay.bean.object.Trade;
import com.payline.payment.alipay.bean.response.APIResponse;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.utils.SignatureUtils;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.payment.alipay.utils.constant.RequestContextKeys;
import com.payline.payment.alipay.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.OnHoldCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentWithRedirectionServiceImplTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private SignatureUtils signatureUtils;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private APIResponse apiResponse;

    @Captor
    private ArgumentCaptor<RequestConfiguration> requestConfigurationArgumentCaptor1;

    @Captor
    private ArgumentCaptor<RequestConfiguration> requestConfigurationArgumentCaptor2;

    @Captor
    private ArgumentCaptor<Map<String, String>> parametersMapArgumentCaptor;

    @InjectMocks
    @Spy
    private PaymentWithRedirectionServiceImpl underTest;

    @Test
    void finalizeRedirectionPaymentOk() {
        final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        final Map<String, String[]> httpRequestParametersMap = new HashMap<>();
        final String transactionId = "the_out_trade_no";
        httpRequestParametersMap.put("out_trade_no", new String[]{transactionId});
        httpRequestParametersMap.put("key", new String[]{"value"});
        final RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withHttpRequestParametersMap(httpRequestParametersMap)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(MockUtils.anEnvironment())
                .withPartnerConfiguration(partnerConfiguration)
                .withAmount(MockUtils.aPaylineAmount())
                .withOrder(MockUtils.aPaylineOrder())
                .withBuyer(MockUtils.aBuyer())
                .withBrowser(MockUtils.aBrowser())
                .withTransactionId("transId")
                .build();
        final PaymentResponse paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(FailureCause.CANCEL)
                .build();

        doReturn(true).when(signatureUtils).getVerification(requestConfigurationArgumentCaptor1.capture(),
                parametersMapArgumentCaptor.capture());
        doReturn(paymentResponse).when(underTest).retrieveTransactionStatus(requestConfigurationArgumentCaptor2.capture(), eq(transactionId));

        final PaymentResponse response = underTest.finalizeRedirectionPayment(redirectionPaymentRequest);

        assertEquals(paymentResponse, response);

        final RequestConfiguration requestConfiguration1 = requestConfigurationArgumentCaptor1.getValue();
        assertEquals(contractConfiguration, requestConfiguration1.getContractConfiguration());
        assertEquals(partnerConfiguration, requestConfiguration1.getPartnerConfiguration());
        final RequestConfiguration requestConfiguration2 = requestConfigurationArgumentCaptor2.getValue();
        assertEquals(contractConfiguration, requestConfiguration2.getContractConfiguration());
        assertEquals(partnerConfiguration, requestConfiguration2.getPartnerConfiguration());

        final Map<String, String> parametersMap = parametersMapArgumentCaptor.getValue();
        assertEquals(2, parametersMap.size());
        assertEquals(transactionId, parametersMap.get("out_trade_no"));
        assertEquals("value", parametersMap.get("key"));
    }

    @Test
    void finalizeRedirectionPaymentPluginException() {
        final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        final Map<String, String[]> httpRequestParametersMap = new HashMap<>();
        final String transactionId = "the_out_trade_no";
        httpRequestParametersMap.put("out_trade_no", new String[]{transactionId});
        httpRequestParametersMap.put("key", new String[]{"value"});
        final RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withHttpRequestParametersMap(httpRequestParametersMap)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(MockUtils.anEnvironment())
                .withPartnerConfiguration(partnerConfiguration)
                .withAmount(MockUtils.aPaylineAmount())
                .withOrder(MockUtils.aPaylineOrder())
                .withBuyer(MockUtils.aBuyer())
                .withBrowser(MockUtils.aBrowser())
                .withTransactionId("transId")
                .build();
        final PluginException pluginException = new PluginException("a message to you", FailureCause.CANCEL);

        doThrow(pluginException).when(signatureUtils).getVerification(any(), any());

        final PaymentResponse paymentResponse = underTest.finalizeRedirectionPayment(redirectionPaymentRequest);

        assertTrue(paymentResponse instanceof PaymentResponseFailure);
        final PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals("a message to you", paymentResponseFailure.getErrorCode());
        assertEquals(FailureCause.CANCEL, paymentResponseFailure.getFailureCause());
    }

    @Test
    void finalizeRedirectionPaymentInternalError() {
        final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        final Map<String, String[]> httpRequestParametersMap = new HashMap<>();
        final String transactionId = "the_out_trade_no";
        httpRequestParametersMap.put("out_trade_no", new String[]{transactionId});
        httpRequestParametersMap.put("key", new String[]{"value"});
        final RedirectionPaymentRequest redirectionPaymentRequest = RedirectionPaymentRequest.builder()
                .withHttpRequestParametersMap(httpRequestParametersMap)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(MockUtils.anEnvironment())
                .withPartnerConfiguration(partnerConfiguration)
                .withAmount(MockUtils.aPaylineAmount())
                .withOrder(MockUtils.aPaylineOrder())
                .withBuyer(MockUtils.aBuyer())
                .withBrowser(MockUtils.aBrowser())
                .withTransactionId("transId")
                .build();
        final RuntimeException runtimeException = new RuntimeException("a message to you");

        doThrow(runtimeException).when(signatureUtils).getVerification(any(), any());

        final PaymentResponse paymentResponse = underTest.finalizeRedirectionPayment(redirectionPaymentRequest);

        assertTrue(paymentResponse instanceof PaymentResponseFailure);
        final PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals("plugin error: RuntimeException: a message to you", paymentResponseFailure.getErrorCode());
        assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
    }

    @Test
    void handleSessionExpired() {
        final PaymentResponse paymentResponse = underTest.handleSessionExpired(null);

        assertTrue(paymentResponse instanceof PaymentResponseFailure);
        final PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals("Session expired", paymentResponseFailure.getErrorCode());
        assertEquals(FailureCause.SESSION_EXPIRED, paymentResponseFailure.getFailureCause());
    }

    @Test
    void retrieveTransactionStatusTradeFinished() {
        final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        final RequestConfiguration requestConfiguration = new RequestConfiguration(contractConfiguration,
                MockUtils.anEnvironment(), partnerConfiguration);
        final String transactionId = "transactionId";
        final String buyerId = "buyerId";
        doReturn(apiResponse).when(httpClient).get(eq(requestConfiguration), parametersMapArgumentCaptor.capture());
        doReturn(true).when(apiResponse).isSuccess();
        when(apiResponse.getResponse().getTrade().getTradeStatus()).thenReturn(Trade.TradeStatus.TRADE_FINISHED);
        when(apiResponse.getResponse().getTrade().getBuyerEmail()).thenReturn("pes@test.com");
        when(apiResponse.getResponse().getTrade().getBuyerId()).thenReturn(buyerId);

        final PaymentResponse paymentResponse = underTest.retrieveTransactionStatus(requestConfiguration, transactionId);

        assertTrue(paymentResponse instanceof PaymentResponseSuccess);
        final PaymentResponseSuccess paymentResponseSuccess = (PaymentResponseSuccess) paymentResponse;
        assertEquals(transactionId, paymentResponseSuccess.getPartnerTransactionId());
        assertEquals("TRADE_FINISHED", paymentResponseSuccess.getStatusCode());
        assertEquals(buyerId, paymentResponseSuccess.getTransactionAdditionalData());
        assertEquals("pes@test.com", paymentResponseSuccess.getRequestContext().getRequestData().get(RequestContextKeys.BUYER_ID));

        final Map<String, String> parametersMap = parametersMapArgumentCaptor.getValue();
        assertEquals(4, parametersMap.size());
        assertEquals(transactionId, parametersMap.get("out_trade_no"));
        assertEquals(contractConfiguration.getProperty(ContractConfigurationKeys.MERCHANT_PID).getValue(), parametersMap.get("partner"));
        assertEquals("UTF-8", parametersMap.get("_input_charset"));
        assertEquals(ForexService.SINGLE_TRADE_QUERY.getService(), parametersMap.get("service"));
    }

    @Test
    void retrieveTransactionStatusWaitBuyerPay() {
        final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        final RequestConfiguration requestConfiguration = new RequestConfiguration(contractConfiguration,
                MockUtils.anEnvironment(), partnerConfiguration);
        final String transactionId = "transactionId";
        doReturn(apiResponse).when(httpClient).get(eq(requestConfiguration), any());
        doReturn(true).when(apiResponse).isSuccess();
        when(apiResponse.getResponse().getTrade().getTradeStatus()).thenReturn(Trade.TradeStatus.WAIT_BUYER_PAY);

        final PaymentResponse paymentResponse = underTest.retrieveTransactionStatus(requestConfiguration, transactionId);

        assertTrue(paymentResponse instanceof PaymentResponseOnHold);
        final PaymentResponseOnHold paymentResponseOnHold = (PaymentResponseOnHold) paymentResponse;
        assertEquals(transactionId, paymentResponseOnHold.getPartnerTransactionId());
        assertEquals(OnHoldCause.ASYNC_RETRY, paymentResponseOnHold.getOnHoldCause());
    }

    @Test
    void retrieveTransactionStatusTradeClosed() {
        final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        final RequestConfiguration requestConfiguration = new RequestConfiguration(contractConfiguration,
                MockUtils.anEnvironment(), partnerConfiguration);
        final String transactionId = "transactionId";
        doReturn(apiResponse).when(httpClient).get(eq(requestConfiguration), any());
        doReturn(true).when(apiResponse).isSuccess();
        when(apiResponse.getResponse().getTrade().getTradeStatus()).thenReturn(Trade.TradeStatus.TRADE_CLOSED);

        final PaymentResponse paymentResponse = underTest.retrieveTransactionStatus(requestConfiguration, transactionId);

        assertTrue(paymentResponse instanceof PaymentResponseFailure);
        final PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals("TRADE_CLOSED", paymentResponseFailure.getErrorCode());
        assertEquals(FailureCause.REFUSED, paymentResponseFailure.getFailureCause());
    }

    @Test
    void retrieveTransactionStatusResponseKo() {
        final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        final RequestConfiguration requestConfiguration = new RequestConfiguration(contractConfiguration,
                MockUtils.anEnvironment(), partnerConfiguration);
        final String transactionId = "transactionId";
        final String error = "error";
        doReturn(apiResponse).when(httpClient).get(eq(requestConfiguration), any());
        doReturn(false).when(apiResponse).isSuccess();
        when(apiResponse.getError()).thenReturn(error);

        final PaymentResponse paymentResponse = underTest.retrieveTransactionStatus(requestConfiguration, transactionId);

        assertTrue(paymentResponse instanceof PaymentResponseFailure);
        final PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals(error, paymentResponseFailure.getErrorCode());
        assertEquals(FailureCause.INVALID_DATA, paymentResponseFailure.getFailureCause());
    }
}