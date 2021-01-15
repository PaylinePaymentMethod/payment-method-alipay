package com.payline.payment.alipay.utils.http;

import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.bean.request.EndTransactionNotificationRequest;
import com.payline.payment.alipay.bean.response.APIResponse;
import com.payline.payment.alipay.bean.response.NotifyResponse;
import com.payline.payment.alipay.exception.InvalidDataException;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.utils.EndTransactionNotificationUtils;
import com.payline.payment.alipay.utils.PluginUtils;
import com.payline.payment.alipay.utils.SignatureUtils;
import com.payline.payment.alipay.utils.business.ErrorUtils;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.payment.alipay.utils.constant.PartnerConfigurationKeys;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.logger.LogManager;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HttpClient {
    private static final Logger LOGGER = LogManager.getLogger(HttpClient.class);

    private org.apache.http.client.HttpClient client;
    private SignatureUtils signatureUtils = SignatureUtils.getInstance();

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    HttpClient() {
        // instantiate Apache HTTP client
        this.client = HttpClientBuilder.create()
                .useSystemProperties()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), SSLConnectionSocketFactory.getDefaultHostnameVerifier()))
                .build();

    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    private static class Holder {
        private static final HttpClient instance = new HttpClient();
    }

    /**
     * Return HttpClient instance
     *
     * @return HttpClient instance
     */
    public static HttpClient getInstance() {
        return Holder.instance;
    }
    // --- Singleton Holder pattern + initialization END

    /**
     * Send the request, with a retry system in case the client does not obtain a proper response from the server.
     *
     * @param httpRequest The request to send.
     * @return The response converted as a {@link StringResponse}.
     * @throws PluginException If an error repeatedly occurs and no proper response is obtained.
     */
    StringResponse execute(HttpRequestBase httpRequest) {
        StringResponse strResponse = null;
        int attempts = 1;
        int retries = 3;

        while (strResponse == null && attempts <= retries) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Start call to partner API (attempt {}) : {}{}", attempts, System.lineSeparator(), PluginUtils.requestToString(httpRequest));
            } else {
                LOGGER.info("Start call to partner API [{} {}] (attempt {})", httpRequest.getMethod(), httpRequest.getURI(), attempts);
            }
            try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) this.client.execute(httpRequest)) {
                strResponse = StringResponse.fromHttpResponse(httpResponse);
            } catch (IOException e) {
                LOGGER.error("An error occurred during the HTTP call :", e);
                strResponse = null;
            } finally {
                attempts++;
            }
        }

        if (strResponse == null) {
            throw new PluginException("Failed to contact the partner API", FailureCause.COMMUNICATION_ERROR);
        }
        LOGGER.info("Response obtained from partner API [{} {}]", strResponse.getStatusCode(), strResponse.getStatusMessage());
        return strResponse;
    }


    /**
     * Build and send request to verify a notification by its id
     * Verification is done by calling an Alipay's service
     *
     * @param requestConfiguration
     * @param params
     */
    public NotifyResponse notificationIsVerified(RequestConfiguration requestConfiguration, Map<String, String> params) {
        try {
            // Create the HttpGet url with parameters
            URI baseUrl = constructURL(requestConfiguration);
            List<NameValuePair> list = fromMap(signatureUtils.getSignedParameters(requestConfiguration, params));
            URI uri = new URIBuilder(baseUrl)
                    .setParameters(list)
                    .build();

            // Create the HttpGet request
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(createHttpRequestConfig(requestConfiguration));

            // Execute request
            StringResponse response = this.execute(httpGet);
            return NotifyResponse.valueOf(response.getContent().toUpperCase());

        } catch (URISyntaxException e) {
            throw new InvalidDataException("Syntax Exception", e);
        }
    }

    /**
     * Build and send a GET request
     *
     * @param requestConfiguration
     * @return The response converted as a {@link APIResponse}.
     */
    public APIResponse get(RequestConfiguration requestConfiguration, Map<String, String> params) {
        try {
            // Create the HttpGet url with parameters
            URI baseUrl = constructURL(requestConfiguration);
            List<NameValuePair> list = fromMap(signatureUtils.getSignedParameters(requestConfiguration, params));
            URI uri = new URIBuilder(baseUrl)
                    .setParameters(list)
                    .build();

            // Create the HttpGet request
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setConfig(createHttpRequestConfig(requestConfiguration));

            // Execute request
            StringResponse response = this.execute(httpGet);

            APIResponse apiResponse = APIResponse.fromXml(response.getContent());
            if (!apiResponse.isSuccess()){
                String errorCode = apiResponse.getError();
                throw new PluginException(errorCode, ErrorUtils.getFailureCause(errorCode));
            }

            return apiResponse;

        } catch (URISyntaxException e) {
            throw new InvalidDataException("Syntax Exception", e);
        }
    }


    /**
     * Send post request with notification data
     *
     * @param requestConfiguration
     * @param endTransactionNotificationRequest
     * @return StringResponse
     */
    public StringResponse sendNotificationMonext(RequestConfiguration requestConfiguration, EndTransactionNotificationRequest endTransactionNotificationRequest) {
        try {
            URI baseUrl = new URI(requestConfiguration.getContractConfiguration().getProperty(ContractConfigurationKeys.NOTIFICATION_URL).getValue());
            // Create the httpPost request
            StringEntity entity = new StringEntity(EndTransactionNotificationUtils.toJson(endTransactionNotificationRequest));
            HttpPost httpPost = new HttpPost(baseUrl);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setConfig(createHttpRequestConfig(requestConfiguration));

            // Execute request
            return this.execute(httpPost);

        } catch (URISyntaxException | UnsupportedEncodingException e) {
            throw new InvalidDataException("Syntax Exception", e);
        }
    }

    /**
     * Construct an URI url from a String given in PartnerConfiguration
     *
     * @param requestConfiguration Contain the String to convert
     * @return {@link URI}
     */
    private URI constructURL(RequestConfiguration requestConfiguration) {
        // check if URL exists in partnerConfiguration
        String url = requestConfiguration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.ALIPAY_URL);
        if (PluginUtils.isEmpty(url)) {
            throw new InvalidDataException("Missing API url from partner configuration");
        }

        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidDataException("Service URL is invalid", e);
        }
    }

    /**
     * Allow to create "time out" configuration for Http Request
     *
     * @param requestConfiguration
     * @return {@link RequestConfig}
     */

    private RequestConfig createHttpRequestConfig(RequestConfiguration requestConfiguration) {
        // Timeout
        final String readTimeout = requestConfiguration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.READ_TIMEOUT);
        final String connectTimeout = requestConfiguration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.CONNECT_TIMEOUT);

        if (PluginUtils.isEmpty(readTimeout) || PluginUtils.isEmpty(connectTimeout)) {
            throw new InvalidDataException("Missing timeout from partner configuration");
        }

        return RequestConfig.custom()
                .setConnectTimeout(Integer.parseInt(connectTimeout))
                .setSocketTimeout(Integer.parseInt(readTimeout))
                .build();
    }


    /**
     * Convert Map to List<NameValuePair>
     *
     * @param map
     * @return {@link List<NameValuePair>}
     */
    private static List<NameValuePair> fromMap(Map<String, String> map) {
        List<NameValuePair> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return list;
    }

}
