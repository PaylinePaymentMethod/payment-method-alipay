package com.payline.payment.alipay.bean.object;

import com.google.gson.annotations.SerializedName;

public enum ForexService {
        @SerializedName("create_forex_trade")
        CREATE_FOREX_TRADE("create_forex_trade"),
        @SerializedName("create_forex_trade_wap")
        CREATE_FOREX_TRADE_WAP("create_forex_trade_wap"),
        @SerializedName("single_trade_query")
        SINGLE_TRADE_QUERY("single_trade_query"),
        @SerializedName("forex_refund")
        FOREX_REFUND("forex_refund"),
        @SerializedName("notify_verify")
        NOTIFY_VERIFY("notify_verify");

        private final String service;

        public String getService() {
                return service;
        }

        ForexService(String service) {
                this.service = service;
        }
}
