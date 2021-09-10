package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.service.AcquirerService;
import com.payline.payment.alipay.service.PartnerTransactionIdService;
import com.payline.payment.alipay.utils.SignatureUtils;
import com.payline.payment.alipay.utils.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.Browser;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

class PaymentServiceImplTest {
    @InjectMocks
    private PaymentServiceImpl underTest = new PaymentServiceImpl();

    @Mock
    private SignatureUtils signatureUtils = SignatureUtils.getInstance();

    @Mock
    private PartnerTransactionIdService partnerTransactionIdService;

    @Mock
    private AcquirerService acquirerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void paymentRequestPCOK() {
        Map<String,String> params = new HashMap<>();
        params.put("foo", "bar");
        params.put("foo2", "baz");
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();

        Mockito.doReturn(params).when(signatureUtils).getSignedParameters(any(), any());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");
        doReturn("partnerTransactionId").when(partnerTransactionIdService).retrievePartnerTransactionId(paymentRequest);

        PaymentResponse response = underTest.paymentRequest(paymentRequest);

        assertEquals(PaymentResponseRedirect.class, response.getClass());
        PaymentResponseRedirect responseRedirect = (PaymentResponseRedirect) response;
        final URL url = responseRedirect.getRedirectionRequest().getUrl();
        assertNotNull(url);
        assertEquals("https://mapi.alipaydev.com/gateway.do?foo=bar&foo2=baz", url.toString());
        assertEquals("partnerTransactionId", responseRedirect.getPartnerTransactionId());
    }

    @Test
    void paymentRequestMobileOK() {
        Map<String,String> params = new HashMap<>();
        params.put("foo", "bar");
        params.put("foo2", "baz");
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequestBuilder().withBrowser(Browser.BrowserBuilder.aBrowser().withUserAgent("Mobile Safari").build()).build();

        Mockito.doReturn(params).when(signatureUtils).getSignedParameters(any(), any());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");
        doReturn("partnerTransactionId").when(partnerTransactionIdService).retrievePartnerTransactionId(paymentRequest);

        PaymentResponse response = underTest.paymentRequest(paymentRequest);

        assertEquals(PaymentResponseRedirect.class, response.getClass());
        PaymentResponseRedirect responseRedirect = (PaymentResponseRedirect) response;
        final URL url = responseRedirect.getRedirectionRequest().getUrl();
        assertNotNull(url);
        assertEquals("https://mapi.alipaydev.com/gateway.do?foo=bar&foo2=baz", url.toString());
        assertEquals("partnerTransactionId", responseRedirect.getPartnerTransactionId());
    }

    @Test
    void paymentRequestMalformedURLException() {
        Mockito.doReturn(new HashMap<String,String>()).when(signatureUtils).getSignedParameters(any(), any());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");

        PaymentRequest.Builder aPaylinePaymentRequest = MockUtils.aPaylinePaymentRequestBuilder().withPartnerConfiguration(MockUtils.aPartnerConfigurationMalformedURLException());
        PaymentResponse response = underTest.paymentRequest(aPaylinePaymentRequest.build());
        assertEquals(PaymentResponseFailure.class, response.getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        assertEquals("PartnerConfig ALIPAY_URL is malformed", responseFailure.getErrorCode());
        assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
    }

    @Test
    void paymentRequestWithoutAlipayUrl() {
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequestBuilder()
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
        Mockito.doReturn(new HashMap<String,String>()).when(signatureUtils).getSignedParameters(any(), any());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");

        PaymentResponse response = underTest.paymentRequest(paymentRequest);

        assertEquals(PaymentResponseFailure.class, response.getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        assertEquals(PartnerConfigurationKeys.ALIPAY_URL + " is missing in the PartnerConfiguration", responseFailure.getErrorCode());
        assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }

    @Test
    void paymentRequestPluginException() {
        PluginException e = new PluginException("thisIsAmessage", FailureCause.CANCEL);
        Mockito.doThrow(e).when(signatureUtils).getSignedParameters(any(), any());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");

        PaymentRequest.Builder aPaylinePaymentRequest = MockUtils.aPaylinePaymentRequestBuilder().withPartnerConfiguration(MockUtils.aPartnerConfigurationMalformedURLException());
        PaymentResponse response = underTest.paymentRequest(aPaylinePaymentRequest.build());
        assertEquals(PaymentResponseFailure.class, response.getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        assertEquals("thisIsAmessage", responseFailure.getErrorCode());
        assertEquals(FailureCause.CANCEL, responseFailure.getFailureCause());
    }

    @Test
    void paymentRequestRuntimeException() {
        NullPointerException e = new NullPointerException("thisIsAmessage");
        Mockito.doThrow(e).when(signatureUtils).getSignedParameters(any(), any());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");

        PaymentRequest.Builder aPaylinePaymentRequest = MockUtils.aPaylinePaymentRequestBuilder().withPartnerConfiguration(MockUtils.aPartnerConfigurationMalformedURLException());
        PaymentResponse response = underTest.paymentRequest(aPaylinePaymentRequest.build());
        assertEquals(PaymentResponseFailure.class, response.getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        assertEquals("plugin error: NullPointerException: thisIsAmessage", responseFailure.getErrorCode());
        assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }
}