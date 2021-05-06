package com.payline.payment.alipay.utils;

import com.payline.payment.alipay.MockUtils;
import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.service.AcquirerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

class SignatureUtilsTest {

    @InjectMocks
    private final SignatureUtils underTest = SignatureUtils.getInstance();

    @Mock
    private AcquirerService acquirerService;

    private final String privateKeyMock = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCgdl6q4cdeAczUH2+rgCJN61FH3IbkRjrhVHXY5Pr44KdWcdFlSP763l/3n4H7BkMM08nVCfWIzDrBjqXhqG7J3VQo+wJ3Ojv5dSmvW0HND2jAqE6WFsVFzwuBP/cBXdWUpK0SiG4c2GZSMM6UJOVz/Lw+8w+mgsELbTZXzvrOyzO2css34JJaMz9zVVCsctbqiszytfs/NzrstiQgk3wfgg8xAwp8obe+/ziJqIWQd2cBkc+i/JfCK8Y215WOHWoErd/fkahfcACGlQ3y8AIedZGOIl85rG1lwqr3oKtgrQ8L2hHxlzXwvn076ONzqhSfEHLZCjgdQNg8b7FsAmsLAgMBAAECggEAEbCbBSm46fM22pfQns6UeZSctFhqgpX/fcHq8AtmY7OYnx4TIKBog82qNMlf8gWhtLNQh+z+JaOmQppwTS1dcWG2GoJ5ph1EaV7yRoPAAFRXjw2/BCrkeqq1sP0VRRBMMIPrFKT3xrRQq3T4ATsEFMJjGml/azL1B0Z1/D40RkHRX35Xm6QPnHH7NS/uXanT4D8DrYs9RYqcfWCQoupQFyOXJubWzAsEwSomlUo2S44rNLqb4f+oMWqdTdjG5BhyE4sMAk1WY3s6uaQro81rdAUvaQPaxBtJBnZ+K/uB7F+8OpEn2ds8/T8VrqIgzsUhbmyI4v/qakXb3xO4Sn7lIQKBgQDuIIPFAXFabHz4eQG1r/mYif24RBvI7CxUUtAp8TEo8DAGoMvTUBGBpbJBDJwrvlazD2CcbOIKGdooyHa3VLe7TVaPhC8GdLuX4ISRYrlUQG1yr4ZrhLkDnhcUQJU5SXFlndppegCUz8LVB+QNFb+P04bM3CFiq9RCJHVceIzZmwKBgQCsgZHwfjuDc+jQ+0dBWzachM1tz3yU3rjEX65G4Sae9YGAspXm413DOFfIWPw1VWOJgGO0hL6rhkZki62fy+8nk7ELf8wyasV63ZdDDquP4DS3An4ySDTjSXoIFQ7+BJuOa1zv5hBYeZjfqkOSn5w0qZgzPQykL2dWUUBVXlpDUQKBgQClY3PhyqS+FwedU/46AKUsqvrLlpJttIr9bh8iYmCwfFH+6INomv3tu/XYdDersiFpcE67lmxQc7AwDztVDcQmlQp/12EJ7N22GLw6dgUeXTknJH4KAz61IoIJEOoTxcy6tS5Tf+cTdFFvwHFGHUrVTCcd9e5CJ/Helo/kz92T7QKBgDh+qqPfQ3BWin2+fWSLEcSKvZhj5kH++vXc+/9ch0Bx2LqkQfF+rttbXWxQsDBrSZDjikO5YZRdAjq1f+Qgbq9AcC/6543vZ8NIRNyCNm+FFt6Eo32mJiP1kVCKTQGl0qf+w04Rw3KmSAmgAkdHnGmmzSCisrwmhio/q9kJcHgxAoGBAJyjGVXdQfqfv2osIVVhoJmxWY/mge0dxsbWQUt9EVC8RZPYorFlOGrma6CGKEnnEHnvp1GaV8FnyhtB90amsZ1+QXaNXMUHw8F+5W/YYg0L7fbv2Bfb+BCV5KGGXThOhBLo9AOCk+YxlwCYj4uB69tIWN87Oi8CuORMh/i+Aa07";
    private final String publicKeyMock = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoHZequHHXgHM1B9vq4AiTetRR9yG5EY64VR12OT6+OCnVnHRZUj++t5f95+B+wZDDNPJ1Qn1iMw6wY6l4ahuyd1UKPsCdzo7+XUpr1tBzQ9owKhOlhbFRc8LgT/3AV3VlKStEohuHNhmUjDOlCTlc/y8PvMPpoLBC202V876zssztnLLN+CSWjM/c1VQrHLW6orM8rX7Pzc67LYkIJN8H4IPMQMKfKG3vv84iaiFkHdnAZHPovyXwivGNteVjh1qBK3f35GoX3AAhpUN8vACHnWRjiJfOaxtZcKq96CrYK0PC9oR8Zc18L59O+jjc6oUnxBy2Qo4HUDYPG+xbAJrCwIDAQAB";
    private final RequestConfiguration config = new RequestConfiguration(
            MockUtils.aContractConfiguration(),
            MockUtils.anEnvironment(),
            MockUtils.aPartnerConfiguration(),
            MockUtils.PLUGIN_CONFIGURATION
    );

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getPublicKey(){
        doReturn(MockUtils.anAcquirer()).when(acquirerService).fetchAcquirer(MockUtils.PLUGIN_CONFIGURATION, "id");

        PublicKey key = underTest.getPublicKey(config);

        Assertions.assertNotNull(key);
        Assertions.assertEquals("RSA",key.getAlgorithm());;
    }

