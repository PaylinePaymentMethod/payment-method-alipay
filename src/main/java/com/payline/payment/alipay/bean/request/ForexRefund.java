package com.payline.payment.alipay.bean.request;

import java.util.HashMap;
import java.util.Map;

public class ForexRefund extends Request {
    private final String currency;
    private final String gmtReturn;
    private final String isSync;
    private final String outReturnNo;
    private final String productCode;
    private final String returnAmount;

    private ForexRefund(ForexRefundBuilder builder) {
        super(builder);
        this.currency = builder.currency;
        this.gmtReturn = builder.gmtReturn;
        this.isSync = builder.isSync;
        this.outReturnNo = builder.outReturnNo;
        this.productCode = builder.productCode;
        this.returnAmount = builder.returnAmount;
    }

    public static class ForexRefundBuilder extends RequestBuilder<ForexRefund.ForexRefundBuilder> {
        private String currency;
        private String gmtReturn;
        private String isSync;
        private String outReturnNo;
        private String productCode;
        private String returnAmount;

        public static ForexRefund.ForexRefundBuilder aForexRefund() {
            return new ForexRefund.ForexRefundBuilder();
        }

        public ForexRefund.ForexRefundBuilder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public ForexRefund.ForexRefundBuilder withGmtReturn(String gmtReturn) {
            this.gmtReturn = gmtReturn;
            return this;
        }

        public ForexRefund.ForexRefundBuilder withIsSync(String isSync) {
            this.isSync = isSync;
            return this;
        }

        public ForexRefund.ForexRefundBuilder withOutReturnNo(String outReturnNo) {
            this.outReturnNo = outReturnNo;
            return this;
        }

        public ForexRefund.ForexRefundBuilder withProductCode(String productCode) {
            this.productCode = productCode;
            return this;
        }

        public ForexRefund.ForexRefundBuilder withReturnAmount(String returnAmount) {
            this.returnAmount = returnAmount;
            return this;
        }

        public ForexRefund build() {
            return new ForexRefund(this);
        }
    }

    public Map<String, String> getParametersList() {
        //Create parameters list
        Map<String, String> params = new HashMap<>();
        params.put("_input_charset", this.getInputCharset());
        params.put("currency", this.currency);
        params.put("gmt_return", this.gmtReturn);
        params.put("is_sync", this.isSync);
        params.put("out_return_no", this.outReturnNo);
        params.put("out_trade_no", this.getOutTradeNo());
        params.put("partner", this.getPartner());
        params.put("product_code", this.productCode);
        params.put("return_amount", this.returnAmount);
        params.put("service", this.getService().getService());
        return params;
    }
}
