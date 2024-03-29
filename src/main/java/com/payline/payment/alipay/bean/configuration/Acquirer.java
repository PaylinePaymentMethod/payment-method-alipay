package com.payline.payment.alipay.bean.configuration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Generated;
import lombok.Value;

@Value
@Builder
@Generated
@JsonDeserialize(builder = Acquirer.AcquirerBuilder.class)
public class Acquirer {
    String id;
    String label;
    String merchantPID;
    String alipayPublicKey;
    String currency;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AcquirerBuilder {

    }
}
