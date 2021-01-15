package com.payline.payment.alipay.bean.request;

import com.payline.payment.alipay.bean.object.ForexService;

import java.util.HashMap;
import java.util.Map;

public class NotifyVerify {
    private final String notifyId;
    private final String partner;
    private final ForexService service;

    private NotifyVerify(NotifyVerifyBuilder builder) {
        this.notifyId = builder.notifyId;
        this.partner = builder.partner;
        this.service = builder.service;
    }

    public static class NotifyVerifyBuilder {
        private String notifyId;
        private String partner;
        private ForexService service;

        public static NotifyVerify.NotifyVerifyBuilder aNotifyVerify() {
            return new NotifyVerify.NotifyVerifyBuilder();
        }

        public NotifyVerify.NotifyVerifyBuilder withNotifyId(String notifyId) {
            this.notifyId = notifyId;
            return this;
        }

        public NotifyVerify.NotifyVerifyBuilder withPartner(String partner) {
            this.partner = partner;
            return this;
        }

        public NotifyVerify.NotifyVerifyBuilder withService(ForexService service) {
            this.service = service;
            return this;
        }

        public NotifyVerify build() {
            return new NotifyVerify(this);
        }
    }

    public Map<String, String> getParametersList() {
        //Create parameters list
        Map<String, String> params = new HashMap<>();
        params.put("notify_id", this.notifyId);
        params.put("partner", this.partner);
        params.put("service", this.service.name());
        return params;
    }
}
