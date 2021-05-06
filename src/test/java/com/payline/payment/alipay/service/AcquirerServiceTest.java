package com.payline.payment.alipay.service;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.bean.configuration.Acquirer;
import com.payline.payment.alipay.exception.PluginException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AcquirerServiceTest {

    private final AcquirerService underTest = AcquirerService.getInstance();
    
    @Test
    void retrieveAcquirers() {
        final List<Acquirer> acquirers = underTest.retrieveAcquirers(MockUtils.PLUGIN_CONFIGURATION);

        assertNotNull(acquirers);
        assertEquals(2, acquirers.size());

        assertEquals("1", acquirers.get(0).getId());
        assertEquals("Label 1", acquirers.get(0).getLabel());
        assertEquals("pid1", acquirers.get(0).getMerchantPID());
        assertEquals("alipay_key1", acquirers.get(0).getAlipayPublicKey());
        assertEquals("EUR", acquirers.get(0).getCurrency());

        assertEquals("2", acquirers.get(1).getId());
        assertEquals("Label 2", acquirers.get(1).getLabel());
        assertEquals("pid2", acquirers.get(1).getMerchantPID());
        assertEquals("alipay_key2", acquirers.get(1).getAlipayPublicKey());
        assertEquals("USD", acquirers.get(1).getCurrency());
    }

    @Test
    void fetchAcquirer() {
        final Acquirer acquirer = underTest.fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION,"1");

        assertNotNull(acquirer);
        assertEquals("1", acquirer.getId());
        assertEquals("Label 1", acquirer.getLabel());
        assertEquals("pid1", acquirer.getMerchantPID());
        assertEquals("alipay_key1", acquirer.getAlipayPublicKey());
        assertEquals("EUR", acquirer.getCurrency());


    }

    static Stream<String> badPluginConfiguration() {
        return Stream.of("", "   ", null);
    }

    @ParameterizedTest
    @MethodSource("badPluginConfiguration")
    void retrieveAcquirersError(final String pluginConfiguration) {
        assertThrows(PluginException.class, () -> underTest.retrieveAcquirers(pluginConfiguration));
    }

    @ParameterizedTest
    @MethodSource("badPluginConfiguration")
    void fetchAcquirerError(final String pluginConfiguration) {
        assertThrows(PluginException.class, () -> underTest.fetchAcquirer(pluginConfiguration, "5"));
    }
}