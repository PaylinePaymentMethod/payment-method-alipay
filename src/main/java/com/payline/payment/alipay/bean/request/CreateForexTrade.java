package com.payline.payment.alipay.bean.request;

import com.payline.payment.alipay.utils.PluginUtils;

import java.util.HashMap;
import java.util.Map;

public class CreateForexTrade extends Request {
    private final String currency;
    private final String notifyUrl;
    private final String productCode;
    private final String referUrl;
    private final String returnUrl;
    private final String subject;
    private final String totalFee;
    private final String supplier;
    private final String secondaryMerchantId;
    private final String secondaryMerchantName;
    private final String secondaryMerchantIndustry;

    private CreateForexTrade(CreateForexTradeBuilder builder) {
        super(builder);
        this.currency = builder.currency;
        this.notifyUrl = builder.notifyUrl;
        this.productCode = builder.productCode;
        this.referUrl = builder.referUrl;
        this.returnUrl = builder.returnUrl;
        this.subject = builder.subject;
        this.totalFee = builder.totalFee;
        this.supplier = builder.supplier;
        this.secondaryMerchantId = builder.secondaryMerchantId;
        this.secondaryMerchantName = builder.secondaryMerchantName;
        this.secondaryMerchantIndustry = builder.secondaryMerchantIndustry;
    }

    public static class CreateForexTradeBuilder extends RequestBuilder<CreateForexTradeBuilder> {
        private String currency;
        private String notifyUrl;
        private String productCode;
        private String referUrl;
        private String returnUrl;
        private String subject;
        private String totalFee;
        private String supplier;
        private String secondaryMerchantId;
        private String secondaryMerchantName;
        private String secondaryMerchantIndustry;

        public static CreateForexTradeBuilder aCreateForexTrade() {
            return new CreateForexTradeBuilder();
        }

        public CreateForexTrade.CreateForexTradeBuilder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withNotifyUrl(String notifyUrl) {
            this.notifyUrl = notifyUrl;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withProductCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withReferUrl(String referUrl) {
            this.referUrl = referUrl;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withTotalFee(String totalFee) {
            this.totalFee = totalFee;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withSupplier(String supplier) {
            this.supplier = supplier;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withSecondaryMerchantId(String secondaryMerchantId) {
            this.secondaryMerchantId = secondaryMerchantId;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withSecondaryMerchantName(String secondaryMerchantName) {
            this.secondaryMerchantName = secondaryMerchantName;
            return this;
        }

        public CreateForexTrade.CreateForexTradeBuilder withSecondaryMerchantIndustry(String secondaryMerchantIndustry) {
            this.secondaryMerchantIndustry = secondaryMerchantIndustry;
            return this;
        }

        public CreateForexTrade build() {
            return new CreateForexTrade(this);
        }
    }

    public Map<String, String> getParametersList() {
        Map<String, String> params = new HashMap<>();
        params.put("_input_charset", this.getInputCharset());
        params.put("currency", this.currency);
        if (!PluginUtils.isEmpty(this.notifyUrl)) {
            params.put("notify_url", this.notifyUrl);
        }
        params.put("out_trade_no", this.getOutTradeNo());
        params.put("partner", this.getPartner());
        params.put("product_code", this.productCode);
        // only non mandatory field
        if (!PluginUtils.isEmpty(this.referUrl)) {
            params.put("refer_url", this.referUrl);
        }
        params.put("return_url", this.returnUrl);
        params.put("secondary_merchant_id", this.secondaryMerchantId);
        params.put("secondary_merchant_industry", this.secondaryMerchantIndustry);
        params.put("secondary_merchant_name", this.secondaryMerchantName);
        params.put("service", this.getService().getService());
        params.put("subject", this.subject);
        params.put("supplier", this.supplier);
        params.put("total_fee", this.totalFee);
        return params;
    }
}
