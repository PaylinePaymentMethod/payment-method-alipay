package com.payline.payment.alipay.utils;

import com.payline.payment.alipay.exception.InvalidDataException;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.Email;
import eu.bitwalker.useragentutils.DeviceType;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PluginUtils {
    private static final String PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";


    /* Static utility class : no need to instantiate it (to please Sonar) */
    private PluginUtils() {
    }

    /**
     * Convert the path and headers of a {@link HttpRequestBase} to a readable {@link String}.
     * Mainly, for debugging purpose.
     *
     * @param httpRequest the request to convert
     * @return request method, path and headers as a string
     */
    public static String requestToString(HttpRequestBase httpRequest) {
        String ln = System.lineSeparator();
        String str = httpRequest.getMethod() + " " + httpRequest.getURI() + ln;

        List<String> strHeaders = new ArrayList<>();
        for (Header h : httpRequest.getAllHeaders()) {
            // For obvious security reason, the value of Authorization header is never printed in the logs
            if (HttpHeaders.AUTHORIZATION.equals(h.getName())) {
                String[] value = h.getValue().split(" ");
                strHeaders.add(h.getName() + ": " + (value.length > 1 ? value[0] : "") + " *****");
            } else {
                strHeaders.add(h.getName() + ": " + h.getValue());
            }
        }
        str += String.join(ln, strHeaders);

        if (httpRequest instanceof HttpPost) {
            try {
                str += ln + new BufferedReader(new InputStreamReader(((HttpPost) httpRequest).getEntity().getContent()))
                        .lines()
                        .collect(Collectors.joining(ln));
            } catch (IOException e) {
                str += ln + "<< Error retrieving request body >>";
            }
        }

        return str;
    }

    /**
     * Truncate the given string with the given length, if necessary.
     *
     * @param value  The string to truncate
     * @param length The maximum allowed length
     * @return The truncated string
     */
    public static String truncate(String value, int length) {
        if (value != null && value.length() > length) {
            value = value.substring(0, length);
        }
        return value;
    }

    /**
     * Convert an InputStream into a String
     *
     * @param stream the InputStream to convert
     * @return the converted String encoded in UTF-8
     */
    public static String inputStreamToString(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Check if a String is null or empty
     *
     * @param value the String to check
     * @return
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }


    /**
     * create a date in the format yyyy-MM-dd HH:mm:ss
     *
     * @param date the date to convert
     * @return
     */
    public static String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        return format.format(date);
    }

    public static Map<String, String> createMapFromString(String message) {
        Map<String, String> map = new HashMap<>();
        Pattern pattern = Pattern.compile("(.+)=(.+)");
        Matcher matcher;

        String[] keyValuePairs = message.split("&");
        for (String kv : keyValuePairs) {
            matcher = pattern.matcher(kv);
            if (matcher.find()) {
                map.put(matcher.group(1), matcher.group(2));
            }
        }

        return map;
    }

    /**
     * Check if agent is a mobile agent or not
     *
     * @param agent the Browser agent
     * @return tru if the agent is a mobile agent
     */
    public static boolean mobileUser(String agent) {
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        DeviceType deviceType = userAgent.getOperatingSystem().getDeviceType();

        return DeviceType.MOBILE.equals(deviceType);
    }

    /**
     * create an Email object from val
     *
     * @param val the String to convert into an Email, if val is not a valid email, add '@id.com' at the end
     * @return a valid Email
     */
    public static Email buildEmail(String val) {
        val = val.replace("*", "X");

        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(val);
        if (!matcher.matches()) {
            val += "@id.com";
        }

        return Email.EmailBuilder.anEmail().withEmail(val).build();
    }


    public static Map<String, String> encode(Map<String, String> params) {
        try{
            //Create parameters list
            LinkedHashMap<String, String> parametersListForResponse = new LinkedHashMap<>();
            for (Map.Entry<String,String> e : params.entrySet()) {
                parametersListForResponse.put(e.getKey(), URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8.toString()));
            }
            return parametersListForResponse;
        } catch (UnsupportedEncodingException e) {
            throw new InvalidDataException("Unable to encode params", e);
        }
    }

    /**
     * Return a string which was converted from cents to currency amount
     *
     * @param amount the amount to convert
     * @return Amount as String
     */
    public static String createStringAmount(Amount amount) {
        int nbDigits = amount.getCurrency().getDefaultFractionDigits();

        StringBuilder sb = new StringBuilder();
        sb.append(amount.getAmountInSmallestUnit());

        for (int i = sb.length(); i < nbDigits+1; i++) {
            sb.insert(0, "0");
        }

        sb.insert(sb.length() - nbDigits, ".");
        return sb.toString();
    }

    /**
     * Left pad a string with zeros
     * @param input the string to pad
     * @param size   final size
     * @return padded string
     */
    public static String leftPad(final String input, final int size) {
        final String result;
        if (input == null || input.length() >= size) {
            result = input;
        } else {
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                stringBuilder.append('0');
            }
            result = stringBuilder.substring(input.length()) + input;
        }
        return result;
    }
}