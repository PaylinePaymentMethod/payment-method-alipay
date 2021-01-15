package com.payline.payment.alipay.utils.business;

import com.payline.pmapi.bean.common.FailureCause;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ErrorUtilsTest {

    private static Stream<Arguments> errorCodes() {
        return Stream.of(
                Arguments.of("PURCHASE_TRADE_NOT_EXIST", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_SIGN", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_DYN_MD5_KEY", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_ENCRYPT", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_ARGUMENT", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_SERVICE", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_USER", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_PARTNER", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_AGENT", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_SIGN_TYPE", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_CLIENT_IP", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_CHARSET", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_DIGEST_TYPE", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_DIGEST", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_FILE_FORMAT", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_ENCODING", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_TARGET_SERVICE", FailureCause.INVALID_DATA),
                Arguments.of("ILLEGAL_EXTERFACE", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("ILLEGAL_PARTNER_EXTERFACE", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("ILLEGAL_SECURITY_PROFILE", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("HAS_NO_PRIVILEGE", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("EXTERFACE_IS_CLOSED", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("ILLEGAL_REQUEST_REFERER", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("ILLEGAL_ANTI_PHISHING_KEY", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("ANTI_PHISHING_KEY_TIMEOUT", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("ILLEGAL_EXTER_INVOKE_IP", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("SESSION_TIMEOUT", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("ILLEGAL_ACCESS_SWITCH_SYSTEM", FailureCause.COMMUNICATION_ERROR),
                Arguments.of("SYSTEM_ERROR", FailureCause.PARTNER_UNKNOWN_ERROR),
                Arguments.of("404 Not Found", FailureCause.PARTNER_UNKNOWN_ERROR),
                Arguments.of("XXXXXXXXXX", FailureCause.PARTNER_UNKNOWN_ERROR)
                );
    }


    @ParameterizedTest
    @MethodSource("errorCodes")
    void getFailureCause(String errorCode, FailureCause cause) {
        Assertions.assertEquals(cause, ErrorUtils.getFailureCause(errorCode));
    }
}