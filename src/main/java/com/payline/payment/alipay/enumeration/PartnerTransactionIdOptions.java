package com.payline.payment.alipay.enumeration;

/**
 * Les différentes versions de partner transaction id
 */
public enum PartnerTransactionIdOptions {
    ORDER_REFERENCE("contract.partnerTransactionId.orderReference"),
    EDEL("contract.partnerTransactionId.edel");

    private final String i18nKey;

    PartnerTransactionIdOptions(final String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getI18nKey() {
        return i18nKey;
    }
}
