package com.payline.payment.alipay.service.impl;

import com.payline.payment.alipay.bean.configuration.RequestConfiguration;
import com.payline.payment.alipay.bean.request.ForexRefund;
import com.payline.payment.alipay.exception.PluginException;
import com.payline.payment.alipay.service.AcquirerService;
import com.payline.payment.alipay.utils.PluginUtils;
import com.payline.payment.alipay.utils.constant.ContractConfigurationKeys;
import com.payline.payment.alipay.utils.http.HttpClient;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.RefundService;
import org.apache.logging.log4j.Logger;

import java.util.Date;

import static com.payline.payment.alipay.bean.object.ForexService.FOREX_REFUND;

public class RefundServiceImpl implements RefundService {
    private static final Logger LOGGER = LogManager.getLogger(RefundServiceImpl.class);
    private HttpClient client = HttpClient.getInstance();
    private AcquirerService acquirerService = AcquirerService.getInstance();


    @Override
    public RefundResponse refundRequest(RefundRequest refundRequest) {
        RefundResponse refundResponse;
        try {
            RequestConfiguration configuration = RequestConfiguration.build(refundRequest);

            // create ForexRefund request object
            final String merchantPID = acquirerService.fetchAcquirer(configuration.getPluginConfiguration(),
                    configuration.getContractConfiguration().getProperty(ContractConfigurationKeys.ACQUIRER_ID).getValue()).getMerchantPID();
            ForexRefund forexRefund = ForexRefund.ForexRefundBuilder
                    .aForexRefund()
                    .withCurrency(refundRequest.getAmount().getCurrency().getCurrencyCode())
                    .withGmtReturn(PluginUtils.formatDate(new Date()))
                    .withIsSync("Y")
                    .withOutReturnNo(refundRequest.getTransactionId())  // refund Id
                    .withOutTradeNo(refundRequest.getPartnerTransactionId())   // transaction Id
                    .withPartner(merchantPID)
                    .withProductCode("NEW_OVERSEAS_SELLER")
                    .withReturnAmount(PluginUtils.createStringAmount(refundRequest.getAmount()))
                    .withService(FOREX_REFUND)
                    .build();

            // call refund API
            client.get(configuration, forexRefund.getParametersList());

            // if code goes here, the refund is a success.
            refundResponse = RefundResponseSuccess.RefundResponseSuccessBuilder.aRefundResponseSuccess()
                    .withPartnerTransactionId(refundRequest.getPartnerTransactionId())
                    .withStatusCode("200")
                    .build();
        } catch (PluginException e) {
            LOGGER.error(e.getErrorCode(), e);
            refundResponse = e.toRefundResponseFailureBuilder().build();

        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            refundResponse = RefundResponseFailure.RefundResponseFailureBuilder.aRefundResponseFailure()
                    .withErrorCode(PluginException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();

        }
        return refundResponse;
    }

    @Override
    public boolean canMultiple() {
        return true;
    }

    @Override
    public boolean canPartial() {
        return true;
    }

}
