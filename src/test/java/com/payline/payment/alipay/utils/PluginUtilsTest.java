package com.payline.payment.alipay.utils;

import com.payline.pmapi.bean.common.Amount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginUtilsTest {

    @Test
    void truncate() {
        assertEquals("", PluginUtils.truncate("foo", 0));
        assertEquals("foo", PluginUtils.truncate("foo", 3));
        assertEquals("foo", PluginUtils.truncate("foo", 5));
    }

    @Test
    void isEmpty() {
        Assertions.assertTrue(PluginUtils.isEmpty(null));
        Assertions.assertTrue(PluginUtils.isEmpty(""));
        Assertions.assertTrue(PluginUtils.isEmpty("   "));
        Assertions.assertFalse(PluginUtils.isEmpty("foo"));
    }

    @Test
    void formatDate() throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateInString = "12/12/2012";

        Date date = formatter.parse(dateInString);
        assertEquals("2012-12-12 00:00:00 CET", PluginUtils.formatDate(date));
    }

    @Test
    void testCreateMapFromString() {
        String message = "currency=USD" +
                "&notify_id=5ac236e4cf7822d205cedcc252b54ebwg1" +
                "&notify_time=2020-01-14 15:23:12" +
                "&notify_type=trade_status_sync" +
                "&out_trade_no=FALCN32YWXN2CL4KFT8" +
                "&sign=NN2trlV3PKBjZN7KS4oE8PG8WkHFqXIvvQl32fJ2FO9J+HniSuvv36VYPWbARVmodnTvYVkFmR2FB9ioDX0iRTRRSCkz8+ox3ytrlRdRfaeGMSGBuHN6WP/tAHscBbNvjkzyshjTCoXO6MFFg92CR2K50DvtNNUerZa/mx4lA5I=" +
                "&sign_type=RSA2" +
                "&total_fee=108.00" +
                "&trade_no=2020011421001003050502834160" +
                "&trade_status=TRADE_FINISHED";

        Map<String, String> expected = new HashMap<>();
        expected.put("currency", "USD");
        expected.put("notify_id", "5ac236e4cf7822d205cedcc252b54ebwg1");
        expected.put("notify_time", "2020-01-14 15:23:12");
        expected.put("notify_type", "trade_status_sync");
        expected.put("out_trade_no", "FALCN32YWXN2CL4KFT8");
        expected.put("sign", "NN2trlV3PKBjZN7KS4oE8PG8WkHFqXIvvQl32fJ2FO9J+HniSuvv36VYPWbARVmodnTvYVkFmR2FB9ioDX0iRTRRSCkz8+ox3ytrlRdRfaeGMSGBuHN6WP/tAHscBbNvjkzyshjTCoXO6MFFg92CR2K50DvtNNUerZa/mx4lA5I=");
        expected.put("sign_type", "RSA2");
        expected.put("total_fee", "108.00");
        expected.put("trade_no", "2020011421001003050502834160");
        expected.put("trade_status", "TRADE_FINISHED");

        assertEquals(expected, PluginUtils.createMapFromString(message));
    }

    @Test
    void createStringAmount() {
        BigInteger int1 = BigInteger.ZERO;
        BigInteger int2 = BigInteger.ONE;
        BigInteger int3 = BigInteger.TEN;
        BigInteger int4 = BigInteger.valueOf(100);
        BigInteger int5 = BigInteger.valueOf(1000);

        assertEquals("0.00", PluginUtils.createStringAmount( new Amount( int1, Currency.getInstance("EUR"))));
        assertEquals("0.01", PluginUtils.createStringAmount(new Amount(int2, Currency.getInstance("EUR"))));
        assertEquals("0.10", PluginUtils.createStringAmount(new Amount(int3, Currency.getInstance("EUR"))));
        assertEquals("1.00", PluginUtils.createStringAmount(new Amount(int4, Currency.getInstance("EUR"))));
        assertEquals("10.00", PluginUtils.createStringAmount(new Amount(int5, Currency.getInstance("EUR"))));
    }

    @CsvSource({
        "111,00111",
        "11111,11111",
        "111111,111111",
    })
    @ParameterizedTest
    void leftPad(final String input, final String expected) {
        assertEquals(expected, PluginUtils.leftPad(input, 5));
    }
}