    @Test
    void getPrivateKey(){
        PrivateKey key = underTest.getPrivateKey(config);
        Assertions.assertNotNull(key);
        Assertions.assertEquals("RSA", key.getAlgorithm());
    }


    @Test
    void getSignedParameters() {

        Map<String, String> map = new HashMap<>();
        map.put("foo","FOO");
        map.put("bar","BAR");
        map.put("baz","BAZ");
        String expected = "Nqr1u8l4YYFlbM7VMGrhJ7+tEz0WGQzznc8uBAohvfbed54bA5m4rHJwkUUE0GtqEkEB/3uY4zkeBLHbxDZpyxCkaFCErtxf60UBugegGojTwWe2Af6cmLkeAVQ/oPjA+9yYcXdO3bFkFsIhp+re4Ldyl4zHtnXUj0YBWukE5ul18qBZi4ExcFHeg40eR+AzckvxVWCs1+iaUKOrabVB8AI+q4dy0SQMycoBVUFs6Km6WxGGMyRDW+CuLfzfG8G+4ynjnxnWP1QHBM8++A9WgRWYjW0ZiYlsx61Wl+vhAZXaVDI5FpqHb5L9ZOUU8VvdJsZ6r8Z7eAqTop26HgQIdg==";

        Assertions.assertEquals(expected, underTest.getSignedParameters(config,map).get("sign"));
    }

    @Test
    void mapToString() {
        Map<String, String> map = new HashMap<>();
        map.put("foo","FOO");
        map.put("bar","BAR");
        map.put("baz","BAZ");

        Assertions.assertEquals("bar=BAR&baz=BAZ&foo=FOO", underTest.mapToString(map));
    }

    @Test
    void fullCycleTest() throws Exception{
        KeyFactory kf = KeyFactory.getInstance("RSA");
        // create privateKey
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyMock));
        PrivateKey privateKey = kf.generatePrivate(pkcs8EncodedKeySpec);

        // create publicKey
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyMock));
        PublicKey publicKey = kf.generatePublic(x509EncodedKeySpec);

        // test sign and verify
        String message = "foo";
        String signed = underTest.signSHA256withRSA(privateKey, message);

        boolean b = underTest.verifySignature(publicKey, message, signed);
        Assertions.assertTrue(b);
    }
}