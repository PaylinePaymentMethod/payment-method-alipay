package com.payline.payment.alipay.bean.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.alipay.bean.object.ForexService.SINGLE_TRADE_QUERY;

class SingleTradeQueryTest {

    @Test
    void get_input_charset() {
        SingleTradeQuery singleTradeQuery = SingleTradeQuery.SingleTradeQueryBuilder
                .aSingleTradeQuery()
                .withOutTradeNo("0")
                .withPartner("1")
                .withService(SINGLE_TRADE_QUERY)
                .build();

        Assertions.assertNotNull(singleTradeQuery);
        Assertions.assertNotNull(singleTradeQuery.getInputCharset());
        Assertions.assertEquals("UTF-8", singleTradeQuery.getInputCharset());
        Assertions.assertNotNull(singleTradeQuery.getOutTradeNo());
        Assertions.assertEquals("0", singleTradeQuery.getOutTradeNo());
        Assertions.assertNotNull(singleTradeQuery.getPartner());
        Assertions.assertEquals("1", singleTradeQuery.getPartner());
        Assertions.assertNotNull(singleTradeQuery.getService());
        Assertions.assertEquals(SINGLE_TRADE_QUERY, singleTradeQuery.getService());
    }


}
