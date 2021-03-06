package com.payline.payment.alipay.service;

import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.utils.PluginUtils;
import com.payline.payment.alipay.utils.i18n.I18nService;
import com.payline.payment.alipay.utils.properties.ConfigProperties;
import com.payline.pmapi.bean.paymentform.bean.PaymentFormLogo;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.paymentform.response.logo.PaymentFormLogoResponse;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseFile;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentFormConfigurationService;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public abstract class LogoPaymentFormConfigurationService implements PaymentFormConfigurationService {
    private static final Logger LOGGER = LogManager.getLogger(LogoPaymentFormConfigurationService.class);

    private I18nService i18n = I18nService.getInstance();
    private ConfigProperties config = ConfigProperties.getInstance();

    @Override
    public PaymentFormLogoResponse getPaymentFormLogo(PaymentFormLogoRequest paymentFormLogoRequest) {
        Locale locale = paymentFormLogoRequest.getLocale();

        String sHeight = config.get("logo.height");
        if (PluginUtils.isEmpty(sHeight)) {
            LOGGER.error("No height for the logo");
            throw new PluginException("Plugin error: No height for the logo");
        }

        String sWidth = config.get("logo.width");
        if (PluginUtils.isEmpty(sWidth)) {
            LOGGER.error("No width for the logo");
            throw new PluginException("Plugin error: No width for the logo");
        }

        try {
            return PaymentFormLogoResponseFile.PaymentFormLogoResponseFileBuilder.aPaymentFormLogoResponseFile()
                    .withHeight(Integer.parseInt(sHeight))
                    .withWidth(Integer.parseInt(sWidth))
                    .withTitle(i18n.getMessage("paymentMethod.name", locale))
                    .withAlt(i18n.getMessage("paymentMethod.name", locale) + " logo")
                    .build();
        } catch (NumberFormatException e) {
            String errorMessage = "Plugin config error: logo height and width must be integers";
            LOGGER.error(errorMessage, e);
            throw new PluginException(errorMessage, e);
        }
    }

    @Override
    public PaymentFormLogo getLogo(String paymentMethodIdentifier, Locale locale) {
        String filename = config.get("logo.filename");
        if (PluginUtils.isEmpty(filename)) {
            LOGGER.error("No file name for the logo");
            throw new PluginException("Plugin error: No file name for the logo");
        }
        String format = config.get("logo.format");
        if (PluginUtils.isEmpty(format)) {
            LOGGER.error("no format defined for file {}", filename);
            throw new PluginException("Plugin error: No file format for the logo");
        }

        String contentType = config.get("logo.contentType");
        if (PluginUtils.isEmpty(format)) {
            LOGGER.error("no content type defined for file {}", filename);
            throw new PluginException("Plugin error: No content type for the logo");
        }

        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input == null) {
                LOGGER.error("Unable to load file {}", filename);
                throw new PluginException("Plugin error: unable to load the logo file");
            }
            // Read logo file
            BufferedImage logo = ImageIO.read(input);

            // Recover byte array from image
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(logo, format, baos);
            baos.close();

            return PaymentFormLogo.PaymentFormLogoBuilder.aPaymentFormLogo()
                    .withFile(baos.toByteArray())
                    .withContentType(contentType)
                    .build();
        } catch (IOException e) {
            String errorMessage = "Plugin error: unable to read the logo";
            LOGGER.error("Plugin error: unable to read the logo", e);
            throw new PluginException(errorMessage, e);
        }
    }


}
