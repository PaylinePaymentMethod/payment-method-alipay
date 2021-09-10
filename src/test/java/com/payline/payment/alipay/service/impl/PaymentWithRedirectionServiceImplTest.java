package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.bean.object.ForexService;
import com.payline.payment.alipay.bean.object.Trade;
import com.payline.payment.alipay.bean.response.APIResponse;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.service.AcquirerService;
import com.payline.payment.alipay.utils.SignatureUtils;
import com.payline.payment.alipay.utils.constant.RequestContextKeys;
import com.payline.payment.alipay.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.OnHoldCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseActiveWaiting;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private AcquirerService acquirerService;

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
                .withPluginConfiguration(MockUtils.PLUGIN_CONFIGURATION)
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
                .withPluginConfiguration(MockUtils.PLUGIN_CONFIGURATION)
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
                .withPluginConfiguration(MockUtils.PLUGIN_CONFIGURATION)
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

    @Nested
    class HandleSessionExpired {

        public static final String TRANSACTION_ID = "transactionId";
        private final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        private final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        private final TransactionStatusRequest transactionStatusRequest = TransactionStatusRequest.TransactionStatusRequestBuilder
                .aNotificationRequest()
                .withTransactionId(TRANSACTION_ID)
                .withAmount(MockUtils.aPaylineAmount())
                .withOrder(MockUtils.aPaylineOrder())
                .withBuyer(MockUtils.aBuyer())
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(MockUtils.anEnvironment())
                .withPartnerConfiguration(partnerConfiguration)
                .withPluginConfiguration(MockUtils.PLUGIN_CONFIGURATION)
                .build();

        @Test
        void nominal() {
            final PaymentResponseActiveWaiting response = PaymentResponseActiveWaiting.builder().build();
            doReturn(response).when(underTest).retrieveTransactionStatus(requestConfigurationArgumentCaptor1.capture(),
                    eq(TRANSACTION_ID));

            final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusRequest);

            assertEquals(response, paymentResponse);
            final RequestConfiguration requestConfiguration = requestConfigurationArgumentCaptor1.getValue();
            assertNotNull(requestConfiguration);
            assertEquals(contractConfiguration, requestConfiguration.getContractConfiguration());
            assertEquals(partnerConfiguration, requestConfiguration.getPartnerConfiguration());
            assertEquals(MockUtils.PLUGIN_CONFIGURATION, requestConfiguration.getPluginConfiguration());
        }

        @Test
        void pluginExceptionTradeNotExist() {
            // any car testé dans le cas nominal
            doThrow(new PluginException(PaymentWithRedirectionServiceImpl.TRADE_NOT_EXIST))
                    .when(underTest).retrieveTransactionStatus(any(RequestConfiguration.class), eq(TRANSACTION_ID));

            final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusRequest);

            assertTrue(paymentResponse instanceof PaymentResponseFailure);
            final PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
            assertEquals(FailureCause.SESSION_EXPIRED, paymentResponseFailure.getFailureCause());
            assertEquals(TRANSACTION_ID, paymentResponseFailure.getPartnerTransactionId());
            assertEquals(PaymentWithRedirectionServiceImpl.TRADE_NOT_EXIST, paymentResponseFailure.getErrorCode());
        }

        @Test
        void pluginException() {
            // any car testé dans le cas nominal
            doThrow(new PluginException("error", FailureCause.COMMUNICATION_ERROR))
                    .when(underTest).retrieveTransactionStatus(any(RequestConfiguration.class), eq(TRANSACTION_ID));

            final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusRequest);

            assertTrue(paymentResponse instanceof PaymentResponseFailure);
            final PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
            assertEquals(FailureCause.COMMUNICATION_ERROR, paymentResponseFailure.getFailureCause());
            assertEquals(TRANSACTION_ID, paymentResponseFailure.getPartnerTransactionId());
            assertEquals("error", paymentResponseFailure.getErrorCode());
        }

        @Test
        void runtimeException() {
            // any car testé dans le cas nominal
            doThrow(new RuntimeException("error"))
                    .when(underTest).retrieveTransactionStatus(any(RequestConfiguration.class), eq(TRANSACTION_ID));

            final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusRequest);

            assertTrue(paymentResponse instanceof PaymentResponseFailure);
            final PaymentResponseFailure paymentResponseFailure = (PaymentResponseFailure) paymentResponse;
            assertEquals(FailureCause.INTERNAL_ERROR, paymentResponseFailure.getFailureCause());
            assertEquals(TRANSACTION_ID, paymentResponseFailure.getPartnerTransactionId());
            assertEquals("plugin error: RuntimeException: error", paymentResponseFailure.getErrorCode());
        }
    }

    @Test
    void retrieveTransactionStatusTradeFinished() {
        final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        final RequestConfiguration requestConfiguration = new RequestConfiguration(contractConfiguration,
                MockUtils.anEnvironment(), partnerConfiguration, MockUtils.PLUGIN_CONFIGURATION);
        final String transactionId = "transactionId";
        final String buyerId = "buyerId";
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");
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
        assertEquals("merchantPID", parametersMap.get("partner"));
        assertEquals("UTF-8", parametersMap.get("_input_charset"));
        assertEquals(ForexService.SINGLE_TRADE_QUERY.getService(), parametersMap.get("service"));
    }

    @Test
    void retrieveTransactionStatusWaitBuyerPay() {
        final ContractConfiguration contractConfiguration = MockUtils.aContractConfiguration();
        final PartnerConfiguration partnerConfiguration = MockUtils.aPartnerConfiguration();
        final RequestConfiguration requestConfiguration = new RequestConfiguration(contractConfiguration,
                MockUtils.anEnvironment(), partnerConfiguration, MockUtils.PLUGIN_CONFIGURATION);
        final String transactionId = "transactionId";
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");
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
                MockUtils.anEnvironment(), partnerConfiguration, MockUtils.PLUGIN_CONFIGURATION);
        final String transactionId = "transactionId";
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");
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
                MockUtils.anEnvironment(), partnerConfiguration, MockUtils.PLUGIN_CONFIGURATION);
        final String transactionId = "transactionId";
        final String error = "error";
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");
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