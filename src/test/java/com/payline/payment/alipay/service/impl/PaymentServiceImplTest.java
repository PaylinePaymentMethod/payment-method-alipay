package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.utils.SignatureUtils;
import com.payline.pmapi.bean.common.FailureCause;
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

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

class PaymentServiceImplTest {
    @InjectMocks
    private PaymentServiceImpl service = new PaymentServiceImpl();

    @Mock
    private SignatureUtils signatureUtils = SignatureUtils.getInstance();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void paymentRequestPCOK() {
        Map<String,String> params = new HashMap<>();
        params.put("foo", "bar");
        Mockito.doReturn(params).when(signatureUtils).getSignedParameters(any(), any());

        PaymentResponse response = service.paymentRequest(MockUtils.aPaylinePaymentRequest());

        Assertions.assertEquals(PaymentResponseRedirect.class, response.getClass());
        PaymentResponseRedirect responseRedirect = (PaymentResponseRedirect) response;
        Assertions.assertEquals(params, responseRedirect.getRedirectionRequest().getParameters());
    }

    @Test
    void paymentRequestMobileOK() {
        Map<String,String> params = new HashMap<>();
        params.put("foo", "bar");
        Mockito.doReturn(params).when(signatureUtils).getSignedParameters(any(), any());

        PaymentResponse response = service.paymentRequest(MockUtils.aPaylinePaymentRequestBuilder().withBrowser(Browser.BrowserBuilder.aBrowser().withUserAgent("Mobile Safari").build()).build());

        Assertions.assertEquals(PaymentResponseRedirect.class, response.getClass());
        PaymentResponseRedirect responseRedirect = (PaymentResponseRedirect) response;
        Assertions.assertEquals(params, responseRedirect.getRedirectionRequest().getParameters());
    }

    @Test
    void paymentRequestMalformedURLException() {
        Mockito.doReturn(new HashMap<String,String>()).when(signatureUtils).getSignedParameters(any(), any());

        PaymentRequest.Builder aPaylinePaymentRequest = MockUtils.aPaylinePaymentRequestBuilder().withPartnerConfiguration(MockUtils.aPartnerConfigurationMalformedURLException());
        PaymentResponse response = service.paymentRequest(aPaylinePaymentRequest.build());
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        Assertions.assertEquals("PartnerConfig ALIPAY_URL is malformed", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
    }

    @Test
    void paymentRequestPluginException() {
        PluginException e = new PluginException("thisIsAmessage", FailureCause.CANCEL);
        Mockito.doThrow(e).when(signatureUtils).getSignedParameters(any(), any());

        PaymentRequest.Builder aPaylinePaymentRequest = MockUtils.aPaylinePaymentRequestBuilder().withPartnerConfiguration(MockUtils.aPartnerConfigurationMalformedURLException());
        PaymentResponse response = service.paymentRequest(aPaylinePaymentRequest.build());
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
        PaymentResponse response = service.paymentRequest(aPaylinePaymentRequest.build());
        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) response;
        Assertions.assertEquals("plugin error: NullPointerException: thisIsAmessage", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }
}