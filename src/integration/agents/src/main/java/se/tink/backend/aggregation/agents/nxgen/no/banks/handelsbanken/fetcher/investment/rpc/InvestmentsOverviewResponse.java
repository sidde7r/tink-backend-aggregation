package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities.DisposableAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities.OwnCsdAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentsOverviewResponse {
    private String phone;
    private String telefax;
    private String postZipCode;
    private String postCountry;
    private String address1;
    private String citizenship;
    private String address2;
    private String city;
    private String phonePrivate;
    private String whenChanged;
    private String zipCode;
    private List<Object> ascribedCsdAccounts;
    private String postAddressValidFrom;
    private List<OwnCsdAccountEntity> ownCsdAccounts;
    private String postCity;
    private String firstName;
    private String coAddress;
    private String lastName;
    private String investorId;
    private String postAddress2;
    private String addressValidTo;
    private String postAddress1;
    private String postCoAddress;
    private String uri;
    private String postWhenAddressChanged;
    private String country;
    private String whenAddressChanged;
    private String mobilePhone;
    private String email;
    private String addressValidFrom;
    private DisposableAccountsEntity disposableAccounts;
    private List<Object> disposableCsdAccounts;
    private String postAddressValidTo;

    public String getPhone() {
        return phone;
    }

    public String getTelefax() {
        return telefax;
    }

    public String getPostZipCode() {
        return postZipCode;
    }

    public String getPostCountry() {
        return postCountry;
    }

    public String getAddress1() {
        return address1;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getPhonePrivate() {
        return phonePrivate;
    }

    public String getWhenChanged() {
        return whenChanged;
    }

    public String getZipCode() {
        return zipCode;
    }

    public List<Object> getAscribedCsdAccounts() {
        return ascribedCsdAccounts;
    }

    public String getPostAddressValidFrom() {
        return postAddressValidFrom;
    }

    public List<OwnCsdAccountEntity> getOwnCsdAccounts() {
        return ownCsdAccounts;
    }

    public String getPostCity() {
        return postCity;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getCoAddress() {
        return coAddress;
    }

    public String getLastName() {
        return lastName;
    }

    public String getInvestorId() {
        return investorId;
    }

    public String getPostAddress2() {
        return postAddress2;
    }

    public String getAddressValidTo() {
        return addressValidTo;
    }

    public String getPostAddress1() {
        return postAddress1;
    }

    public String getPostCoAddress() {
        return postCoAddress;
    }

    public String getUri() {
        return uri;
    }

    public String getPostWhenAddressChanged() {
        return postWhenAddressChanged;
    }

    public String getCountry() {
        return country;
    }

    public String getWhenAddressChanged() {
        return whenAddressChanged;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddressValidFrom() {
        return addressValidFrom;
    }

    public DisposableAccountsEntity getDisposableAccounts() {
        return disposableAccounts;
    }

    public List<Object> getDisposableCsdAccounts() {
        return disposableCsdAccounts;
    }

    public String getPostAddressValidTo() {
        return postAddressValidTo;
    }
}
