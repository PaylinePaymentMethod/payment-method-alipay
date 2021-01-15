package com.payline.payment.alipay.bean.request;

import java.util.HashMap;
import java.util.Map;

public class SingleTradeQuery extends Request {

    private SingleTradeQuery(SingleTradeQueryBuilder builder) {
        super(builder);
    }

    public static class SingleTradeQueryBuilder extends RequestBuilder<SingleTradeQueryBuilder> {

        public static SingleTradeQuery.SingleTradeQueryBuilder aSingleTradeQuery() {
            return new SingleTradeQuery.SingleTradeQueryBuilder();
        }

        public SingleTradeQuery build() {
            return new SingleTradeQuery(this);
        }
    }

    public Map<String, String> getParametersList() {
        //Create parameters list
        Map<String, String> params = new HashMap<>();
        params.put("_input_charset", this.getInputCharset());
        params.put("out_trade_no", this.getOutTradeNo());
        params.put("partner", this.getPartner());
        params.put("service", this.getService().getService());
        return params;
    }
}
