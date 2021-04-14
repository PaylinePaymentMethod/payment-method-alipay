package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.exception.PluginException;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class PaymentServiceImplTest {
    @InjectMocks
    private PaymentServiceImpl underTest = new PaymentServiceImpl();

    @Mock
    private SignatureUtils signatureUtils = SignatureUtils.getInstance();

    @Mock
    private PartnerTransactionIdService partnerTransactionIdService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void paymentRequestPCOK() {
        Map<String,String> params = new HashMap<>();
        params.put("foo", "bar");
        params.put("foo2", "baz");
        Mockito.doReturn(params).when(signatureUtils).getSignedParameters(any(), any());

        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();

        PaymentResponse response = underTest.paymentRequest(paymentRequest);

        Assertions.assertEquals(PaymentResponseRedirect.class, response.getClass());
        PaymentResponseRedirect responseRedirect = (PaymentResponseRedirect) response;
        final URL url = responseRedirect.getRedirectionRequest().getUrl();
        Assertions.assertNotNull(url);
        Assertions.assertEquals("https://mapi.alipaydev.com/gateway.do?foo=bar&foo2=baz", url.toString());

        verify(partnerTransactionIdService).retrievePartnerTransactionId(paymentRequest);
    }

    @Test
    void paymentRequestMobileOK() {
        Map<String,String> params = new HashMap<>();
        params.put("foo", "bar");
        params.put("foo2", "baz");
        Mockito.doReturn(params).when(signatureUtils).getSignedParameters(any(), any());

        PaymentResponse response = underTest.paymentRequest(MockUtils.aPaylinePaymentRequestBuilder().withBrowser(Browser.BrowserBuilder.aBrowser().withUserAgent("Mobile Safari").build()).build());

        Assertions.assertEquals(PaymentResponseRedirect.class, response.getClass());
        PaymentResponseRedirect responseRedirect = (PaymentResponseRedirect) response;
        final URL url = responseRedirect.getRedirectionRequest().getUrl();
        Assertions.assertNotNull(url);
        Assertions.assertEquals("https://mapi.alipaydev.com/gateway.do?foo=bar&foo2=baz", url.toString());
    }

    @Test
    void paymentRequestMalformedURLException() {
        Mockito.doReturn(new HashMap<String,String>()).when(signatureUtils).getSignedParameters(any(), any());

        PaymentRequest.Builder aPaylinePaymentRequest = MockUtils.aPaylinePaymentRequestBuilder().withPartnerConfiguration(MockUtils.aPartnerConfigurationMalformedURLException());
        PaymentResponse response = underTest.paymentRequest(aPaylinePaymentRequest.build());
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        Assertions.assertEquals("PartnerConfig ALIPAY_URL is malformed", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
    }

    @Test
    void paymentRequestWithoutAlipayUrl() {
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequestBuilder()
                .withPartnerConfiguration(new PartnerConfiguration(new HashMap<>(), new HashMap<>()))
                .build();
        Mockito.doReturn(new HashMap<String,String>()).when(signatureUtils).getSignedParameters(any(), any());

        PaymentResponse response = underTest.paymentRequest(paymentRequest);

        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        Assertions.assertEquals(PartnerConfigurationKeys.ALIPAY_URL + " is missing in the PartnerConfiguration", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }

    @Test
    void paymentRequestPluginException() {
        PluginException e = new PluginException("thisIsAmessage", FailureCause.CANCEL);
        Mockito.doThrow(e).when(signatureUtils).getSignedParameters(any(), any());

        PaymentRequest.Builder aPaylinePaymentRequest = MockUtils.aPaylinePaymentRequestBuilder().withPartnerConfiguration(MockUtils.aPartnerConfigurationMalformedURLException());
        PaymentResponse response = underTest.paymentRequest(aPaylinePaymentRequest.build());
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        Assertions.assertEquals("thisIsAmessage", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.CANCEL, responseFailure.getFailureCause());
    }

    @Test
    void paymentRequestRuntimeException() {
        NullPointerException e = new NullPointerException("thisIsAmessage");
        Mockito.doThrow(e).when(signatureUtils).getSignedParameters(any(), any());

        PaymentRequest.Builder aPaylinePaymentRequest = MockUtils.aPaylinePaymentRequestBuilder().withPartnerConfiguration(MockUtils.aPartnerConfigurationMalformedURLException());
        PaymentResponse response = underTest.paymentRequest(aPaylinePaymentRequest.build());
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        Assertions.assertEquals("plugin error: NullPointerException: thisIsAmessage", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }
}