package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.ApiRequest;

public class LoanPostRequest implements ApiRequest {

    private Double alimonyAmountPerMonth = null;
    private Double alimonyAmountPerMonthCodebtor = null;
    private String allFirstNames = null;
    private String allFirstNamesCodebtor = null;
    private String apartmentDesignation = null;
    private Boolean approve = null;
    private String bankName = null;
    private String cadastral = null;
    private String cellPhoneNo = null;
    private String cellPhoneNoCodebtor = null;
    private String city = null;
    private String cityCodebtor = null;
    private String co = null;
    private String coCodebtor = null;
    private String codebtorCustomerNumber = null;
    private String country = null;
    private String countryCodebtor = null;
    private Boolean creditCardAndVariousDebts = null;
    private Double creditCardAndVariousDebtsAmount = null;
    private Integer currentNumberOfApplicants = null;
    private String customerNumber = null;
    private Double deferredCapitalGainsTaxAmount = null;
    private String email = null;
    private String emailCodebtor = null;
    private String employer = null;
    private String employerCodebtor = null;
    private String employmentSince = null;
    private String employmentSinceCodebtor = null;
    private String employmentType = null;
    private String employmentTypeCodebtor = null;
    private Double estimatedTotalValueOfSavings = null;
    private String filter = null;
    private String firstName = null;
    private String firstNameCodebtor = null;
    private Boolean hasDeferredCapitalGainsTax = null;
    private Boolean hasSuretyForSomeonesLoans = null;
    private Boolean haveStudentLoan = null;
    private Boolean haveStudentLoanCodebtor = null;
    private Double interestRate = null;
    private Boolean kycAdditionalTaxResidency = null;
    private String kycAltCity = null;
    private String kycAltCo = null;
    private String kycAltCountry = null;
    private String kycAltFirstname = null;
    private String kycAltStreetAddress = null;
    private String kycAltTaxIdentificationNumber = null;
    private Boolean kycAltTaxIdentificationNumberIsMissing = null;
    private String kycAltZipCode = null;
    private Boolean kycAmbassador = null;
    private Boolean kycAssets = null;
    private Boolean kycBoardMemberOfAnInternationalOrganization = null;
    private Boolean kycCard = null;
    private String kycCellPhoneNo = null;
    private Boolean kycChild = null;
    private Boolean kycChildsWifeOrHusbandPartnerOrCohabitant = null;
    private String kycCity = null;
    private String kycCo = null;
    private String kycCountry = null;
    private String kycCountryAlternativelyOrganization = null;
    private Boolean kycCustody = null;
    private String kycCustomerNumber = null;
    private Boolean kycDeputyDirectorOfAnInternOrganization = null;
    private Boolean kycDiplomaticEnvoys = null;
    private Boolean kycDirectorOfAnInternationalOrganization = null;
    private Boolean kycDirectorOfTheCentralBank = null;
    private String kycEmail = null;
    private String kycEmployerCompanyName = null;
    private String kycEmploymentType = null;
    private Boolean kycFinancing = null;
    private String kycFirstname = null;
    private String kycGiveAltPepName = null;
    private String kycGiveCountry = null;
    private String kycGiveReason = null;
    private Boolean kycHighOfficersInTheArmedForces = null;
    private Boolean kycHusbandOrWife = null;
    private Boolean kycIMyselfAmAPoliticallyExposedPerson = null;
    private Boolean kycIncomeGain = null;
    private Boolean kycInvestment = null;
    private Boolean kycIsAltMailingAddress = null;
    private Boolean kycIsPep = null;
    private Boolean kycIsTaxResidentInSweden = null;
    private Boolean kycIsTaxResidentInUsa = null;
    private Boolean kycJudgeInAnotherCourt = null;
    private Boolean kycJudgeOfTheSupremeCourt = null;
    private Boolean kycKnownEmployee = null;
    private Boolean kycLivingEconomy = null;
    private Boolean kycMep = null;
    private String kycMiddlename = null;
    private Boolean kycMinister = null;
    private Boolean kycOfficialAtTheAuditOffice = null;
    private Boolean kycOtherTaxResidencies = null;
    private Boolean kycParent = null;
    private Boolean kycPartner = null;
    private Boolean kycPersonWithHighPostInStateOwnedCompany = null;
    private String kycPhoneNo = null;
    private Boolean kycRegistreredPartner = null;
    private Boolean kycRiskCover = null;
    private Boolean kycSavings = null;
    private String kycSecondname = null;
    private Boolean kycSpecialAdministration = null;
    private String kycStreetAddress = null;
    private String kycTaxIdentificationNumber = null;
    private Boolean kycTheHeadOfStateOrGovernment = null;
    private String kycTitle = null;
    private Boolean kycTransactions = null;
    private Boolean kycViceAndDeputyMinister = null;
    private Boolean kycWealthAdministration = null;
    private String kycWorkPhoneNo = null;
    private String kycZipCode = null;
    private String lastName = null;
    private String lastNameCodebtor = null;
    private String livingSpace = null;
    private Integer loanAmount = null;
    private Integer marketValue = null;
    private Double monthlyFee = null;
    private Double monthlyGrossSalary = null;
    private Double monthlyGrossSalaryCodebtor = null;
    private Integer numberOfAdults = null;
    private Integer numberOfApplicants = null;
    private Integer numberOfChildren = null;
    private Integer numberOfChildrenReceivingAlimony = null;
    private Integer numberOfOtherProperties = null;
    private Integer numberOfRooms = null;
    private String otherInformation = null;
    private List<OtherProperties> otherPropertieses = null;
    private Boolean payAlimony = null;
    private Boolean payAlimonyCodebtor = null;
    private String phoneNo = null;
    private String phoneNoCodebtor = null;
    private Double priceIndication = null;
    private String propertyType = null;
    private Boolean recieveAlimony = null;
    private String streetAddress = null;
    private String streetAddressCodebtor = null;
    private String streetProperty = null;
    private Double studentLoanAmount = null;
    private Double studentLoanAmountCodebtor = null;
    private Double suretyForSomeonesLoansAmount = null;
    private String tenantCorporateNumber = null;
    private String tenantName = null;
    private String workPhoneNo = null;
    private String workPhoneNoCodebtor = null;
    private String zipCode = null;
    private String zipCodeAndCityProperty = null;
    private String zipCodeCodebtor = null;

    public LoanPostRequest() {}

    @JsonIgnore
    @Override
    public String getUriPath() {
        return "/loans";
    }

    @JsonProperty("customer_number")
    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    @JsonProperty("price_indication")
    public Double getPriceIndication() {
        return priceIndication;
    }

    public void setPriceIndication(Double priceIndication) {
        this.priceIndication = priceIndication;
    }

