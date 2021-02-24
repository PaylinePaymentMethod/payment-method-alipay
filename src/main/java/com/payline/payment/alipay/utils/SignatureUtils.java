package com.payline.payment.alipay.utils;

import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.exception.InvalidDataException;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.utils.constant.PartnerConfigurationKeys;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

public class SignatureUtils {
    private static final String SIGN = "sign";
    private static final String SIGN_TYPE = "sign_type";

    private static final String RSA = "RSA";
    private static final String SHA_256_WITH_RSA = "SHA256withRSA";

    private static final String INVALID_ALGORITHM = "Invalid algorithm";
    private static final String INVALID_KEYS_SPEC = "Invalid KeySpec";
    private static final String INVALID_SIGNATURE = "Unable to verify the signature";


    private static class Holder {
        private static final SignatureUtils instance = new SignatureUtils();
    }

    /**
     * Return SignatureUtils instance
     * @return SignatureUtils
     */
    public static SignatureUtils getInstance() {
        return SignatureUtils.Holder.instance;
    }

    /**
     * Return a map with request parameters + signature and sign_type
     * @param configuration
     * @param params
     * @return Map<String, String>
     */
    public Map<String, String> getSignedParameters(RequestConfiguration configuration, Map<String, String> params) {
        // Create the RSA2 signature
        PrivateKey privateKey = getPrivateKey(configuration);
        String preSigning = mapToString(params);
        String sha256withRsa = signSHA256withRSA(privateKey, preSigning);

        // Add the signature to the parameters
        params.put(SIGN, sha256withRsa);
        params.put(SIGN_TYPE, "RSA2");

        return params;
    }

    /**
     * Return true if signature is verified, else it returns no
     * @param configuration
     * @param map
     * @return boolean
     */
    public boolean getVerification(RequestConfiguration configuration, Map<String, String> map) {
        // extract data
        PublicKey publicKey = getPublicKey(configuration);
        String messageToCompare = mapToString(map);
        String signature = map.get(SIGN);

        // execute the verification
        // attention, la signature qui arrive de payline contient des espaces à la place des "+", il faut donc la
        // remettre en forme
        return verifySignature(publicKey, messageToCompare, signature.replace(" ", "+"));
    }

    /**
     * Convert map in String
     * @param map
     * @return String
     */
    public String mapToString(Map<String, String> map) {
        return map.entrySet()
                .stream()
                .filter(e -> !e.getKey().equals(SIGN_TYPE))
                .filter(e -> !e.getKey().equals(SIGN))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }


    /**
     * Generate a RSA signature based on the query parameters
     *
     * @param message
     * @return RSA signature
     */
    String signSHA256withRSA(PrivateKey privateKey, String message) {
        try {
            Signature signature = Signature.getInstance(SHA_256_WITH_RSA);
            signature.initSign(privateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signatureBytes);

        } catch (NoSuchAlgorithmException e) {
            // should never append, "SHA256withRSA" is a valid algorithm
            throw new PluginException(INVALID_ALGORITHM, e);
        } catch (SignatureException e) {
            throw new PluginException("Unable to sign the message", e);
        } catch (InvalidKeyException e) {
            throw new PluginException("Invalid Key", e);
        }
    }

    /**
     * verify if the signature is valid
     *
     * @param publicKey the Key to use to verify the signature
     * @param message   the message to compare
     * @param signature the signature of the message
     * @return
     */
    boolean verifySignature(PublicKey publicKey, String message, String signature) {
        try {
            Signature publicSignature = Signature.getInstance(SHA_256_WITH_RSA);
            publicSignature.initVerify(publicKey);
            publicSignature.update(message.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return publicSignature.verify(signatureBytes);

        } catch (NoSuchAlgorithmException e) {
            // should never append, "SHA256withRSA" is a valid algorithm
            throw new PluginException(INVALID_ALGORITHM, e);
        } catch (SignatureException e) {
            throw new PluginException(INVALID_SIGNATURE, e);
        } catch (InvalidKeyException e) {
            throw new PluginException("Invalid Key", e);
        }
    }


    /**
     * Get public key using RequestConfiguration
     * @param configuration
     * @return PublicKey
     */
    PublicKey getPublicKey(RequestConfiguration configuration) {
        try {
            // check if privateKey exists in partnerConfiguration
            String sKey = configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.PUBLIC_KEY);
            if (PluginUtils.isEmpty(sKey)) {
                throw new InvalidDataException("Missing public key from partner configuration");
            }

            // extract byte key
            String publicKey = sKey.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replace("\n", "");
            byte[] decoded = Base64.getDecoder().decode(publicKey);

            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance(RSA);
            return kf.generatePublic(spec);

        } catch (NoSuchAlgorithmException e) {
            // should never append, "RSA" is a valid algorithm
            throw new PluginException(INVALID_ALGORITHM, e);
        } catch (InvalidKeySpecException e) {
            throw new PluginException(INVALID_KEYS_SPEC, e);
        }
    }

    /**
     * Get private key using RequestConfiguration
     * @param configuration
     * @return PrivateKey
     */
    PrivateKey getPrivateKey(RequestConfiguration configuration) {
        try {
            // check if privateKey exists in partnerConfiguration
            String sKey = configuration.getPartnerConfiguration().getProperty(PartnerConfigurationKeys.PAYLINE_PRIVATE_KEY);
            if (PluginUtils.isEmpty(sKey)) {
                throw new InvalidDataException("Missing private key from partner configuration");
            }

            // extract byte key
            String privKeyPEM = sKey.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\n", "");
            byte[] decoded = Base64.getDecoder().decode(privKeyPEM);

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance(RSA);
            return kf.generatePrivate(spec);

        } catch (NoSuchAlgorithmException e) {
            // should never append, "RSA" is a valid algorithm
            throw new PluginException(INVALID_ALGORITHM, e);
        } catch (InvalidKeySpecException e) {
            throw new PluginException(INVALID_KEYS_SPEC, e);
        }
    }
}
