package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = false)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = FraudNonPaymentContent.class),
                @JsonSubTypes.Type(value = FraudCompanyEngagementContent.class),
                @JsonSubTypes.Type(value = FraudCreditorContent.class),
                @JsonSubTypes.Type(value = FraudInquiryContent.class),
                @JsonSubTypes.Type(value = FraudRealEstateEngagementContent.class),
                @JsonSubTypes.Type(value = FraudTransactionContent.class),
                @JsonSubTypes.Type(value = FraudIdentityContent.class),
                @JsonSubTypes.Type(value = FraudAddressContent.class),
                @JsonSubTypes.Type(value = FraudCreditScoringContent.class),
                @JsonSubTypes.Type(value = FraudIncomeContent.class),
                @JsonSubTypes.Type(value = FraudCompanyContent.class)
        })
public abstract class FraudDetailsContent {

    public static final int CACHE_EXPIRY = 5 * 60; // 5min

    protected String contentId;
    protected FraudDetailsContentType contentType;

    public FraudDetailsContentType getContentType() {
        return contentType;
    }

    public void setContentType(FraudDetailsContentType contentType) {
        this.contentType = contentType;
    }

    @JsonIgnore
    public String getContentId() {
        return generateContentId();
    }

    public abstract String generateContentId();

    public abstract FraudTypes itemType();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FraudDetailsContent that = (FraudDetailsContent) o;

        return generateContentId().equals(that.generateContentId());

    }

    @Override
    public int hashCode() {
        return generateContentId().hashCode();
    }
}
