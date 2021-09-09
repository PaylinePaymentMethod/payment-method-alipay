package com.payline.payment.alipay.bean.object;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Date;

public class Trade {

    @JacksonXmlProperty(localName = "buyer_email")
    private String buyerEmail;

    @JacksonXmlProperty(localName = "buyer_id")
    private String buyerId;

    private String discount;

    @JacksonXmlProperty(localName = "flag_trade_locked")
    private String flagTradeLocked;

    @JacksonXmlProperty(localName = "gmt_create")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtCreate;

    @JacksonXmlProperty(localName = "gmt_last_modified_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtLastModifiedTime;

    @JacksonXmlProperty(localName = "gmt_payment")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date gmtPayment;

    @JacksonXmlProperty(localName = "is_total_fee_adjust")
    private String isTotalFeeAdjust;

    @JacksonXmlProperty(localName = "operator_role")
    private String operatorRole;

    @JacksonXmlProperty(localName = "out_trade_no")
    private String outTradeNo;

    @JacksonXmlProperty(localName = "payment_type")
    private String paymentType;

    private String price;

    private String quantity;

    @JacksonXmlProperty(localName = "seller_email")
    private String sellerEmail;

    @JacksonXmlProperty(localName = "seller_id")
    private String sellerId;

    private String subject;

    @JacksonXmlProperty(localName = "to_buyer_fee")
    private String toBuyerFee;

    @JacksonXmlProperty(localName = "to_seller_fee")
    private String toSellerFee;

    @JacksonXmlProperty(localName = "total_fee")
    private String totalFee;

    @JacksonXmlProperty(localName = "trade_no")
    private String tradeNo;

    @JacksonXmlProperty(localName = "trade_status")
    private TradeStatus tradeStatus;

    @JacksonXmlProperty(localName = "use_coupon")
    private String useCoupon;

    @JacksonXmlProperty(localName = "time_out")
    private String timeOut;

    @JacksonXmlProperty(localName = "time_out_type")
    private String timeOutType;

    public String getDiscount() {
        return discount;
    }

    public String getPrice() {
        return price;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getSubject() {
        return subject;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getFlagTradeLocked() {
        return flagTradeLocked;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public Date getGmtLastModifiedTime() {
        return gmtLastModifiedTime;
    }

    public Date getGmtPayment() {
        return gmtPayment;
    }

    public String getIsTotalFeeAdjust() {
        return isTotalFeeAdjust;
    }

    public String getOperatorRole() {
        return operatorRole;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public String getSellerId() {
        return sellerId;
    }

    public String getToBuyerFee() {
        return toBuyerFee;
    }

    public String getToSellerFee() {
        return toSellerFee;
    }

    public String getTotalFee() {
        return totalFee;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public String getUseCoupon() {
        return useCoupon;
    }

    public enum TradeStatus {
        TRADE_FINISHED, WAIT_BUYER_PAY, TRADE_CLOSED
    }
}
