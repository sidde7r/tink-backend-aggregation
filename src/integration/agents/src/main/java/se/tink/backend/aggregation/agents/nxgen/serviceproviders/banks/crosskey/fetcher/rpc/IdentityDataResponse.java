package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDataResponse extends CrossKeyResponse {
    private Object advisorPhoneNumber;
    private String lastName;
    private String lastLogin;
    private String gender;
    private Object defaultAdvisorImageName;
    private String mobileNumber;
    private String domesticPhoneNumberWork;
    private String postalCode;
    private String emailAddressHome;
    private String locale;
    private String ssn;
    private String customerType;
    private Object advisorImageName;
    private String addressCountryCode;
    private String countryCode;
    private List<Object> serviceCodes;
    private String postOffice;
    private String domesticMobileNumber;
    private String domesticMobileNumberWork;
    private String phoneNumberWork;
    private String address;
    private String domesticPhoneNumber;
    private Object externalId;
    private Object advisorName;
    private String emailAddressWork;
    private String firstName;
    private String phoneNumber;
    private String faxNumber;
    private int bankOffice;
    private String mobileNumberWork;

    public void setAdvisorPhoneNumber(Object advisorPhoneNumber) {
        this.advisorPhoneNumber = advisorPhoneNumber;
    }

    public Object getAdvisorPhoneNumber() {
        return advisorPhoneNumber;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setDefaultAdvisorImageName(Object defaultAdvisorImageName) {
        this.defaultAdvisorImageName = defaultAdvisorImageName;
    }

    public Object getDefaultAdvisorImageName() {
        return defaultAdvisorImageName;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setDomesticPhoneNumberWork(String domesticPhoneNumberWork) {
        this.domesticPhoneNumberWork = domesticPhoneNumberWork;
    }

    public String getDomesticPhoneNumberWork() {
        return domesticPhoneNumberWork;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setEmailAddressHome(String emailAddressHome) {
        this.emailAddressHome = emailAddressHome;
    }

    public String getEmailAddressHome() {
        return emailAddressHome;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLocale() {
        return locale;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getSsn() {
        return ssn;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setAdvisorImageName(Object advisorImageName) {
        this.advisorImageName = advisorImageName;
    }

    public Object getAdvisorImageName() {
        return advisorImageName;
    }

    public void setAddressCountryCode(String addressCountryCode) {
        this.addressCountryCode = addressCountryCode;
    }

    public String getAddressCountryCode() {
        return addressCountryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setServiceCodes(List<Object> serviceCodes) {
        this.serviceCodes = serviceCodes;
    }

    public List<Object> getServiceCodes() {
        return serviceCodes;
    }

    public void setPostOffice(String postOffice) {
        this.postOffice = postOffice;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public void setDomesticMobileNumber(String domesticMobileNumber) {
        this.domesticMobileNumber = domesticMobileNumber;
    }

    public String getDomesticMobileNumber() {
        return domesticMobileNumber;
    }

    public void setDomesticMobileNumberWork(String domesticMobileNumberWork) {
        this.domesticMobileNumberWork = domesticMobileNumberWork;
    }

    public String getDomesticMobileNumberWork() {
        return domesticMobileNumberWork;
    }

    public void setPhoneNumberWork(String phoneNumberWork) {
        this.phoneNumberWork = phoneNumberWork;
    }

    public String getPhoneNumberWork() {
        return phoneNumberWork;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setDomesticPhoneNumber(String domesticPhoneNumber) {
        this.domesticPhoneNumber = domesticPhoneNumber;
    }

    public String getDomesticPhoneNumber() {
        return domesticPhoneNumber;
    }

    public void setExternalId(Object externalId) {
        this.externalId = externalId;
    }

    public Object getExternalId() {
        return externalId;
    }

    public void setAdvisorName(Object advisorName) {
        this.advisorName = advisorName;
    }

    public Object getAdvisorName() {
        return advisorName;
    }

    public void setEmailAddressWork(String emailAddressWork) {
        this.emailAddressWork = emailAddressWork;
    }

    public String getEmailAddressWork() {
        return emailAddressWork;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setBankOffice(int bankOffice) {
        this.bankOffice = bankOffice;
    }

    public int getBankOffice() {
        return bankOffice;
    }

    public void setMobileNumberWork(String mobileNumberWork) {
        this.mobileNumberWork = mobileNumberWork;
    }

    public String getMobileNumberWork() {
        return mobileNumberWork;
    }
}
