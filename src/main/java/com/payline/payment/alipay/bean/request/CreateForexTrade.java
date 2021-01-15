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

    private CreateForexTrade(CreateForexTradeBuilder builder) {
        super(builder);
        this.currency = builder.currency;
        this.notifyUrl = builder.notifyUrl;
        this.productCode = builder.productCode;
        this.referUrl = builder.referUrl;
        this.returnUrl = builder.returnUrl;
        this.subject = builder.subject;
        this.totalFee = builder.totalFee;
    }

    public static class CreateForexTradeBuilder extends RequestBuilder<CreateForexTradeBuilder> {
        private String currency;
        private String notifyUrl;
        private String productCode;
        private String referUrl;
        private String returnUrl;
        private String subject;
        private String totalFee;

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

        public CreateForexTrade build() {
            return new CreateForexTrade(this);
        }
    }

    public Map<String, String> getParametersList() {
        Map<String, String> params = new HashMap<>();
        params.put("_input_charset", this.getInputCharset());
        params.put("currency", this.currency);
        params.put("notify_url", this.notifyUrl);
        params.put("out_trade_no", this.getOutTradeNo());
        params.put("partner", this.getPartner());
        params.put("product_code", this.productCode);
        // only non mandatory field
        if (!PluginUtils.isEmpty(this.referUrl)) {
            params.put("refer_url", this.referUrl);
        }
        params.put("return_url", this.returnUrl);
        params.put("service", this.getService().getService());
        params.put("subject", this.subject);
        params.put("total_fee", this.totalFee);
        return params;
    }
}
