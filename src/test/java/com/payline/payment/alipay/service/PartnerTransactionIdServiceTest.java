package com.payline.payment.alipay.service;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.enumeration.PartnerTransactionIdOptions;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class PartnerTransactionIdServiceTest {

    @Spy
    private PartnerTransactionIdService underTest = PartnerTransactionIdService.getInstance();

    @Test
    void retreivePartnerTransactionIdEdel() {
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        paymentRequest.getContractConfiguration().getContractProperties()
                .put(ContractConfigurationKeys.PARTNER_TRANSACTION_ID, new ContractProperty(PartnerTransactionIdOptions.EDEL.name()));
        final String partnerTransactionId = "partnerTransactionId";

        doReturn(partnerTransactionId).when(underTest).computeEdelPartnerTransactionId(paymentRequest);

        final String result = underTest.retreivePartnerTransactionId(paymentRequest);

        assertEquals(partnerTransactionId, result);
    }

    @Test
    void retreivePartnerTransactionIdOrderReference() {
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequest();
        paymentRequest.getContractConfiguration().getContractProperties()
                .put(ContractConfigurationKeys.PARTNER_TRANSACTION_ID, new ContractProperty(PartnerTransactionIdOptions.ORDER_REFERENCE.name()));

        final String result = underTest.retreivePartnerTransactionId(paymentRequest);

        assertEquals(paymentRequest.getOrder().getReference(), result);
    }

    @Test
    void computeEdelPartnerTransactionId() {
        final String transactionIdDecimal = "G98765430051966";
        final PaymentRequest paymentRequest = MockUtils.aPaylinePaymentRequestBuilder()
                .withTransactionId(transactionIdDecimal)
                .build();

        doReturn(LocalDateTime.of(2020, 12, 13, 14, 15, 16)).when(underTest).currentDateTime();

        final String partnerTransactionId = underTest.computeEdelPartnerTransactionId(paymentRequest);

        assertEquals("A000CAFE03039140ED80032ED93CE20C", partnerTransactionId);
    }
}