    @JsonProperty("loan_amount")
    public Integer getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Integer loanAmount) {
        this.loanAmount = loanAmount;
    }

    @JsonProperty("interest_rate")
    public Double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(Double interestRate) {
        this.interestRate = interestRate;
    }

    @JsonProperty("bank_name")
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @JsonProperty("current_number_of_applicants")
    public Integer getCurrentNumberOfApplicants() {
        return currentNumberOfApplicants;
    }

    public void setCurrentNumberOfApplicants(Integer currentNumberOfApplicants) {
        this.currentNumberOfApplicants = currentNumberOfApplicants;
    }

    @JsonProperty("number_of_applicants")
    public Integer getNumberOfApplicants() {
        return numberOfApplicants;
    }

    public void setNumberOfApplicants(Integer numberOfApplicants) {
        this.numberOfApplicants = numberOfApplicants;
    }

    @JsonProperty("last_name")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty("filter")
    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @JsonProperty("first_name")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonProperty("all_first_names")
    public String getAllFirstNames() {
        return allFirstNames;
    }

    public void setAllFirstNames(String allFirstNames) {
        this.allFirstNames = allFirstNames;
    }

    @JsonProperty("street_address")
    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    @JsonProperty("co")
    public String getCo() {
        return co;
    }

    public void setCo(String co) {
        this.co = co;
    }

    @JsonProperty("zip_code")
    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @JsonProperty("city")
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty("cell_phone_no")
    public String getCellPhoneNo() {
        return cellPhoneNo;
    }

    public void setCellPhoneNo(String cellPhoneNo) {
        this.cellPhoneNo = cellPhoneNo;
    }

    @JsonProperty("phone_no")
    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    @JsonProperty("work_phone_no")
    public String getWorkPhoneNo() {
        return workPhoneNo;
    }

    public void setWorkPhoneNo(String workPhoneNo) {
        this.workPhoneNo = workPhoneNo;
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("employment_type")
    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    @JsonProperty("employment_since")
    public String getEmploymentSince() {
        return employmentSince;
    }

    public void setEmploymentSince(String employmentSince) {
        this.employmentSince = employmentSince;
    }

    @JsonProperty("employer")
    public String getEmployer() {
        return employer;
    }

    public void setEmployer(String employer) {
        this.employer = employer;
    }

    @JsonProperty("monthly_gross_salary")
    public Double getMonthlyGrossSalary() {
        return monthlyGrossSalary;
    }

    public void setMonthlyGrossSalary(Double monthlyGrossSalary) {
        this.monthlyGrossSalary = monthlyGrossSalary;
    }

    @JsonProperty("pay_alimony")
    public Boolean getPayAlimony() {
        return payAlimony;
    }

    public void setPayAlimony(Boolean payAlimony) {
        this.payAlimony = payAlimony;
    }

    @JsonProperty("alimony_amount_per_month")
    public Double getAlimonyAmountPerMonth() {
        return alimonyAmountPerMonth;
    }

    public void setAlimonyAmountPerMonth(Double alimonyAmountPerMonth) {
        this.alimonyAmountPerMonth = alimonyAmountPerMonth;
    }

    @JsonProperty("recieve_alimony")
    public Boolean getRecieveAlimony() {
        return recieveAlimony;
    }

    public void setRecieveAlimony(Boolean recieveAlimony) {
        this.recieveAlimony = recieveAlimony;
    }

    @JsonProperty("number_of_children_receiving_alimony")
    public Integer getNumberOfChildrenReceivingAlimony() {
        return numberOfChildrenReceivingAlimony;
    }

    public void setNumberOfChildrenReceivingAlimony(Integer numberOfChildrenReceivingAlimony) {
        this.numberOfChildrenReceivingAlimony = numberOfChildrenReceivingAlimony;
    }

    @JsonProperty("number_of_other_properties")
    public Integer getNumberOfOtherProperties() {
        return numberOfOtherProperties;
    }

    public void setNumberOfOtherProperties(Integer numberOfOtherProperties) {
        this.numberOfOtherProperties = numberOfOtherProperties;
    }

    @JsonProperty("have_student_loan")
    public Boolean getHaveStudentLoan() {
        return haveStudentLoan;
    }

    public void setHaveStudentLoan(Boolean haveStudentLoan) {
        this.haveStudentLoan = haveStudentLoan;
    }

    @JsonProperty("student_loan_amount")
    public Double getStudentLoanAmount() {
        return studentLoanAmount;
    }

    public void setStudentLoanAmount(Double studentLoanAmount) {
        this.studentLoanAmount = studentLoanAmount;
    }

    @JsonProperty("last_name_codebtor")
    public String getLastNameCodebtor() {
        return lastNameCodebtor;
    }

    public void setLastNameCodebtor(String lastNameCodebtor) {
        this.lastNameCodebtor = lastNameCodebtor;
    }

    @JsonProperty("first_name_codebtor")
    public String getFirstNameCodebtor() {
        return firstNameCodebtor;
    }

    public void setFirstNameCodebtor(String firstNameCodebtor) {
        this.firstNameCodebtor = firstNameCodebtor;
    }

    @JsonProperty("all_first_names_codebtor")
    public String getAllFirstNamesCodebtor() {
        return allFirstNamesCodebtor;
    }

    public void setAllFirstNamesCodebtor(String allFirstNamesCodebtor) {
        this.allFirstNamesCodebtor = allFirstNamesCodebtor;
    }

    @JsonProperty("codebtor_customer_number")
    public String getCodebtorCustomerNumber() {
        return codebtorCustomerNumber;
    }

    public void setCodebtorCustomerNumber(String codebtorCustomerNumber) {
        this.codebtorCustomerNumber = codebtorCustomerNumber;
    }

    @JsonProperty("street_address_codebtor")
    public String getStreetAddressCodebtor() {
        return streetAddressCodebtor;
    }

    public void setStreetAddressCodebtor(String streetAddressCodebtor) {
        this.streetAddressCodebtor = streetAddressCodebtor;
    }

    @JsonProperty("co_codebtor")
    public String getCoCodebtor() {
        return coCodebtor;
    }

    public void setCoCodebtor(String coCodebtor) {
        this.coCodebtor = coCodebtor;
    }

    @JsonProperty("zip_code_codebtor")
    public String getZipCodeCodebtor() {
        return zipCodeCodebtor;
    }

    public void setZipCodeCodebtor(String zipCodeCodebtor) {
        this.zipCodeCodebtor = zipCodeCodebtor;
    }

    @JsonProperty("city_codebtor")
    public String getCityCodebtor() {
        return cityCodebtor;
    }

    public void setCityCodebtor(String cityCodebtor) {
        this.cityCodebtor = cityCodebtor;
    }

    @JsonProperty("country_codebtor")
    public String getCountryCodebtor() {
        return countryCodebtor;
    }

    public void setCountryCodebtor(String countryCodebtor) {
        this.countryCodebtor = countryCodebtor;
    }

    @JsonProperty("cell_phone_no_codebtor")
    public String getCellPhoneNoCodebtor() {
        return cellPhoneNoCodebtor;
    }

    public void setCellPhoneNoCodebtor(String cellPhoneNoCodebtor) {
        this.cellPhoneNoCodebtor = cellPhoneNoCodebtor;
    }

    @JsonProperty("phone_no_codebtor")
    public String getPhoneNoCodebtor() {
        return phoneNoCodebtor;
    }

    public void setPhoneNoCodebtor(String phoneNoCodebtor) {
        this.phoneNoCodebtor = phoneNoCodebtor;
    }

    @JsonProperty("work_phone_no_codebtor")
    public String getWorkPhoneNoCodebtor() {
        return workPhoneNoCodebtor;
    }

    public void setWorkPhoneNoCodebtor(String workPhoneNoCodebtor) {
        this.workPhoneNoCodebtor = workPhoneNoCodebtor;
    }

    @JsonProperty("email_codebtor")
    public String getEmailCodebtor() {
        return emailCodebtor;
    }

    public void setEmailCodebtor(String emailCodebtor) {
        this.emailCodebtor = emailCodebtor;
    }

    @JsonProperty("employment_type_codebtor")
    public String getEmploymentTypeCodebtor() {
        return employmentTypeCodebtor;
    }

    public void setEmploymentTypeCodebtor(String employmentTypeCodebtor) {
        this.employmentTypeCodebtor = employmentTypeCodebtor;
    }

    @JsonProperty("employment_since_codebtor")
    public String getEmploymentSinceCodebtor() {
        return employmentSinceCodebtor;
    }

    public void setEmploymentSinceCodebtor(String employmentSinceCodebtor) {
        this.employmentSinceCodebtor = employmentSinceCodebtor;
    }

    @JsonProperty("employer_codebtor")
    public String getEmployerCodebtor() {
        return employerCodebtor;
    }

    public void setEmployerCodebtor(String employerCodebtor) {
        this.employerCodebtor = employerCodebtor;
    }

    @JsonProperty("monthly_gross_salary_codebtor")
    public Double getMonthlyGrossSalaryCodebtor() {
        return monthlyGrossSalaryCodebtor;
    }

    public void setMonthlyGrossSalaryCodebtor(Double monthlyGrossSalaryCodebtor) {
        this.monthlyGrossSalaryCodebtor = monthlyGrossSalaryCodebtor;
    }

    @JsonProperty("pay_alimony_codebtor")
    public Boolean getPayAlimonyCodebtor() {
        return payAlimonyCodebtor;
    }

    public void setPayAlimonyCodebtor(Boolean payAlimonyCodebtor) {
        this.payAlimonyCodebtor = payAlimonyCodebtor;
    }

    @JsonProperty("alimony_amount_per_month_codebtor")
    public Double getAlimonyAmountPerMonthCodebtor() {
        return alimonyAmountPerMonthCodebtor;
    }

    public void setAlimonyAmountPerMonthCodebtor(Double alimonyAmountPerMonthCodebtor) {
        this.alimonyAmountPerMonthCodebtor = alimonyAmountPerMonthCodebtor;
    }

    @JsonProperty("have_student_loan_codebtor")
    public Boolean getHaveStudentLoanCodebtor() {
        return haveStudentLoanCodebtor;
    }

    public void setHaveStudentLoanCodebtor(Boolean haveStudentLoanCodebtor) {
        this.haveStudentLoanCodebtor = haveStudentLoanCodebtor;
    }

    @JsonProperty("student_loan_amount_codebtor")
    public Double getStudentLoanAmountCodebtor() {
        return studentLoanAmountCodebtor;
    }

    public void setStudentLoanAmountCodebtor(Double studentLoanAmountCodebtor) {
        this.studentLoanAmountCodebtor = studentLoanAmountCodebtor;
    }

    @JsonProperty("credit_card_and_various_debts")
    public Boolean getCreditCardAndVariousDebts() {
        return creditCardAndVariousDebts;
    }

    public void setCreditCardAndVariousDebts(Boolean creditCardAndVariousDebts) {
        this.creditCardAndVariousDebts = creditCardAndVariousDebts;
    }

    @JsonProperty("credit_card_and_various_debts_amount")
    public Double getCreditCardAndVariousDebtsAmount() {
        return creditCardAndVariousDebtsAmount;
    }

    public void setCreditCardAndVariousDebtsAmount(Double creditCardAndVariousDebtsAmount) {
        this.creditCardAndVariousDebtsAmount = creditCardAndVariousDebtsAmount;
    }

    @JsonProperty("has_surety_for_someones_loans")
    public Boolean getHasSuretyForSomeonesLoans() {
        return hasSuretyForSomeonesLoans;
    }

    public void setHasSuretyForSomeonesLoans(Boolean hasSuretyForSomeonesLoans) {
        this.hasSuretyForSomeonesLoans = hasSuretyForSomeonesLoans;
    }

    @JsonProperty("surety_for_someones_loans_amount")
    public Double getSuretyForSomeonesLoansAmount() {
        return suretyForSomeonesLoansAmount;
    }

    public void setSuretyForSomeonesLoansAmount(Double suretyForSomeonesLoansAmount) {
        this.suretyForSomeonesLoansAmount = suretyForSomeonesLoansAmount;
    }

    @JsonProperty("has_deferred_capital_gains_tax")
    public Boolean getHasDeferredCapitalGainsTax() {
        return hasDeferredCapitalGainsTax;
    }

    public void setHasDeferredCapitalGainsTax(Boolean hasDeferredCapitalGainsTax) {
        this.hasDeferredCapitalGainsTax = hasDeferredCapitalGainsTax;
    }

    @JsonProperty("deferred_capital_gains_tax_amount")
    public Double getDeferredCapitalGainsTaxAmount() {
        return deferredCapitalGainsTaxAmount;
    }

    public void setDeferredCapitalGainsTaxAmount(Double deferredCapitalGainsTaxAmount) {
        this.deferredCapitalGainsTaxAmount = deferredCapitalGainsTaxAmount;
    }

    @JsonProperty("estimated_total_value_of_savings")
    public Double getEstimatedTotalValueOfSavings() {
        return estimatedTotalValueOfSavings;
    }

    public void setEstimatedTotalValueOfSavings(Double estimatedTotalValueOfSavings) {
        this.estimatedTotalValueOfSavings = estimatedTotalValueOfSavings;
    }

    @JsonProperty("number_of_adults")
    public Integer getNumberOfAdults() {
        return numberOfAdults;
    }

    public void setNumberOfAdults(Integer numberOfAdults) {
        this.numberOfAdults = numberOfAdults;
    }

    @JsonProperty("number_of_children")
    public Integer getNumberOfChildren() {
        return numberOfChildren;
    }

    public void setNumberOfChildren(Integer numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }

    @JsonProperty("property_type")
    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    @JsonProperty("street_property")
    public String getStreetProperty() {
        return streetProperty;
    }

    public void setStreetProperty(String streetProperty) {
        this.streetProperty = streetProperty;
    }

    @JsonProperty("zip_code_and_city_property")
    public String getZipCodeAndCityProperty() {
        return zipCodeAndCityProperty;
    }

    public void setZipCodeAndCityProperty(String zipCodeAndCityProperty) {
        this.zipCodeAndCityProperty = zipCodeAndCityProperty;
    }

    @JsonProperty("apartment_designation")
    public String getApartmentDesignation() {
        return apartmentDesignation;
    }

    public void setApartmentDesignation(String apartmentDesignation) {
        this.apartmentDesignation = apartmentDesignation;
    }

    @JsonProperty("cadastral")
    public String getCadastral() {
        return cadastral;
    }

    public void setCadastral(String cadastral) {
        this.cadastral = cadastral;
    }

    @JsonProperty("tenant_name")
    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    @JsonProperty("tenant_corporate_number")
    public String getTenantCorporateNumber() {
        return tenantCorporateNumber;
    }

    public void setTenantCorporateNumber(String tenantCorporateNumber) {
        this.tenantCorporateNumber = tenantCorporateNumber;
    }

    @JsonProperty("living_space")
    public String getLivingSpace() {
        return livingSpace;
    }

    public void setLivingSpace(String livingSpace) {
        this.livingSpace = livingSpace;
    }

    @JsonProperty("number_of_rooms")
    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Integer numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    @JsonProperty("market_value")
    public Integer getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(Integer marketValue) {
        this.marketValue = marketValue;
    }

    @JsonProperty("monthly_fee")
    public Double getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(Double monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    @JsonProperty("other_information")
    public String getOtherInformation() {
        return otherInformation;
    }

    public void setOtherInformation(String otherInformation) {
        this.otherInformation = otherInformation;
    }

    @JsonProperty("approve")
    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }

    @JsonProperty("kyc_secondname")
    public String getKycSecondname() {
        return kycSecondname;
    }

    public void setKycSecondname(String kycSecondname) {
        this.kycSecondname = kycSecondname;
    }

    @JsonProperty("kyc_firstname")
    public String getKycFirstname() {
        return kycFirstname;
    }

    public void setKycFirstname(String kycFirstname) {
        this.kycFirstname = kycFirstname;
    }

    @JsonProperty("kyc_middlename")
    public String getKycMiddlename() {
        return kycMiddlename;
    }

    public void setKycMiddlename(String kycMiddlename) {
        this.kycMiddlename = kycMiddlename;
    }

    @JsonProperty("kyc_customer_number")
    public String getKycCustomerNumber() {
        return kycCustomerNumber;
    }

    public void setKycCustomerNumber(String kycCustomerNumber) {
        this.kycCustomerNumber = kycCustomerNumber;
    }

    @JsonProperty("kyc_street_address")
    public String getKycStreetAddress() {
        return kycStreetAddress;
    }

    public void setKycStreetAddress(String kycStreetAddress) {
        this.kycStreetAddress = kycStreetAddress;
    }

    @JsonProperty("kyc_co")
    public String getKycCo() {
        return kycCo;
    }

    public void setKycCo(String kycCo) {
        this.kycCo = kycCo;
    }

    @JsonProperty("kyc_zip_code")
    public String getKycZipCode() {
        return kycZipCode;
    }

    public void setKycZipCode(String kycZipCode) {
        this.kycZipCode = kycZipCode;
    }

    @JsonProperty("kyc_city")
    public String getKycCity() {
        return kycCity;
    }

    public void setKycCity(String kycCity) {
        this.kycCity = kycCity;
    }

    @JsonProperty("kyc_country")
    public String getKycCountry() {
        return kycCountry;
    }

    public void setKycCountry(String kycCountry) {
        this.kycCountry = kycCountry;
    }

    @JsonProperty("kyc_is_alt_mailing_address")
    public Boolean getKycIsAltMailingAddress() {
        return kycIsAltMailingAddress;
    }

    public void setKycIsAltMailingAddress(Boolean kycIsAltMailingAddress) {
        this.kycIsAltMailingAddress = kycIsAltMailingAddress;
    }

    @JsonProperty("kyc_alt_firstname")
    public String getKycAltFirstname() {
        return kycAltFirstname;
    }

    public void setKycAltFirstname(String kycAltFirstname) {
        this.kycAltFirstname = kycAltFirstname;
    }

    @JsonProperty("kyc_alt_street_address")
    public String getKycAltStreetAddress() {
        return kycAltStreetAddress;
    }

    public void setKycAltStreetAddress(String kycAltStreetAddress) {
        this.kycAltStreetAddress = kycAltStreetAddress;
    }

    @JsonProperty("kyc_alt_co")
    public String getKycAltCo() {
        return kycAltCo;
    }

    public void setKycAltCo(String kycAltCo) {
        this.kycAltCo = kycAltCo;
    }

    @JsonProperty("kyc_alt_zip_code")
    public String getKycAltZipCode() {
        return kycAltZipCode;
    }

    public void setKycAltZipCode(String kycAltZipCode) {
        this.kycAltZipCode = kycAltZipCode;
    }

    @JsonProperty("kyc_alt_city")
    public String getKycAltCity() {
        return kycAltCity;
    }

    public void setKycAltCity(String kycAltCity) {
        this.kycAltCity = kycAltCity;
    }

    @JsonProperty("kyc_alt_country")
    public String getKycAltCountry() {
        return kycAltCountry;
    }

    public void setKycAltCountry(String kycAltCountry) {
        this.kycAltCountry = kycAltCountry;
    }

    @JsonProperty("kyc_cell_phone_no")
    public String getKycCellPhoneNo() {
        return kycCellPhoneNo;
    }

    public void setKycCellPhoneNo(String kycCellPhoneNo) {
        this.kycCellPhoneNo = kycCellPhoneNo;
    }

    @JsonProperty("kyc_phone_no")
    public String getKycPhoneNo() {
        return kycPhoneNo;
    }

    public void setKycPhoneNo(String kycPhoneNo) {
        this.kycPhoneNo = kycPhoneNo;
    }

    @JsonProperty("kyc_work_phone_no")
    public String getKycWorkPhoneNo() {
        return kycWorkPhoneNo;
    }

    public void setKycWorkPhoneNo(String kycWorkPhoneNo) {
        this.kycWorkPhoneNo = kycWorkPhoneNo;
    }

    @JsonProperty("kyc_email")
    public String getKycEmail() {
        return kycEmail;
    }

    public void setKycEmail(String kycEmail) {
        this.kycEmail = kycEmail;
    }

    @JsonProperty("kyc_employment_type")
    public String getKycEmploymentType() {
        return kycEmploymentType;
    }

    public void setKycEmploymentType(String kycEmploymentType) {
        this.kycEmploymentType = kycEmploymentType;
    }

    @JsonProperty("kyc_employer_company_name")
    public String getKycEmployerCompanyName() {
        return kycEmployerCompanyName;
    }

    public void setKycEmployerCompanyName(String kycEmployerCompanyName) {
        this.kycEmployerCompanyName = kycEmployerCompanyName;
    }

    @JsonProperty("kyc_is_tax_resident_in_sweden")
    public Boolean getKycIsTaxResidentInSweden() {
        return kycIsTaxResidentInSweden;
    }

    public void setKycIsTaxResidentInSweden(Boolean kycIsTaxResidentInSweden) {
        this.kycIsTaxResidentInSweden = kycIsTaxResidentInSweden;
    }

    @JsonProperty("kyc_is_tax_resident_in_usa")
    public Boolean getKycIsTaxResidentInUsa() {
        return kycIsTaxResidentInUsa;
    }

    public void setKycIsTaxResidentInUsa(Boolean kycIsTaxResidentInUsa) {
        this.kycIsTaxResidentInUsa = kycIsTaxResidentInUsa;
    }

    @JsonProperty("kyc_give_reason")
    public String getKycGiveReason() {
        return kycGiveReason;
    }

    public void setKycGiveReason(String kycGiveReason) {
        this.kycGiveReason = kycGiveReason;
    }

    @JsonProperty("kyc_tax_identification_number")
    public String getKycTaxIdentificationNumber() {
        return kycTaxIdentificationNumber;
    }

    public void setKycTaxIdentificationNumber(String kycTaxIdentificationNumber) {
        this.kycTaxIdentificationNumber = kycTaxIdentificationNumber;
    }

    @JsonProperty("kyc_additional_tax_residency")
    public Boolean getKycAdditionalTaxResidency() {
        return kycAdditionalTaxResidency;
    }

    public void setKycAdditionalTaxResidency(Boolean kycAdditionalTaxResidency) {
        this.kycAdditionalTaxResidency = kycAdditionalTaxResidency;
    }

    @JsonProperty("kyc_give_country")
    public String getKycGiveCountry() {
        return kycGiveCountry;
    }

    public void setKycGiveCountry(String kycGiveCountry) {
        this.kycGiveCountry = kycGiveCountry;
    }

    @JsonProperty("kyc_alt_tax_identification_number")
    public String getKycAltTaxIdentificationNumber() {
        return kycAltTaxIdentificationNumber;
    }

    public void setKycAltTaxIdentificationNumber(String kycAltTaxIdentificationNumber) {
        this.kycAltTaxIdentificationNumber = kycAltTaxIdentificationNumber;
    }

    @JsonProperty("kyc_alt_tax_identification_number_is_missing")
    public Boolean getKycAltTaxIdentificationNumberIsMissing() {
        return kycAltTaxIdentificationNumberIsMissing;
    }

    public void setKycAltTaxIdentificationNumberIsMissing(
            Boolean kycAltTaxIdentificationNumberIsMissing) {
        this.kycAltTaxIdentificationNumberIsMissing = kycAltTaxIdentificationNumberIsMissing;
    }

    @JsonProperty("kyc_other_tax_residencies")
    public Boolean getKycOtherTaxResidencies() {
        return kycOtherTaxResidencies;
    }

    public void setKycOtherTaxResidencies(Boolean kycOtherTaxResidencies) {
        this.kycOtherTaxResidencies = kycOtherTaxResidencies;
    }

    @JsonProperty("kyc_is_pep")
    public Boolean getKycIsPep() {
        return kycIsPep;
    }

    public void setKycIsPep(Boolean kycIsPep) {
        this.kycIsPep = kycIsPep;
    }

    @JsonProperty("kyc_the_head_of_state_or_government")
    public Boolean getKycTheHeadOfStateOrGovernment() {
        return kycTheHeadOfStateOrGovernment;
    }

    public void setKycTheHeadOfStateOrGovernment(Boolean kycTheHeadOfStateOrGovernment) {
        this.kycTheHeadOfStateOrGovernment = kycTheHeadOfStateOrGovernment;
    }

    @JsonProperty("kyc_minister")
    public Boolean getKycMinister() {
        return kycMinister;
    }

    public void setKycMinister(Boolean kycMinister) {
        this.kycMinister = kycMinister;
    }

    @JsonProperty("kyc_vice_and_deputy_minister")
    public Boolean getKycViceAndDeputyMinister() {
        return kycViceAndDeputyMinister;
    }

    public void setKycViceAndDeputyMinister(Boolean kycViceAndDeputyMinister) {
        this.kycViceAndDeputyMinister = kycViceAndDeputyMinister;
    }

    @JsonProperty("kyc_mep")
    public Boolean getKycMep() {
        return kycMep;
    }

    public void setKycMep(Boolean kycMep) {
        this.kycMep = kycMep;
    }

    @JsonProperty("kyc_judge_of_the_supreme_court")
    public Boolean getKycJudgeOfTheSupremeCourt() {
        return kycJudgeOfTheSupremeCourt;
    }

    public void setKycJudgeOfTheSupremeCourt(Boolean kycJudgeOfTheSupremeCourt) {
        this.kycJudgeOfTheSupremeCourt = kycJudgeOfTheSupremeCourt;
    }

    @JsonProperty("kyc_judge_in_another_court")
    public Boolean getKycJudgeInAnotherCourt() {
        return kycJudgeInAnotherCourt;
    }

    public void setKycJudgeInAnotherCourt(Boolean kycJudgeInAnotherCourt) {
        this.kycJudgeInAnotherCourt = kycJudgeInAnotherCourt;
    }

    @JsonProperty("kyc_official_at_the_audit_office")
    public Boolean getKycOfficialAtTheAuditOffice() {
        return kycOfficialAtTheAuditOffice;
    }

    public void setKycOfficialAtTheAuditOffice(Boolean kycOfficialAtTheAuditOffice) {
        this.kycOfficialAtTheAuditOffice = kycOfficialAtTheAuditOffice;
    }

    @JsonProperty("kyc_director_of_the_central_bank")
    public Boolean getKycDirectorOfTheCentralBank() {
        return kycDirectorOfTheCentralBank;
    }

    public void setKycDirectorOfTheCentralBank(Boolean kycDirectorOfTheCentralBank) {
        this.kycDirectorOfTheCentralBank = kycDirectorOfTheCentralBank;
    }

    @JsonProperty("kyc_ambassador")
    public Boolean getKycAmbassador() {
        return kycAmbassador;
    }

    public void setKycAmbassador(Boolean kycAmbassador) {
        this.kycAmbassador = kycAmbassador;
    }

    @JsonProperty("kyc_diplomatic_envoys")
    public Boolean getKycDiplomaticEnvoys() {
        return kycDiplomaticEnvoys;
    }

    public void setKycDiplomaticEnvoys(Boolean kycDiplomaticEnvoys) {
        this.kycDiplomaticEnvoys = kycDiplomaticEnvoys;
    }

    @JsonProperty("kyc_high_officers_in_the_armed_forces")
    public Boolean getKycHighOfficersInTheArmedForces() {
        return kycHighOfficersInTheArmedForces;
    }

    public void setKycHighOfficersInTheArmedForces(Boolean kycHighOfficersInTheArmedForces) {
        this.kycHighOfficersInTheArmedForces = kycHighOfficersInTheArmedForces;
    }

    @JsonProperty("kyc_person_with_high_post_in_state_owned_company")
    public Boolean getKycPersonWithHighPostInStateOwnedCompany() {
        return kycPersonWithHighPostInStateOwnedCompany;
    }

    public void setKycPersonWithHighPostInStateOwnedCompany(
            Boolean kycPersonWithHighPostInStateOwnedCompany) {
        this.kycPersonWithHighPostInStateOwnedCompany = kycPersonWithHighPostInStateOwnedCompany;
    }

    @JsonProperty("kyc_director_of_an_international_organization")
    public Boolean getKycDirectorOfAnInternationalOrganization() {
        return kycDirectorOfAnInternationalOrganization;
    }

    public void setKycDirectorOfAnInternationalOrganization(
            Boolean kycDirectorOfAnInternationalOrganization) {
        this.kycDirectorOfAnInternationalOrganization = kycDirectorOfAnInternationalOrganization;
    }

    @JsonProperty("kyc_deputy_director_of_an_intern_organization")
    public Boolean getKycDeputyDirectorOfAnInternOrganization() {
        return kycDeputyDirectorOfAnInternOrganization;
    }

    public void setKycDeputyDirectorOfAnInternOrganization(
            Boolean kycDeputyDirectorOfAnInternOrganization) {
        this.kycDeputyDirectorOfAnInternOrganization = kycDeputyDirectorOfAnInternOrganization;
    }

    @JsonProperty("kyc_board_member_of_an_international_organization")
    public Boolean getKycBoardMemberOfAnInternationalOrganization() {
        return kycBoardMemberOfAnInternationalOrganization;
    }

    public void setKycBoardMemberOfAnInternationalOrganization(
            Boolean kycBoardMemberOfAnInternationalOrganization) {
        this.kycBoardMemberOfAnInternationalOrganization =
                kycBoardMemberOfAnInternationalOrganization;
    }

    @JsonProperty("kyc_title")
    public String getKycTitle() {
        return kycTitle;
    }

    public void setKycTitle(String kycTitle) {
        this.kycTitle = kycTitle;
    }

    @JsonProperty("kyc_country_alternatively_organization")
    public String getKycCountryAlternativelyOrganization() {
        return kycCountryAlternativelyOrganization;
    }

    public void setKycCountryAlternativelyOrganization(String kycCountryAlternativelyOrganization) {
        this.kycCountryAlternativelyOrganization = kycCountryAlternativelyOrganization;
    }

    @JsonProperty("kyc_i_myself_am_a_politically_exposed_person")
    public Boolean getKycIMyselfAmAPoliticallyExposedPerson() {
        return kycIMyselfAmAPoliticallyExposedPerson;
    }

    public void setKycIMyselfAmAPoliticallyExposedPerson(
            Boolean kycIMyselfAmAPoliticallyExposedPerson) {
        this.kycIMyselfAmAPoliticallyExposedPerson = kycIMyselfAmAPoliticallyExposedPerson;
    }

    @JsonProperty("kyc_husband_or_wife")
    public Boolean getKycHusbandOrWife() {
        return kycHusbandOrWife;
    }

    public void setKycHusbandOrWife(Boolean kycHusbandOrWife) {
        this.kycHusbandOrWife = kycHusbandOrWife;
    }

    @JsonProperty("kyc_registrered_partner")
    public Boolean getKycRegistreredPartner() {
        return kycRegistreredPartner;
    }

    public void setKycRegistreredPartner(Boolean kycRegistreredPartner) {
        this.kycRegistreredPartner = kycRegistreredPartner;
    }

    @JsonProperty("kyc_partner")
    public Boolean getKycPartner() {
        return kycPartner;
    }

    public void setKycPartner(Boolean kycPartner) {
        this.kycPartner = kycPartner;
    }

    @JsonProperty("kyc_child")
    public Boolean getKycChild() {
        return kycChild;
    }

    public void setKycChild(Boolean kycChild) {
        this.kycChild = kycChild;
    }

    @JsonProperty("kyc_childs_wife_or_husband_partner_or_cohabitant")
    public Boolean getKycChildsWifeOrHusbandPartnerOrCohabitant() {
        return kycChildsWifeOrHusbandPartnerOrCohabitant;
    }

    public void setKycChildsWifeOrHusbandPartnerOrCohabitant(
            Boolean kycChildsWifeOrHusbandPartnerOrCohabitant) {
        this.kycChildsWifeOrHusbandPartnerOrCohabitant = kycChildsWifeOrHusbandPartnerOrCohabitant;
    }

    @JsonProperty("kyc_parent")
    public Boolean getKycParent() {
        return kycParent;
    }

    public void setKycParent(Boolean kycParent) {
        this.kycParent = kycParent;
    }

    @JsonProperty("kyc_known_employee")
    public Boolean getKycKnownEmployee() {
        return kycKnownEmployee;
    }

    public void setKycKnownEmployee(Boolean kycKnownEmployee) {
        this.kycKnownEmployee = kycKnownEmployee;
    }

    @JsonProperty("kyc_give_alt_pep_name")
    public String getKycGiveAltPepName() {
        return kycGiveAltPepName;
    }

    public void setKycGiveAltPepName(String kycGiveAltPepName) {
        this.kycGiveAltPepName = kycGiveAltPepName;
    }

    @JsonProperty("kyc_living_economy")
    public Boolean getKycLivingEconomy() {
        return kycLivingEconomy;
    }

    public void setKycLivingEconomy(Boolean kycLivingEconomy) {
        this.kycLivingEconomy = kycLivingEconomy;
    }

    @JsonProperty("kyc_financing")
    public Boolean getKycFinancing() {
        return kycFinancing;
    }

    public void setKycFinancing(Boolean kycFinancing) {
        this.kycFinancing = kycFinancing;
    }

    @JsonProperty("kyc_card")
    public Boolean getKycCard() {
        return kycCard;
    }

    public void setKycCard(Boolean kycCard) {
        this.kycCard = kycCard;
    }

    @JsonProperty("kyc_savings")
    public Boolean getKycSavings() {
        return kycSavings;
    }

    public void setKycSavings(Boolean kycSavings) {
        this.kycSavings = kycSavings;
    }

    @JsonProperty("kyc_wealth_administration")
    public Boolean getKycWealthAdministration() {
        return kycWealthAdministration;
    }

    public void setKycWealthAdministration(Boolean kycWealthAdministration) {
        this.kycWealthAdministration = kycWealthAdministration;
    }

    @JsonProperty("kyc_special_administration")
    public Boolean getKycSpecialAdministration() {
        return kycSpecialAdministration;
    }

    public void setKycSpecialAdministration(Boolean kycSpecialAdministration) {
        this.kycSpecialAdministration = kycSpecialAdministration;
    }

    @JsonProperty("kyc_custody")
    public Boolean getKycCustody() {
        return kycCustody;
    }

    public void setKycCustody(Boolean kycCustody) {
        this.kycCustody = kycCustody;
    }

    @JsonProperty("kyc_transactions")
    public Boolean getKycTransactions() {
        return kycTransactions;
    }

    public void setKycTransactions(Boolean kycTransactions) {
        this.kycTransactions = kycTransactions;
    }

    @JsonProperty("kyc_investment")
    public Boolean getKycInvestment() {
        return kycInvestment;
    }

    public void setKycInvestment(Boolean kycInvestment) {
        this.kycInvestment = kycInvestment;
    }

    @JsonProperty("kyc_income_gain")
    public Boolean getKycIncomeGain() {
        return kycIncomeGain;
    }

    public void setKycIncomeGain(Boolean kycIncomeGain) {
        this.kycIncomeGain = kycIncomeGain;
    }

    @JsonProperty("kyc_risk_cover")
    public Boolean getKycRiskCover() {
        return kycRiskCover;
    }

    public void setKycRiskCover(Boolean kycRiskCover) {
        this.kycRiskCover = kycRiskCover;
    }

    @JsonProperty("kyc_assets")
    public Boolean getKycAssets() {
        return kycAssets;
    }

    public void setKycAssets(Boolean kycAssets) {
        this.kycAssets = kycAssets;
    }

    @JsonProperty("other_propertieses")
    public List<OtherProperties> getOtherPropertieses() {
        return otherPropertieses;
    }

    public void setOtherPropertieses(List<OtherProperties> otherPropertieses) {
        this.otherPropertieses = otherPropertieses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LoanPostRequest that = (LoanPostRequest) o;

        return Objects.equal(this.alimonyAmountPerMonth, that.alimonyAmountPerMonth)
                && Objects.equal(
                        this.alimonyAmountPerMonthCodebtor, that.alimonyAmountPerMonthCodebtor)
                && Objects.equal(this.allFirstNames, that.allFirstNames)
                && Objects.equal(this.allFirstNamesCodebtor, that.allFirstNamesCodebtor)
                && Objects.equal(this.apartmentDesignation, that.apartmentDesignation)
                && Objects.equal(this.approve, that.approve)
                && Objects.equal(this.bankName, that.bankName)
                && Objects.equal(this.cadastral, that.cadastral)
                && Objects.equal(this.cellPhoneNo, that.cellPhoneNo)
                && Objects.equal(this.cellPhoneNoCodebtor, that.cellPhoneNoCodebtor)
                && Objects.equal(this.city, that.city)
                && Objects.equal(this.cityCodebtor, that.cityCodebtor)
                && Objects.equal(this.co, that.co)
                && Objects.equal(this.coCodebtor, that.coCodebtor)
                && Objects.equal(this.codebtorCustomerNumber, that.codebtorCustomerNumber)
                && Objects.equal(this.country, that.country)
                && Objects.equal(this.countryCodebtor, that.countryCodebtor)
                && Objects.equal(this.creditCardAndVariousDebts, that.creditCardAndVariousDebts)
                && Objects.equal(
                        this.creditCardAndVariousDebtsAmount, that.creditCardAndVariousDebtsAmount)
                && Objects.equal(this.currentNumberOfApplicants, that.currentNumberOfApplicants)
                && Objects.equal(this.customerNumber, that.customerNumber)
                && Objects.equal(
                        this.deferredCapitalGainsTaxAmount, that.deferredCapitalGainsTaxAmount)
                && Objects.equal(this.email, that.email)
                && Objects.equal(this.emailCodebtor, that.emailCodebtor)
                && Objects.equal(this.employer, that.employer)
                && Objects.equal(this.employerCodebtor, that.employerCodebtor)
                && Objects.equal(this.employmentSince, that.employmentSince)
                && Objects.equal(this.employmentSinceCodebtor, that.employmentSinceCodebtor)
                && Objects.equal(this.employmentType, that.employmentType)
                && Objects.equal(this.employmentTypeCodebtor, that.employmentTypeCodebtor)
                && Objects.equal(
                        this.estimatedTotalValueOfSavings, that.estimatedTotalValueOfSavings)
                && Objects.equal(this.filter, that.filter)
                && Objects.equal(this.firstName, that.firstName)
                && Objects.equal(this.firstNameCodebtor, that.firstNameCodebtor)
                && Objects.equal(this.hasDeferredCapitalGainsTax, that.hasDeferredCapitalGainsTax)
                && Objects.equal(this.hasSuretyForSomeonesLoans, that.hasSuretyForSomeonesLoans)
                && Objects.equal(this.haveStudentLoan, that.haveStudentLoan)
                && Objects.equal(this.haveStudentLoanCodebtor, that.haveStudentLoanCodebtor)
                && Objects.equal(this.interestRate, that.interestRate)
                && Objects.equal(this.kycAdditionalTaxResidency, that.kycAdditionalTaxResidency)
                && Objects.equal(this.kycAltCity, that.kycAltCity)
                && Objects.equal(this.kycAltCo, that.kycAltCo)
                && Objects.equal(this.kycAltCountry, that.kycAltCountry)
                && Objects.equal(this.kycAltFirstname, that.kycAltFirstname)
                && Objects.equal(this.kycAltStreetAddress, that.kycAltStreetAddress)
                && Objects.equal(
                        this.kycAltTaxIdentificationNumber, that.kycAltTaxIdentificationNumber)
                && Objects.equal(
                        this.kycAltTaxIdentificationNumberIsMissing,
                        that.kycAltTaxIdentificationNumberIsMissing)
                && Objects.equal(this.kycAltZipCode, that.kycAltZipCode)
                && Objects.equal(this.kycAmbassador, that.kycAmbassador)
                && Objects.equal(this.kycAssets, that.kycAssets)
                && Objects.equal(
                        this.kycBoardMemberOfAnInternationalOrganization,
                        that.kycBoardMemberOfAnInternationalOrganization)
                && Objects.equal(this.kycCard, that.kycCard)
                && Objects.equal(this.kycCellPhoneNo, that.kycCellPhoneNo)
                && Objects.equal(this.kycChild, that.kycChild)
                && Objects.equal(
                        this.kycChildsWifeOrHusbandPartnerOrCohabitant,
                        that.kycChildsWifeOrHusbandPartnerOrCohabitant)
                && Objects.equal(this.kycCity, that.kycCity)
                && Objects.equal(this.kycCo, that.kycCo)
                && Objects.equal(this.kycCountry, that.kycCountry)
                && Objects.equal(
                        this.kycCountryAlternativelyOrganization,
                        that.kycCountryAlternativelyOrganization)
                && Objects.equal(this.kycCustody, that.kycCustody)
                && Objects.equal(this.kycCustomerNumber, that.kycCustomerNumber)
                && Objects.equal(
                        this.kycDeputyDirectorOfAnInternOrganization,
                        that.kycDeputyDirectorOfAnInternOrganization)
                && Objects.equal(this.kycDiplomaticEnvoys, that.kycDiplomaticEnvoys)
                && Objects.equal(
                        this.kycDirectorOfAnInternationalOrganization,
                        that.kycDirectorOfAnInternationalOrganization)
                && Objects.equal(this.kycDirectorOfTheCentralBank, that.kycDirectorOfTheCentralBank)
                && Objects.equal(this.kycEmail, that.kycEmail)
                && Objects.equal(this.kycEmployerCompanyName, that.kycEmployerCompanyName)
                && Objects.equal(this.kycEmploymentType, that.kycEmploymentType)
                && Objects.equal(this.kycFinancing, that.kycFinancing)
                && Objects.equal(this.kycFirstname, that.kycFirstname)
                && Objects.equal(this.kycGiveAltPepName, that.kycGiveAltPepName)
                && Objects.equal(this.kycGiveCountry, that.kycGiveCountry)
                && Objects.equal(this.kycGiveReason, that.kycGiveReason)
                && Objects.equal(
                        this.kycHighOfficersInTheArmedForces, that.kycHighOfficersInTheArmedForces)
                && Objects.equal(this.kycHusbandOrWife, that.kycHusbandOrWife)
                && Objects.equal(
                        this.kycIMyselfAmAPoliticallyExposedPerson,
                        that.kycIMyselfAmAPoliticallyExposedPerson)
                && Objects.equal(this.kycIncomeGain, that.kycIncomeGain)
                && Objects.equal(this.kycInvestment, that.kycInvestment)
                && Objects.equal(this.kycIsAltMailingAddress, that.kycIsAltMailingAddress)
                && Objects.equal(this.kycIsPep, that.kycIsPep)
                && Objects.equal(this.kycIsTaxResidentInSweden, that.kycIsTaxResidentInSweden)
                && Objects.equal(this.kycIsTaxResidentInUsa, that.kycIsTaxResidentInUsa)
                && Objects.equal(this.kycJudgeInAnotherCourt, that.kycJudgeInAnotherCourt)
                && Objects.equal(this.kycJudgeOfTheSupremeCourt, that.kycJudgeOfTheSupremeCourt)
                && Objects.equal(this.kycKnownEmployee, that.kycKnownEmployee)
                && Objects.equal(this.kycLivingEconomy, that.kycLivingEconomy)
                && Objects.equal(this.kycMep, that.kycMep)
                && Objects.equal(this.kycMiddlename, that.kycMiddlename)
                && Objects.equal(this.kycMinister, that.kycMinister)
                && Objects.equal(this.kycOfficialAtTheAuditOffice, that.kycOfficialAtTheAuditOffice)
                && Objects.equal(this.kycOtherTaxResidencies, that.kycOtherTaxResidencies)
                && Objects.equal(this.kycParent, that.kycParent)
                && Objects.equal(this.kycPartner, that.kycPartner)
                && Objects.equal(
                        this.kycPersonWithHighPostInStateOwnedCompany,
                        that.kycPersonWithHighPostInStateOwnedCompany)
                && Objects.equal(this.kycPhoneNo, that.kycPhoneNo)
                && Objects.equal(this.kycRegistreredPartner, that.kycRegistreredPartner)
                && Objects.equal(this.kycRiskCover, that.kycRiskCover)
                && Objects.equal(this.kycSavings, that.kycSavings)
                && Objects.equal(this.kycSecondname, that.kycSecondname)
                && Objects.equal(this.kycSpecialAdministration, that.kycSpecialAdministration)
                && Objects.equal(this.kycStreetAddress, that.kycStreetAddress)
                && Objects.equal(this.kycTaxIdentificationNumber, that.kycTaxIdentificationNumber)
                && Objects.equal(
                        this.kycTheHeadOfStateOrGovernment, that.kycTheHeadOfStateOrGovernment)
                && Objects.equal(this.kycTitle, that.kycTitle)
                && Objects.equal(this.kycTransactions, that.kycTransactions)
                && Objects.equal(this.kycViceAndDeputyMinister, that.kycViceAndDeputyMinister)
                && Objects.equal(this.kycWealthAdministration, that.kycWealthAdministration)
                && Objects.equal(this.kycWorkPhoneNo, that.kycWorkPhoneNo)
                && Objects.equal(this.kycZipCode, that.kycZipCode)
                && Objects.equal(this.lastName, that.lastName)
                && Objects.equal(this.lastNameCodebtor, that.lastNameCodebtor)
                && Objects.equal(this.livingSpace, that.livingSpace)
                && Objects.equal(this.loanAmount, that.loanAmount)
                && Objects.equal(this.marketValue, that.marketValue)
                && Objects.equal(this.monthlyFee, that.monthlyFee)
                && Objects.equal(this.monthlyGrossSalary, that.monthlyGrossSalary)
                && Objects.equal(this.monthlyGrossSalaryCodebtor, that.monthlyGrossSalaryCodebtor)
                && Objects.equal(this.numberOfAdults, that.numberOfAdults)
                && Objects.equal(this.numberOfApplicants, that.numberOfApplicants)
                && Objects.equal(this.numberOfChildren, that.numberOfChildren)
                && Objects.equal(
                        this.numberOfChildrenReceivingAlimony,
                        that.numberOfChildrenReceivingAlimony)
                && Objects.equal(this.numberOfOtherProperties, that.numberOfOtherProperties)
                && Objects.equal(this.numberOfRooms, that.numberOfRooms)
                && Objects.equal(this.otherPropertieses, that.otherPropertieses)
                && Objects.equal(this.otherInformation, that.otherInformation)
                && Objects.equal(this.payAlimony, that.payAlimony)
                && Objects.equal(this.payAlimonyCodebtor, that.payAlimonyCodebtor)
                && Objects.equal(this.phoneNo, that.phoneNo)
                && Objects.equal(this.phoneNoCodebtor, that.phoneNoCodebtor)
                && Objects.equal(this.priceIndication, that.priceIndication)
                && Objects.equal(this.propertyType, that.propertyType)
                && Objects.equal(this.recieveAlimony, that.recieveAlimony)
                && Objects.equal(this.streetAddress, that.streetAddress)
                && Objects.equal(this.streetAddressCodebtor, that.streetAddressCodebtor)
                && Objects.equal(this.streetProperty, that.streetProperty)
                && Objects.equal(this.studentLoanAmount, that.studentLoanAmount)
                && Objects.equal(this.studentLoanAmountCodebtor, that.studentLoanAmountCodebtor)
                && Objects.equal(
                        this.suretyForSomeonesLoansAmount, that.suretyForSomeonesLoansAmount)
                && Objects.equal(this.tenantCorporateNumber, that.tenantCorporateNumber)
                && Objects.equal(this.tenantName, that.tenantName)
                && Objects.equal(this.workPhoneNo, that.workPhoneNo)
                && Objects.equal(this.workPhoneNoCodebtor, that.workPhoneNoCodebtor)
                && Objects.equal(this.zipCode, that.zipCode)
                && Objects.equal(this.zipCodeAndCityProperty, that.zipCodeAndCityProperty)
                && Objects.equal(this.zipCodeCodebtor, that.zipCodeCodebtor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                alimonyAmountPerMonth,
                alimonyAmountPerMonthCodebtor,
                allFirstNames,
                allFirstNamesCodebtor,
                apartmentDesignation,
                approve,
                bankName,
                cadastral,
                cellPhoneNo,
                cellPhoneNoCodebtor,
                city,
                cityCodebtor,
                co,
                coCodebtor,
                codebtorCustomerNumber,
                country,
                countryCodebtor,
                creditCardAndVariousDebts,
                creditCardAndVariousDebtsAmount,
                currentNumberOfApplicants,
                customerNumber,
                deferredCapitalGainsTaxAmount,
                email,
                emailCodebtor,
                employer,
                employerCodebtor,
                employmentSince,
                employmentSinceCodebtor,
                employmentType,
                employmentTypeCodebtor,
                estimatedTotalValueOfSavings,
                filter,
                firstName,
                firstNameCodebtor,
                hasDeferredCapitalGainsTax,
                hasSuretyForSomeonesLoans,
                haveStudentLoan,
                haveStudentLoanCodebtor,
                interestRate,
                kycAdditionalTaxResidency,
                kycAltCity,
                kycAltCo,
                kycAltCountry,
                kycAltFirstname,
                kycAltStreetAddress,
                kycAltTaxIdentificationNumber,
                kycAltTaxIdentificationNumberIsMissing,
                kycAltZipCode,
                kycAmbassador,
                kycAssets,
                kycBoardMemberOfAnInternationalOrganization,
                kycCard,
                kycCellPhoneNo,
                kycChild,
                kycChildsWifeOrHusbandPartnerOrCohabitant,
                kycCity,
                kycCo,
                kycCountry,
                kycCountryAlternativelyOrganization,
                kycCustody,
                kycCustomerNumber,
                kycDeputyDirectorOfAnInternOrganization,
                kycDiplomaticEnvoys,
                kycDirectorOfAnInternationalOrganization,
                kycDirectorOfTheCentralBank,
                kycEmail,
                kycEmployerCompanyName,
                kycEmploymentType,
                kycFinancing,
                kycFirstname,
                kycGiveAltPepName,
                kycGiveCountry,
                kycGiveReason,
                kycHighOfficersInTheArmedForces,
                kycHusbandOrWife,
                kycIMyselfAmAPoliticallyExposedPerson,
                kycIncomeGain,
                kycInvestment,
                kycIsAltMailingAddress,
                kycIsPep,
                kycIsTaxResidentInSweden,
                kycIsTaxResidentInUsa,
                kycJudgeInAnotherCourt,
                kycJudgeOfTheSupremeCourt,
                kycKnownEmployee,
                kycLivingEconomy,
                kycMep,
                kycMiddlename,
                kycMinister,
                kycOfficialAtTheAuditOffice,
                kycOtherTaxResidencies,
                kycParent,
                kycPartner,
                kycPersonWithHighPostInStateOwnedCompany,
                kycPhoneNo,
                kycRegistreredPartner,
                kycRiskCover,
                kycSavings,
                kycSecondname,
                kycSpecialAdministration,
                kycStreetAddress,
                kycTaxIdentificationNumber,
                kycTheHeadOfStateOrGovernment,
                kycTitle,
                kycTransactions,
                kycViceAndDeputyMinister,
                kycWealthAdministration,
                kycWorkPhoneNo,
                kycZipCode,
                lastName,
                lastNameCodebtor,
                livingSpace,
                loanAmount,
                marketValue,
                monthlyFee,
                monthlyGrossSalary,
                monthlyGrossSalaryCodebtor,
                numberOfAdults,
                numberOfApplicants,
                numberOfChildren,
                numberOfChildrenReceivingAlimony,
                numberOfOtherProperties,
                numberOfRooms,
                otherInformation,
                otherPropertieses,
                payAlimony,
                payAlimonyCodebtor,
                phoneNo,
                phoneNoCodebtor,
                priceIndication,
                propertyType,
                recieveAlimony,
                streetAddress,
                streetAddressCodebtor,
                streetProperty,
                studentLoanAmount,
                studentLoanAmountCodebtor,
                suretyForSomeonesLoansAmount,
                tenantCorporateNumber,
                tenantName,
                workPhoneNo,
                workPhoneNoCodebtor,
                zipCode,
                zipCodeAndCityProperty,
                zipCodeCodebtor);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("alimonyAmountPerMonth", alimonyAmountPerMonth)
                .add("alimonyAmountPerMonthCodebtor", alimonyAmountPerMonthCodebtor)
                .add("allFirstNames", allFirstNames)
                .add("allFirstNamesCodebtor", allFirstNamesCodebtor)
                .add("apartmentDesignation", apartmentDesignation)
                .add("approve", approve)
                .add("bankName", bankName)
                .add("cadastral", cadastral)
                .add("cellPhoneNo", cellPhoneNo)
                .add("cellPhoneNoCodebtor", cellPhoneNoCodebtor)
                .add("city", city)
                .add("cityCodebtor", cityCodebtor)
                .add("co", co)
                .add("coCodebtor", coCodebtor)
                .add("codebtorCustomerNumber", codebtorCustomerNumber)
                .add("country", country)
                .add("countryCodebtor", countryCodebtor)
                .add("creditCardAndVariousDebts", creditCardAndVariousDebts)
                .add("creditCardAndVariousDebtsAmount", creditCardAndVariousDebtsAmount)
                .add("currentNumberOfApplicants", currentNumberOfApplicants)
                .add("customerNumber", customerNumber)
                .add("deferredCapitalGainsTaxAmount", deferredCapitalGainsTaxAmount)
                .add("email", email)
                .add("emailCodebtor", emailCodebtor)
                .add("employer", employer)
                .add("employerCodebtor", employerCodebtor)
                .add("employmentSince", employmentSince)
                .add("employmentSinceCodebtor", employmentSinceCodebtor)
                .add("employmentType", employmentType)
                .add("employmentTypeCodebtor", employmentTypeCodebtor)
                .add("estimatedTotalValueOfSavings", estimatedTotalValueOfSavings)
                .add("filter", filter)
                .add("firstName", firstName)
                .add("firstNameCodebtor", firstNameCodebtor)
                .add("hasDeferredCapitalGainsTax", hasDeferredCapitalGainsTax)
                .add("hasSuretyForSomeonesLoans", hasSuretyForSomeonesLoans)
                .add("haveStudentLoan", haveStudentLoan)
                .add("haveStudentLoanCodebtor", haveStudentLoanCodebtor)
                .add("interestRate", interestRate)
                .add("kycAdditionalTaxResidency", kycAdditionalTaxResidency)
                .add("kycAltCity", kycAltCity)
                .add("kycAltCo", kycAltCo)
                .add("kycAltCountry", kycAltCountry)
                .add("kycAltFirstname", kycAltFirstname)
                .add("kycAltStreetAddress", kycAltStreetAddress)
                .add("kycAltTaxIdentificationNumber", kycAltTaxIdentificationNumber)
                .add(
                        "kycAltTaxIdentificationNumberIsMissing",
                        kycAltTaxIdentificationNumberIsMissing)
                .add("kycAltZipCode", kycAltZipCode)
                .add("kycAmbassador", kycAmbassador)
                .add("kycAssets", kycAssets)
                .add(
                        "kycBoardMemberOfAnInternationalOrganization",
                        kycBoardMemberOfAnInternationalOrganization)
                .add("kycCard", kycCard)
                .add("kycCellPhoneNo", kycCellPhoneNo)
                .add("kycChild", kycChild)
                .add(
                        "kycChildsWifeOrHusbandPartnerOrCohabitant",
                        kycChildsWifeOrHusbandPartnerOrCohabitant)
                .add("kycCity", kycCity)
                .add("kycCo", kycCo)
                .add("kycCountry", kycCountry)
                .add("kycCountryAlternativelyOrganization", kycCountryAlternativelyOrganization)
                .add("kycCustody", kycCustody)
                .add("kycCustomerNumber", kycCustomerNumber)
                .add(
                        "kycDeputyDirectorOfAnInternOrganization",
                        kycDeputyDirectorOfAnInternOrganization)
                .add("kycDiplomaticEnvoys", kycDiplomaticEnvoys)
                .add(
                        "kycDirectorOfAnInternationalOrganization",
                        kycDirectorOfAnInternationalOrganization)
                .add("kycDirectorOfTheCentralBank", kycDirectorOfTheCentralBank)
                .add("kycEmail", kycEmail)
                .add("kycEmployerCompanyName", kycEmployerCompanyName)
                .add("kycEmploymentType", kycEmploymentType)
                .add("kycFinancing", kycFinancing)
                .add("kycFirstname", kycFirstname)
                .add("kycGiveAltPepName", kycGiveAltPepName)
                .add("kycGiveCountry", kycGiveCountry)
                .add("kycGiveReason", kycGiveReason)
                .add("kycHighOfficersInTheArmedForces", kycHighOfficersInTheArmedForces)
                .add("kycHusbandOrWife", kycHusbandOrWife)
                .add("kycIMyselfAmAPoliticallyExposedPerson", kycIMyselfAmAPoliticallyExposedPerson)
                .add("kycIncomeGain", kycIncomeGain)
                .add("kycInvestment", kycInvestment)
                .add("kycIsAltMailingAddress", kycIsAltMailingAddress)
                .add("kycIsPep", kycIsPep)
                .add("kycIsTaxResidentInSweden", kycIsTaxResidentInSweden)
                .add("kycIsTaxResidentInUsa", kycIsTaxResidentInUsa)
                .add("kycJudgeInAnotherCourt", kycJudgeInAnotherCourt)
                .add("kycJudgeOfTheSupremeCourt", kycJudgeOfTheSupremeCourt)
                .add("kycKnownEmployee", kycKnownEmployee)
                .add("kycLivingEconomy", kycLivingEconomy)
                .add("kycMep", kycMep)
                .add("kycMiddlename", kycMiddlename)
                .add("kycMinister", kycMinister)
                .add("kycOfficialAtTheAuditOffice", kycOfficialAtTheAuditOffice)
                .add("kycOtherTaxResidencies", kycOtherTaxResidencies)
                .add("kycParent", kycParent)
                .add("kycPartner", kycPartner)
                .add(
                        "kycPersonWithHighPostInStateOwnedCompany",
                        kycPersonWithHighPostInStateOwnedCompany)
                .add("kycPhoneNo", kycPhoneNo)
                .add("kycRegistreredPartner", kycRegistreredPartner)
                .add("kycRiskCover", kycRiskCover)
                .add("kycSavings", kycSavings)
                .add("kycSecondname", kycSecondname)
                .add("kycSpecialAdministration", kycSpecialAdministration)
                .add("kycStreetAddress", kycStreetAddress)
                .add("kycTaxIdentificationNumber", kycTaxIdentificationNumber)
                .add("kycTheHeadOfStateOrGovernment", kycTheHeadOfStateOrGovernment)
                .add("kycTitle", kycTitle)
                .add("kycTransactions", kycTransactions)
                .add("kycViceAndDeputyMinister", kycViceAndDeputyMinister)
                .add("kycWealthAdministration", kycWealthAdministration)
                .add("kycWorkPhoneNo", kycWorkPhoneNo)
                .add("kycZipCode", kycZipCode)
                .add("lastName", lastName)
                .add("lastNameCodebtor", lastNameCodebtor)
                .add("livingSpace", livingSpace)
                .add("loanAmount", loanAmount)
                .add("marketValue", marketValue)
                .add("monthlyFee", monthlyFee)
                .add("monthlyGrossSalary", monthlyGrossSalary)
                .add("monthlyGrossSalaryCodebtor", monthlyGrossSalaryCodebtor)
                .add("numberOfAdults", numberOfAdults)
                .add("numberOfApplicants", numberOfApplicants)
                .add("numberOfChildren", numberOfChildren)
                .add("numberOfChildrenReceivingAlimony", numberOfChildrenReceivingAlimony)
                .add("numberOfOtherProperties", numberOfOtherProperties)
                .add("numberOfRooms", numberOfRooms)
                .add("otherInformation", otherInformation)
                .add("otherPropertieses", otherPropertieses)
                .add("payAlimony", payAlimony)
                .add("payAlimonyCodebtor", payAlimonyCodebtor)
                .add("phoneNo", phoneNo)
                .add("phoneNoCodebtor", phoneNoCodebtor)
                .add("priceIndication", priceIndication)
                .add("propertyType", propertyType)
                .add("recieveAlimony", recieveAlimony)
                .add("streetAddress", streetAddress)
                .add("streetAddressCodebtor", streetAddressCodebtor)
                .add("streetProperty", streetProperty)
                .add("studentLoanAmount", studentLoanAmount)
                .add("studentLoanAmountCodebtor", studentLoanAmountCodebtor)
                .add("suretyForSomeonesLoansAmount", suretyForSomeonesLoansAmount)
                .add("tenantCorporateNumber", tenantCorporateNumber)
                .add("tenantName", tenantName)
                .add("workPhoneNo", workPhoneNo)
                .add("workPhoneNoCodebtor", workPhoneNoCodebtor)
                .add("zipCode", zipCode)
                .add("zipCodeAndCityProperty", zipCodeAndCityProperty)
                .add("zipCodeCodebtor", zipCodeCodebtor)
                .toString();
    }
}
