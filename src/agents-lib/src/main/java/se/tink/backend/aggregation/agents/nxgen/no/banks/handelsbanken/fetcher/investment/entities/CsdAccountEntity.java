package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CsdAccountEntity {
    private String bankAccountForInterest;
    private String address1;
    private Integer coreToVipId;
    private String citizenship;
    private String bankAccountForCharges;
    private String csdAccountNumber;
    private String type;
    private String city;
    private String bankAccountForDividends;
    private String name;
    private String zipCode;
    private Integer changeNotificationMailDays;
    private String firstName;
    private String note;
    private String bankAccountForRealizations;
    private String lastName;
    private String investorId;
    private String whenCreated;
    private String sendPaymentNotificationMode;
    private String status;
    private String mainLocalParticipantCode;
    private String uri;
    private String country;
    private String changeNotificationMode;
    private String language;
    private String ask;

    public String getBankAccountForInterest() {
        return bankAccountForInterest;
    }

    public String getAddress1() {
        return address1;
    }

    public Integer getCoreToVipId() {
        return coreToVipId;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public String getBankAccountForCharges() {
        return bankAccountForCharges;
    }

    public String getCsdAccountNumber() {
        return csdAccountNumber;
    }

    public String getType() {
        return type;
    }

    public String getCity() {
        return city;
    }

    public String getBankAccountForDividends() {
        return bankAccountForDividends;
    }

    public String getName() {
        return name;
    }

    public String getZipCode() {
        return zipCode;
    }

    public Integer getChangeNotificationMailDays() {
        return changeNotificationMailDays;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getNote() {
        return note;
    }

    public String getBankAccountForRealizations() {
        return bankAccountForRealizations;
    }

    public String getLastName() {
        return lastName;
    }

    public String getInvestorId() {
        return investorId;
    }

    public String getWhenCreated() {
        return whenCreated;
    }

    public String getSendPaymentNotificationMode() {
        return sendPaymentNotificationMode;
    }

    public String getStatus() {
        return status;
    }

    public String getMainLocalParticipantCode() {
        return mainLocalParticipantCode;
    }

    public String getUri() {
        return uri;
    }

    public String getCountry() {
        return country;
    }

    public String getChangeNotificationMode() {
        return changeNotificationMode;
    }

    public String getLanguage() {
        return language;
    }

    public String getAsk() {
        return ask;
    }
}
