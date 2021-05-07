package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.bean.response.APIResponse;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.service.AcquirerService;
import com.payline.payment.alipay.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

class RefundServiceImplTest {
    @InjectMocks
    private RefundServiceImpl underTest = new RefundServiceImpl();
    @Mock
    private HttpClient client = HttpClient.getInstance();
    @Mock
    private AcquirerService acquirerService;

    @Captor
    private ArgumentCaptor<Map<String, String>> forexRefundParametersArgumentCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    void refundRequestOK() {
        String xmlOk = "<?xml version=\"1.0\" encoding=\"GBK\"?>\n" +
                "<alipay>\n" +
                "    <is_success>T</is_success>\n" +
                "</alipay>";
        APIResponse apiResponse = APIResponse.fromXml(xmlOk);
        doReturn(apiResponse).when(client).get(any(), forexRefundParametersArgumentCaptor.capture());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");

        RefundResponse response = underTest.refundRequest(MockUtils.aPaylineRefundRequest());
        Assertions.assertEquals(RefundResponseSuccess.class, response.getClass());

        RefundResponseSuccess responseSuccess = (RefundResponseSuccess) response;
        Assertions.assertEquals("PAYLINE20200116103352", responseSuccess.getPartnerTransactionId());
        Assertions.assertEquals("200", responseSuccess.getStatusCode());
        final Map<String, String> forexRefundParameters = forexRefundParametersArgumentCaptor.getValue();
        assertNotNull(forexRefundParameters);
        assertEquals("merchantPID", forexRefundParameters.get("partner"));
    }

    @Test
    void refundRequestPluginException() {
        PluginException e = new PluginException("foo", FailureCause.REFUSED);
        Mockito.doThrow(e).when(client).get(any(), any());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");

        RefundResponse response = underTest.refundRequest(MockUtils.anInvalidPaylineRefundRequest());
        Assertions.assertEquals(RefundResponseFailure.class, response.getClass());

        RefundResponseFailure responseFailure = (RefundResponseFailure) response;
        Assertions.assertEquals("foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.REFUSED, responseFailure.getFailureCause());
    }

    @Test
    void refundRequestRuntimeException() {
        NullPointerException e = new NullPointerException("foo");
        Mockito.doThrow(e).when(client).get(any(), any());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");

        RefundResponse response = underTest.refundRequest(MockUtils.anInvalidPaylineRefundRequest());
        Assertions.assertEquals(RefundResponseFailure.class, response.getClass());

        RefundResponseFailure responseFailure = (RefundResponseFailure) response;
        Assertions.assertEquals("plugin error: NullPointerException: foo", responseFailure.getErrorCode());
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
    }
}