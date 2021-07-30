package com.payline.payment.alipay.bean.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.payline.payment.alipay.bean.object.Response;
import com.payline.payment.alipay.exception.InvalidDataException;

import java.io.IOException;


@JsonIgnoreProperties({ "request" })
@JacksonXmlRootElement(namespace = "alipay", localName = "AlipayAPIResponse")
public class APIResponse {

    @JacksonXmlProperty(localName = "is_success")
    private String isSuccess;
    @JacksonXmlProperty(localName = "sign_type")
    private String signType;
    private Response response;
    private String error;
    private String sign;

    private static XmlMapper xmlMapper = new XmlMapper();

    public String getIsSuccess() {
        return isSuccess;
    }

    public boolean isSuccess()
    {
        return getIsSuccess().equalsIgnoreCase("t");
    }

    public void setIsSuccess(String isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Response getResponse() {
        return response;
    }

    public String getError()
    {
        return error;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    public static APIResponse fromXml(String xml) {
        try {
            return xmlMapper.readValue(xml, APIResponse.class);
        } catch (IOException e) {
            throw new InvalidDataException("Unable to parse XML AlipayAPIResponse", e);
        }
    }
}
