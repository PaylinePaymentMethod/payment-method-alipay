package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.bean.object.Trade;
import com.payline.payment.alipay.bean.response.APIResponse;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.utils.SignatureUtils;
import com.payline.payment.alipay.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.stream.Stream;

import static com.payline.payment.alipay.bean.object.Trade.TradeStatus.*;
import static com.payline.payment.alipay.bean.response.NotifyResponse.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;

class NotificationServiceImplTest {
    @InjectMocks
    private NotificationServiceImpl service = new NotificationServiceImpl();

    @Mock
    private HttpClient client;

    @Mock
    private SignatureUtils signatureUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.doReturn(true).when(signatureUtils).getVerification(any(),anyMap());
    }


    private static Stream<Arguments> TradesStatus() {
        return Stream.of(
                Arguments.of(TRADE_FINISHED, PaymentResponseSuccess.class),
                Arguments.of(WAIT_BUYER_PAY, PaymentResponseOnHold.class),
                Arguments.of(TRADE_CLOSED, PaymentResponseFailure.class)
        );
    }


    @ParameterizedTest
    @MethodSource("TradesStatus")
    void parse(Trade.TradeStatus status, Class clazz) {
        // Mock
        Mockito.doReturn(TRUE).when(client).notificationIsVerified(any(), any());
        APIResponse mockResponse = APIResponse.fromXml(MockUtils.transactionBody(status));
        Mockito.doReturn(mockResponse).when(client).get(any(), any());

        // call method
        NotificationRequest request = MockUtils.aPaylineNotificationRequest(MockUtils.notificationBodyPaid);
        NotificationResponse response = service.parse(request);

        // assertions
        Assertions.assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        Assertions.assertEquals(clazz, paymentResponseByNotificationResponse.getPaymentResponse().getClass());
    }

    @Test
    void parseInvalidNotification(){


        // call method
        NotificationRequest request = MockUtils.aPaylineNotificationRequest("thisIsaBadNotification");
        NotificationResponse response = service.parse(request);

        // assertions
        Assertions.assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponseByNotificationResponse.getPaymentResponse().getClass());

    }

    @Test
    void parseFailureSingleTradeQuery() {
        // Mock
        Mockito.doReturn(TRUE).when(client).notificationIsVerified(any(), any());
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<alipay>\n" +
                "    <is_success>F</is_success>\n" +
                "    <error>ERROR</error>\n" +
                "</alipay>";
        APIResponse mockResponse = APIResponse.fromXml(xml);
        Mockito.doReturn(mockResponse).when(client).get(any(), any());

        // call method
        NotificationRequest request = MockUtils.aPaylineNotificationRequest(MockUtils.notificationBodyPaid);
        NotificationResponse response = service.parse(request);

        // assertions
        Assertions.assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponseByNotificationResponse.getPaymentResponse().getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponseByNotificationResponse.getPaymentResponse();
        Assertions.assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
        Assertions.assertEquals("ERROR", responseFailure.getErrorCode());
    }

    @Test
    void parseFalseNotificationRequest() {
        // Mock
        Mockito.doReturn(FALSE).when(client).notificationIsVerified(any(), any());

        // call method
        NotificationRequest request = MockUtils.aPaylineNotificationRequest(MockUtils.notificationBodyPaid);
        NotificationResponse response = service.parse(request);

        // assertions
        Assertions.assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponseByNotificationResponse.getPaymentResponse().getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponseByNotificationResponse.getPaymentResponse();
        Assertions.assertEquals(FailureCause.REFUSED, responseFailure.getFailureCause());
    }

    @Test
    void parseInvalidNotificationRequest() {
        // Mock
        Mockito.doReturn(INVALID).when(client).notificationIsVerified(any(), any());

        // call method
        NotificationRequest request = MockUtils.aPaylineNotificationRequest(MockUtils.notificationBodyPaid);
        NotificationResponse response = service.parse(request);

        // assertions
        Assertions.assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponseByNotificationResponse.getPaymentResponse().getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponseByNotificationResponse.getPaymentResponse();
        Assertions.assertEquals(FailureCause.INVALID_DATA, responseFailure.getFailureCause());
    }

    @Test
    void parsePluginException() {
        // Mock
        PluginException e = new PluginException("ERROR", FailureCause.REFUSED);
        Mockito.doThrow(e).when(client).notificationIsVerified(any(), any());

        // call method
        NotificationRequest request = MockUtils.aPaylineNotificationRequest(MockUtils.notificationBodyPaid);
        NotificationResponse response = service.parse(request);

        // assertions
        Assertions.assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponseByNotificationResponse.getPaymentResponse().getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponseByNotificationResponse.getPaymentResponse();
        Assertions.assertEquals(FailureCause.REFUSED, responseFailure.getFailureCause());
        Assertions.assertEquals("ERROR", responseFailure.getErrorCode());
    }

    @Test
    void parseRuntimeException() {
        // Mock
        NullPointerException e = new NullPointerException("ERROR");
        Mockito.doThrow(e).when(client).notificationIsVerified(any(), any());

        // call method
        NotificationRequest request = MockUtils.aPaylineNotificationRequest(MockUtils.notificationBodyPaid);
        NotificationResponse response = service.parse(request);

        // assertions
        Assertions.assertEquals(PaymentResponseByNotificationResponse.class, response.getClass());
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        Assertions.assertEquals(PaymentResponseFailure.class, paymentResponseByNotificationResponse.getPaymentResponse().getClass());

        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponseByNotificationResponse.getPaymentResponse();
        Assertions.assertEquals(FailureCause.INTERNAL_ERROR, responseFailure.getFailureCause());
        Assertions.assertEquals("plugin error: NullPointerException: ERROR", responseFailure.getErrorCode());
    }
}
