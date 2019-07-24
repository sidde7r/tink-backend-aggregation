package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class SibsChallengeDataEntity {
    private String image;
    private String data;
    private String imageLink;
    private Object otpMaxLength;
    private String otpFormat;
    private String additionalInformation;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public Object getOtpMaxLength() {
        return otpMaxLength;
    }

    public void setOtpMaxLength(Object otpMaxLength) {
        this.otpMaxLength = otpMaxLength;
    }

    public String getOtpFormat() {
        return otpFormat;
    }

    public void setOtpFormat(String otpFormat) {
        this.otpFormat = otpFormat;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
