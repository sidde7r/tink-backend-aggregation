package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transferdestination.entities;

import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

@XmlRootElement
public class BeneficiaryEntity implements GeneralAccountEntity {
    private String ibanNumber;
    private String alias;
    private String countryCode;
    private String name;
    private String address;
    private String city;
    private String country;
    private String swift;
    private String flagMobileTrusted;
    private String amount;
    private String commLine1;
    private String commLine2;
    private String commLine3;
    private String commLine4;

    public String getIbanNumber() {
        return ibanNumber;
    }

    public void setIbanNumber(String ibanNumber) {
        this.ibanNumber = ibanNumber;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSwift() {
        return swift;
    }

    public void setSwift(String swift) {
        this.swift = swift;
    }

    public String getFlagMobileTrusted() {
        return flagMobileTrusted;
    }

    public void setFlagMobileTrusted(String flagMobileTrusted) {
        this.flagMobileTrusted = flagMobileTrusted;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCommLine1() {
        return commLine1;
    }

    public void setCommLine1(String commLine1) {
        this.commLine1 = commLine1;
    }

    public String getCommLine2() {
        return commLine2;
    }

    public void setCommLine2(String commLine2) {
        this.commLine2 = commLine2;
    }

    public String getCommLine3() {
        return commLine3;
    }

    public void setCommLine3(String commLine3) {
        this.commLine3 = commLine3;
    }

    public String getCommLine4() {
        return commLine4;
    }

    public void setCommLine4(String commLine4) {
        this.commLine4 = commLine4;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SepaEurIdentifier(ibanNumber);
    }

    @Override
    public String generalGetBank() {
        return "";
    }

    @Override
    public String generalGetName() {
        return name;
    }
}
