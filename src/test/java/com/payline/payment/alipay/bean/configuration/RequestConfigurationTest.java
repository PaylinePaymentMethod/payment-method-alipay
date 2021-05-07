package com.payline.payment.alipay.bean.configuration;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.exception.PluginException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestConfigurationTest {

    @Test
    void constructor_nominal(){
        // given: the constructor is passed valid arguments, when: calling the constructor
        RequestConfiguration requestConfiguration = new RequestConfiguration( MockUtils.aContractConfiguration(),
                MockUtils.anEnvironment(), MockUtils.aPartnerConfiguration(), MockUtils.PLUGIN_CONFIGURATION);

        // then: the instance is not null, no exception is thrown
        assertNotNull( requestConfiguration );
    }

    @Test
    void constructor_nullContractConfiguration(){
        // given: the constructor is a null ContractConfiguration, when: calling the constructor, then: an exception is thrown
        assertThrows(PluginException.class, () -> new RequestConfiguration( null,
                MockUtils.anEnvironment(), MockUtils.aPartnerConfiguration(), MockUtils.PLUGIN_CONFIGURATION) );
    }

    @Test
    void constructor_nullEnvironment(){
        // given: the constructor is a null ContractConfiguration, when: calling the constructor, then: an exception is thrown
        assertThrows(PluginException.class, () -> new RequestConfiguration( MockUtils.aContractConfiguration(),
                null, MockUtils.aPartnerConfiguration(), MockUtils.PLUGIN_CONFIGURATION) );
    }

    @Test
    void constructor_nullPartnerConfiguration(){
        // given: the constructor is a null ContractConfiguration, when: calling the constructor, then: an exception is thrown
        assertThrows(PluginException.class, () -> new RequestConfiguration( MockUtils.aContractConfiguration(),
                MockUtils.anEnvironment(), null, MockUtils.PLUGIN_CONFIGURATION) );
    }

    @Test
    void constructor_nullPluginConfiguration(){
        // given: the constructor is a null ContractConfiguration, when: calling the constructor, then: an exception is thrown
        assertThrows(PluginException.class, () -> new RequestConfiguration( MockUtils.aContractConfiguration(),
                MockUtils.anEnvironment(), MockUtils.aPartnerConfiguration(), null) );
    }

}
