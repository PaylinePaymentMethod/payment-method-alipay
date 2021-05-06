package com.payline.payment.alipay.utils.http;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.utils.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.alipay.utils.http.HttpTestUtils.mockHttpResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpClientTest {

    @InjectMocks
    @Spy
    private HttpClient httpClient;
    @Mock
    private CloseableHttpClient http;

    @BeforeEach
    void setup() {
        // Init tested instance and inject mocks
        httpClient = new HttpClient();
        MockitoAnnotations.initMocks(this);
    }

    // --- Test SharegroopHttpClient#execute ---
    @Test
    void execute_nominal() throws IOException {
        // given: a properly formatted request, which gets a proper response
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        int expectedStatusCode = 200;
        String expectedStatusMessage = "OK";
        String expectedContent = "{\"content\":\"fake\"}";
        doReturn(mockHttpResponse(expectedStatusCode, expectedStatusMessage, expectedContent, null))
                .when(http).execute(request);

        // when: sending the request
        StringResponse stringResponse = httpClient.execute(request);

        // then: the content of the StringResponse reflects the content of the HTTP response
        assertNotNull(stringResponse);
        assertEquals(expectedStatusCode, stringResponse.getStatusCode());
        assertEquals(expectedStatusMessage, stringResponse.getStatusMessage());
        assertEquals(expectedContent, stringResponse.getContent());
    }
    /**------------------------------------------------------------------------------------------------------------------*/
    @Test
    void execute_retry() throws IOException {
        // given: the first 2 requests end up in timeout, the third request gets a response
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        when(http.execute(request))
                .thenThrow(ConnectTimeoutException.class)
                .thenThrow(ConnectTimeoutException.class)
                .thenReturn(mockHttpResponse(200, "OK", "content", null));

        // when: sending the request
        StringResponse stringResponse = httpClient.execute(request);

        // then: the client finally gets the response
        assertNotNull(stringResponse);
    }

    @Test
    void execute_retryFail() throws IOException {
        // given: a request which always gets an exception
        HttpGet request = new HttpGet("http://domain.test.fr/endpoint");
        doThrow(IOException.class).when(http).execute(request);

        // when: sending the request, a PluginException is thrown
        assertThrows(PluginException.class, () -> httpClient.execute(request));
    }
    /**------------------------------------------------------------------------------------------------------------------*/

    @Test
    void execute_invalidResponse() throws IOException {
        // given: a request that gets an invalid response (null)
        HttpGet request = new HttpGet("http://domain.test.fr/malfunctioning-endpoint");
        doReturn(null).when(http).execute(request);

        // when: sending the request, a PluginException is thrown
        assertThrows(PluginException.class, () -> httpClient.execute(request));
    }

    static PartnerConfiguration anInvalidPartnerConfiguration() {
        Map<String, String> partnerConfigurationMap = new HashMap<>();

        partnerConfigurationMap.put(PartnerConfigurationKeys.ALIPAY_URL, "://mapi.alipaydev.com/gateway.do");

        Map<String, String> sensitiveConfigurationMap = new HashMap<>();

        return new PartnerConfiguration(partnerConfigurationMap, sensitiveConfigurationMap);
    }

    // Test the single trade query API call
    @Test
    void verify_Single_Trade_Query() throws IOException {
        // given: the API base URL is missing from the partner configuration

        String content= "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<alipay><is_success>T</is_success><request><param name=\"out_trade_no\">PAYLINE20200116105303</param><param name=\"partner\">2088621926786355</param><param name=\"_input_charset\">utf-8</param><param name=\"service\">single_trade_query</param></request><response><trade><buyer_email>for***@alitest.com</buyer_email><buyer_id>2088622942481446</buyer_id><discount>0.00</discount><flag_trade_locked>0</flag_trade_locked><gmt_create>2020-01-16 17:56:12</gmt_create><gmt_last_modified_time>2020-01-16 17:56:13</gmt_last_modified_time><gmt_payment>2020-01-16 17:56:13</gmt_payment><is_total_fee_adjust>F</is_total_fee_adjust><operator_role>B</operator_role><out_trade_no>PAYLINE20200116105303</out_trade_no><payment_type>100</payment_type><price>73.82</price><quantity>1</quantity><seller_email>for***@alitest.com</seller_email><seller_id>2088621926786355</seller_id><subject>Test</subject><to_buyer_fee>0.00</to_buyer_fee><to_seller_fee>73.82</to_seller_fee><total_fee>73.82</total_fee><trade_no>2020011622001381441000075584</trade_no><trade_status>TRADE_FINISHED</trade_status><use_coupon>F</use_coupon></trade></response><sign>RkzpAs7cQNjw0JelHVZDj1rv7pcUr2O1xJoVOZzuqOlElz9hWM4I88ydNYinDhqcHDDIDvyuy06ceGAYutDYcTt7QRP/ZhUnaAy+ahlwt8Y+SrgiHYpero1qMkcc3xdy016f0oCnGkTE+k9myya5YvTBiFFtmGMTFyzfiiCWZDXKNM/CKODnEK1znqpWFr0r5RGdTJDvqxApjUwfm4OXyUW65d7O64+WQMSeT2XxNOLP60Ah91En73fSbXQ6uP6WdQ9GkHOPoemw2Tgmzw/Ehby5ILb9Br93t/pqO9hl9TcKDWCgx1PpErRUfer3u1UNU5lu8EvfHBEAkrEJR/JvGA==</sign><sign_type>RSA2</sign_type></alipay>";
        StringResponse response = HttpTestUtils.mockStringResponse(200, "OK", content, null);

        doReturn(response).when(httpClient).execute(any(HttpRequestBase.class));

        //assertNotNull(httpClient.single_trade_query(requestConfiguration));

        verify(http, never()).execute(any(HttpRequestBase.class));
    }
    /**------------------------------------------------------------------------------------------------------------------*/

}
