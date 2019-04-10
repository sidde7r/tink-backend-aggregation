package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CsdAccountRightsEntity {
    private String ownerId;
    private String address1;
    private Integer coreToVipId;
    private String address2;
    private String notificationOfChangeMessage;
    private String csdAccountNumber;
    private String type;
    private String paymentMessage;
    private String rightDate;
    private String sequenceNumber;
    private String city;
    private String redemptionMessage;
    private String rightBlock;
    private String interestMessage;
    private String zipCode;
    private String firstName;
    private String rightType;
    private String coAddress;
    private String lastName;
    private String text;
    private String currencyCode;

    @JsonProperty("class")
    private String rightsClass;

    private String country;
    private long rightAmount;
    private String pledgeSequenceNumber;
    private String languageCode;
    private String dividendMessage;

    public String getOwnerId() {
        return ownerId;
    }

    public String getAddress1() {
        return address1;
    }

    public Integer getCoreToVipId() {
        return coreToVipId;
    }

    public String getAddress2() {
        return address2;
    }

    public String getNotificationOfChangeMessage() {
        return notificationOfChangeMessage;
    }

    public String getCsdAccountNumber() {
        return csdAccountNumber;
    }

    public String getType() {
        return type;
    }

    public String getPaymentMessage() {
        return paymentMessage;
    }

    public String getRightDate() {
        return rightDate;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public String getCity() {
        return city;
    }

    public String getRedemptionMessage() {
        return redemptionMessage;
    }

    public String getRightBlock() {
        return rightBlock;
    }

    public String getInterestMessage() {
        return interestMessage;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getRightType() {
        return rightType;
    }

    public String getCoAddress() {
        return coAddress;
    }

    public String getLastName() {
        return lastName;
    }

    public String getText() {
        return text;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getRightsClass() {
        return rightsClass;
    }

    public String getCountry() {
        return country;
    }

    public long getRightAmount() {
        return rightAmount;
    }

    public String getPledgeSequenceNumber() {
        return pledgeSequenceNumber;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getDividendMessage() {
        return dividendMessage;
    }
}
