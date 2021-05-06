package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.bean.response.APIResponse;
import com.payline.payment.alipay.service.AcquirerService;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.payment.alipay.utils.http.HttpClient;
import com.payline.payment.alipay.utils.properties.ReleaseProperties;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.configuration.request.ContractParametersRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;

class ConfigurationServiceImplTest {

    /* I18nService is not mocked here on purpose, to validate the existence of all
    the messages related to this class, at least in the default locale */
    @Mock
    private ReleaseProperties releaseProperties;
    @Mock
    HttpClient httpClient;

    @Mock
    private AcquirerService acquirerService;

    @InjectMocks
    private ConfigurationServiceImpl service;

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    @BeforeAll
    static void before() {
        // This allows to test the default messages.properties file (no locale suffix)
        Locale.setDefault(Locale.CHINESE);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    @AfterAll
    static void after() {
        // Back to standard default locale
        Locale.setDefault(Locale.ENGLISH);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    @Test
    void getName() {
        // when: calling the method getName
        String name = service.getName(Locale.getDefault());

        // then: the method returns the name
        assertNotNull(name);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    @ParameterizedTest
    @MethodSource("getLocales")
    void getParameters(Locale locale) {
        final ContractParametersRequest contractParametersRequest = MockUtils.aContractParametersRequest();
        doReturn(MockUtils.acquirers()).when(acquirerService).retrieveAcquirers(MockUtils.PLUGIN_CONFIGURATION);

        // when: retrieving the contract parameters
        List<AbstractParameter> parameters = service.getParameters(contractParametersRequest);

        // then: each parameter has a unique key, a label and a description. List box parameters have at least 1 possible value.
        List<String> keys = new ArrayList<>();
        for (AbstractParameter param : parameters) {
            // 2 different parameters should not have the same key
            assertFalse(keys.contains(param.getKey()));
            keys.add(param.getKey());

            // each parameter should have a label and a description
            assertNotNull(param.getLabel());
            assertFalse(param.getLabel().contains("???"));
            assertNotNull(param.getDescription());
            assertFalse(param.getDescription().contains("???"));

            // in case of a ListBoxParameter, it should have at least 1 value
            if (param instanceof ListBoxParameter) {
                assertFalse(((ListBoxParameter) param).getList().isEmpty());
            }
        }
    }

    /**
     * Set of locales to test the getParameters() method. ZZ allows to search in the default messages.properties file.
     */
    static Stream<Locale> getLocales() {
        return Stream.of(Locale.FRENCH, Locale.ENGLISH, new Locale("ZZ"));
    }

    @Test
    void getReleaseInformation() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String version = "M.m.p";

        // given: the release properties are OK
        doReturn(version).when(releaseProperties).get("release.version");
        Calendar cal = new GregorianCalendar();
        cal.set(2019, Calendar.AUGUST, 19);
        doReturn(formatter.format(cal.getTime())).when(releaseProperties).get("release.date");

        // when: calling the method getReleaseInformation
        ReleaseInformation releaseInformation = service.getReleaseInformation();

        // then: releaseInformation contains the right values
        assertEquals(version, releaseInformation.getVersion());
        assertEquals(2019, releaseInformation.getDate().getYear());
        assertEquals(Month.AUGUST, releaseInformation.getDate().getMonth());
        assertEquals(19, releaseInformation.getDate().getDayOfMonth());
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    @Test
    void check_nominal() {

        // given: a valid configuration, including client ID / secret
        ContractParametersCheckRequest checkRequest = MockUtils.aContractParametersCheckRequest();

        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<alipay>\n" +
                "    <is_success>F</is_success>\n" +
                "    <error>TRADE_NOT_EXIST</error>\n" +
                "</alipay>";
        APIResponse mockResponse = APIResponse.fromXml(xml);
        doReturn(mockResponse).when(httpClient).get(any(RequestConfiguration.class), anyMap());
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");


        // when: checking the configuration
        Map<String, String> errors = service.check(checkRequest);

        // then: error map is empty
        assertTrue(errors.isEmpty());
    }



    @Test
    void check_missingParameter() {

        // given: a valid configuration, including client ID / secret
        ContractParametersCheckRequest checkRequest = MockUtils.aContractParametersCheckRequestBuilder()
                .withAccountInfo(new HashMap<>())
                .build();
        // when: checking the configuration
        Map<String, String> errors = service.check(checkRequest);

        // then: error map is empty
        assertFalse(errors.isEmpty());
        assertTrue(errors.containsKey(ContractConfigurationKeys.ACQUIRER_ID));
        assertTrue(errors.containsKey(ContractConfigurationKeys.SECONDARY_MERCHANT_ID));
        assertTrue(errors.containsKey(ContractConfigurationKeys.SECONDARY_MERCHANT_NAME));
        assertTrue(errors.containsKey(ContractConfigurationKeys.SECONDARY_MERCHANT_INDUSTRY));
        assertTrue(errors.containsKey(ContractConfigurationKeys.SUPPLIER));
        assertTrue(errors.containsKey(ContractConfigurationKeys.MERCHANT_BANK_CODE));
    }
    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    @Test
    void check_illegalPartner() {

        // given: a valid configuration, including client ID / secret
        ContractParametersCheckRequest checkRequest = MockUtils.aContractParametersCheckRequest();

        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<alipay>\n" +
                "    <is_success>F</is_success>\n" +
                "    <error>ILLEGAL_PARTNER</error>\n" +
                "</alipay>";
        APIResponse mockResponse = APIResponse.fromXml(xml);
        doReturn(mockResponse).when(httpClient).get(any(RequestConfiguration.class), anyMap());

        // when: checking the configuration
        Map<String, String> errors = service.check(checkRequest);

        // then: error map contains one element
        assertTrue(service.check(checkRequest).size() > 0);
    }

    /**
     * ------------------------------------------------------------------------------------------------------------------
     */
    @Test
    void check_illegalSign() {

        // given: a valid configuration, including client ID / secret
        ContractParametersCheckRequest checkRequest = MockUtils.aContractParametersCheckRequest();

        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<alipay>\n" +
                "    <is_success>F</is_success>\n" +
                "    <error>ILLEGAL_SIGN</error>\n" +
                "</alipay>";
        APIResponse mockResponse = APIResponse.fromXml(xml);
        doReturn(mockResponse).when(httpClient).get(any(RequestConfiguration.class), anyMap());

        // when: checking the configuration
        Map<String, String> errors = service.check(checkRequest);

        // then: error map contains one element
        assertTrue(service.check(checkRequest).size() > 0);
    }


    @Test
    void check_invalidPrivateKey(){
        // given: a valid configuration, including client ID / secret
        ContractParametersCheckRequest checkRequest = MockUtils.aContractParametersCheckRequest();

        // when: checking the configuration
        assertTrue(service.check(checkRequest).size() > 0);

    }

}
