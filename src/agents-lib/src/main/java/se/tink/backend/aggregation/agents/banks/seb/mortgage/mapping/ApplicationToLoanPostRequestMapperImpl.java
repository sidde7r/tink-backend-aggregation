package se.tink.backend.aggregation.agents.banks.seb.mortgage.mapping;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.EmploymentType;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.KycEmploymentType;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostRequest;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.OtherProperties;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.PropertyType;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.utils.ApplicationUtils;

public class ApplicationToLoanPostRequestMapperImpl implements ApplicationToLoanPostRequestMapper {
    private final AggregationLogger log;

    @Inject
    public ApplicationToLoanPostRequestMapperImpl(AggregationLogger log) {
        this.log = log;
    }

    @Override
    public LoanPostRequest toLoanRequest(GenericApplication application) {
        Preconditions.checkArgument(application != null);

        ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName = Multimaps.index(
                application.getFieldGroups(), GenericApplicationFieldGroup::getName);

        LoanPostRequest loanPostRequest = new LoanPostRequest();

        populateProduct(loanPostRequest, fieldGroupByName);
        populateApplicant(loanPostRequest, fieldGroupByName);
        populateCoApplicant(loanPostRequest, fieldGroupByName);
        populateHousehold(loanPostRequest, fieldGroupByName);
        populateMortgage(loanPostRequest, fieldGroupByName);
        populateMortgageSecurity(loanPostRequest, fieldGroupByName);
        
        // Other
        loanPostRequest.setApprove(true); // The user accepts terms and conditions.

        // Output the result for debugging purposes
        log.debug(application, String.format("%s -> %s", GenericApplication.class.getSimpleName(),
                LoanPostRequest.class.getSimpleName()));

        return loanPostRequest;
    }
    
    private void populateProduct(LoanPostRequest request, ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {

        Optional<GenericApplicationFieldGroup> product = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.PRODUCT);

        if (!product.isPresent()) {
            log.warn("Product information is not supplied.");
            return;
        }
        
        request.setFilter(product.get().getField(ApplicationFieldName.FILTER_VERSION));
        // SEB has their interest rate represented as e.g. 3.5% -> 3.5 (not 0.035 as expected)
        Optional<Double> interestRate = product.get().tryGetFieldAsDouble(ApplicationFieldName.INTEREST_RATE);
        if (interestRate.isPresent()) {
            double discount = product.get().tryGetFieldAsDouble(ApplicationFieldName.INTEREST_RATE_DISCOUNT).orElse(0d);
            request.setPriceIndication(Math.round((interestRate.get() - discount) * 10000d) / 100d);
        }

        request.setOtherInformation(Catalog.format("Erbjudandet gäller t.o.m. {0}.",
                product.get().getField(ApplicationFieldName.EXPIRATION_DATE)));
    }
    
    private void populateApplicant(LoanPostRequest request, ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        
        Optional<GenericApplicationFieldGroup> applicant = ApplicationUtils.getApplicant(fieldGroupByName);
        
        if (!applicant.isPresent()) {
            log.warn("Applicant information is not supplied.");
            return;
        }
        
        NameToNames applicantNames = new NameToNames(applicant.get().getField(ApplicationFieldName.NAME));

        // Applicant
        request.setAllFirstNames(applicantNames.getAllFirstNames());
        request.setCustomerNumber(applicant.get().getField(ApplicationFieldName.PERSONAL_NUMBER).replace("-", ""));
        request.setFirstName(applicantNames.getFirstName());
        request.setLastName(applicantNames.getLastName());
        
        // Applicant - Contact information
        request.setCellPhoneNo(applicant.get().getField(ApplicationFieldName.PHONE_NUMBER));
        request.setCity(applicant.get().getField(ApplicationFieldName.TOWN));
        request.setCo(null);
        request.setCountry(applicant.get().getField(ApplicationFieldName.COUNTRY));
        request.setEmail(applicant.get().getField(ApplicationFieldName.EMAIL));
        request.setPhoneNo(applicant.get().getField(ApplicationFieldName.PHONE_NUMBER));
        request.setStreetAddress(applicant.get().getField(ApplicationFieldName.STREET_ADDRESS));
        request.setZipCode(applicant.get().getField(ApplicationFieldName.POSTAL_CODE));
        request.setWorkPhoneNo(null);
        
        // Applicant - Employment
        request.setEmployer(applicant.get().getField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME));
        request.setEmploymentSince(applicant.get().getField(ApplicationFieldName.EMPLOYEE_SINCE));
        request.setEmploymentType(getEmploymentType(applicant.get().getField(ApplicationFieldName.EMPLOYMENT_TYPE)).getKey());
        
        // Applicant - Financial situation - Salary
        request.setMonthlyGrossSalary(applicant.get().getFieldAsDouble(ApplicationFieldName.MONTHLY_INCOME));
        
        // Applicant - Financial situation - Paying alimony
        Double payingAlimonyAmount = applicant.get().getFieldAsDouble(ApplicationFieldName.PAYING_ALIMONY_AMOUNT);
        request.setPayAlimony(payingAlimonyAmount != null && payingAlimonyAmount > 0);
        request.setAlimonyAmountPerMonth(payingAlimonyAmount);
        
        // Applicant - Financial situation - Student loan
        Double studentLoanAmount = applicant.get().getFieldAsDouble(ApplicationFieldName.STUDENT_LOAN_AMOUNT);
        request.setHaveStudentLoan(studentLoanAmount != null && studentLoanAmount > 0);
        request.setStudentLoanAmount(studentLoanAmount);
        
        // Applicant - Know Your Customer
        request.setKycAssets(false); // Are any assets transfered to SEB?
        request.setKycCustomerNumber(applicant.get().getField(ApplicationFieldName.PERSONAL_NUMBER).replace("-", ""));
        request.setKycFirstname(applicantNames.getFirstName());
        request.setKycMiddlename(applicantNames.getMiddleNames().orElse(null));
        request.setKycSecondname(applicantNames.getLastName());
        
        // Applicant - Know Your Customer - Contact information
        request.setKycCellPhoneNo(applicant.get().getField(ApplicationFieldName.PHONE_NUMBER));
        request.setKycCity(applicant.get().getField(ApplicationFieldName.TOWN));
        request.setKycCo(null);
        request.setKycCountry(applicant.get().getField(ApplicationFieldName.COUNTRY));
        request.setKycEmail(applicant.get().getField(ApplicationFieldName.EMAIL));
        request.setKycPhoneNo(applicant.get().getField(ApplicationFieldName.PHONE_NUMBER));
        request.setKycStreetAddress(applicant.get().getField(ApplicationFieldName.STREET_ADDRESS));
        request.setKycWorkPhoneNo(null);
        request.setKycZipCode(applicant.get().getField(ApplicationFieldName.POSTAL_CODE));
        
        // Applicant - Know Your Customer - Contact information - Postal address (if different from registered address)
        request.setKycAltCity(null);
        request.setKycAltCo(null);
        request.setKycAltCountry(null);
        request.setKycAltFirstname(null);
        request.setKycAltStreetAddress(null);
        request.setKycAltZipCode(null);
        request.setKycIsAltMailingAddress(false); // Is it different from registered address?
        
        // Applicant - Know Your Customer - Employment
        request.setKycEmployerCompanyName(applicant.get().getField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME));
        request.setKycEmploymentType(getKycEmploymentType(applicant.get().getField(ApplicationFieldName.EMPLOYMENT_TYPE))
                .getKey());
        
        // Applicant - Know Your Customer - Politically Exposed Person
        request.setKycIsPep(applicant.get().getFieldAsBool(ApplicationFieldName.IS_PEP));
        request.setKycCountryAlternativelyOrganization(null);
        request.setKycTitle(null);
        
        // Applicant - Know Your Customer - Politically Exposed Person - How?
        request.setKycChild(null);
        request.setKycChildsWifeOrHusbandPartnerOrCohabitant(null);
        request.setKycGiveAltPepName(null); // Name of the PEP, if it's not the applicant.
        request.setKycHusbandOrWife(null);
        request.setKycIMyselfAmAPoliticallyExposedPerson(null);
        request.setKycKnownEmployee(null);
        request.setKycParent(null);
        request.setKycPartner(null);
        request.setKycRegistreredPartner(null);
        
        // Applicant - Know Your Customer - Politically Exposed Person - Role
        request.setKycAmbassador(null);
        request.setKycBoardMemberOfAnInternationalOrganization(null);
        request.setKycDeputyDirectorOfAnInternOrganization(null);
        request.setKycDiplomaticEnvoys(null);
        request.setKycDirectorOfAnInternationalOrganization(null);
        request.setKycDirectorOfTheCentralBank(null);
        request.setKycHighOfficersInTheArmedForces(null);
        request.setKycJudgeInAnotherCourt(null);
        request.setKycJudgeOfTheSupremeCourt(null);
        request.setKycMep(null);
        request.setKycMinister(null);
        request.setKycOfficialAtTheAuditOffice(null);
        request.setKycPersonWithHighPostInStateOwnedCompany(null);
        request.setKycViceAndDeputyMinister(null);
        request.setKycTheHeadOfStateOrGovernment(null);
        
        
        // Applicant - Know Your Customer - Residence for tax purposes
        request.setKycIsTaxResidentInSweden(false);
        request.setKycIsTaxResidentInUsa(false);
        request.setKycAdditionalTaxResidency(false);
        request.setKycOtherTaxResidencies(false); // FIXME: This one doesn't make sense; there's only one set of
                                                  // properties to specify any tax residency outside of SE and US.
        
        List<GenericApplicationFieldGroup> taxResidencyFieldGroups = ApplicationUtils.getSubGroups(applicant,
                GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);
        
        for (GenericApplicationFieldGroup group : taxResidencyFieldGroups) {
            if ("SE".equalsIgnoreCase(group.getField(ApplicationFieldName.COUNTRY))) {
                // Applicant - Know Your Customer - Residence for tax purposes - Sweden
                request.setKycIsTaxResidentInSweden(true);        
            } else if ("US".equalsIgnoreCase(group.getField(ApplicationFieldName.COUNTRY))) {
                // Applicant - Know Your Customer - Residence for tax purposes - USA
                request.setKycIsTaxResidentInUsa(true);
                request.setKycGiveReason(null); // FIXME: Not available in the application
                request.setKycTaxIdentificationNumber(group.getField(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER));
            } else {
                if (request.getKycAdditionalTaxResidency()) {
                    request.setKycOtherTaxResidencies(true);                    
                } else {
                    request.setKycAdditionalTaxResidency(true);
                    String tin = group.getField(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER);
                    request.setKycAltTaxIdentificationNumber(tin);
                    request.setKycAltTaxIdentificationNumberIsMissing(Strings.isNullOrEmpty(tin));
                    request.setKycGiveCountry(group.getField(ApplicationFieldName.COUNTRY));
                }
            }
        }
        
        // Applicant - Know Your Customer - Reasons for the bank engagement
        request.setKycCard(false);
        request.setKycCustody(false);
        request.setKycFinancing(true);
        request.setKycIncomeGain(false);
        request.setKycInvestment(false);
        request.setKycLivingEconomy(true);
        request.setKycRiskCover(false);
        request.setKycSavings(false);
        request.setKycSpecialAdministration(false);
        request.setKycTransactions(false);
        request.setKycWealthAdministration(false);
    }
    
    private void populateCoApplicant(LoanPostRequest request, ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        
        Optional<GenericApplicationFieldGroup> coApplicant = ApplicationUtils.getCoApplicant(fieldGroupByName);
        
        if (!coApplicant.isPresent()) {
            // Debug level is intended, since a co-applicant is not required.
            log.debug("No co-applicant information supplied.");
            return;
        }
        
        NameToNames coApplicantNames = new NameToNames(coApplicant.get().getField(ApplicationFieldName.NAME));
        
        // Co-applicant
        request.setAllFirstNamesCodebtor(coApplicantNames.getAllFirstNames());
        request.setCodebtorCustomerNumber(coApplicant.get().getField(ApplicationFieldName.PERSONAL_NUMBER).replace("-", ""));
        request.setFirstNameCodebtor(coApplicantNames.getFirstName());
        request.setLastNameCodebtor(coApplicantNames.getLastName());
        
        // Co-applicant - Contact information
        request.setCellPhoneNoCodebtor(coApplicant.get().getField(ApplicationFieldName.PHONE_NUMBER));
        request.setCityCodebtor(coApplicant.get().getField(ApplicationFieldName.TOWN));
        request.setCoCodebtor(null);
        request.setCountryCodebtor(coApplicant.get().getField(ApplicationFieldName.COUNTRY));
        request.setEmailCodebtor(coApplicant.get().getField(ApplicationFieldName.EMAIL));
        request.setPhoneNoCodebtor(coApplicant.get().getField(ApplicationFieldName.PHONE_NUMBER));
        request.setStreetAddressCodebtor(coApplicant.get().getField(ApplicationFieldName.STREET_ADDRESS));
        request.setZipCodeCodebtor(coApplicant.get().getField(ApplicationFieldName.POSTAL_CODE));
        request.setWorkPhoneNoCodebtor(null);
        
        // Co-applicant - Employment
        request.setEmployerCodebtor(coApplicant.get().getField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME));
        request.setEmploymentSinceCodebtor(coApplicant.get().getField(ApplicationFieldName.EMPLOYEE_SINCE));
        request.setEmploymentTypeCodebtor(getEmploymentType(coApplicant.get().getField(ApplicationFieldName.EMPLOYMENT_TYPE)).getKey());
        
        // Co-applicant - Financial situation - Salary
        request.setMonthlyGrossSalaryCodebtor(coApplicant.get().getFieldAsDouble(ApplicationFieldName.MONTHLY_INCOME));
        
        // Co-applicant - Financial situation - Paying alimony
        Double payingAlimonyAmount = coApplicant.get().getFieldAsDouble(ApplicationFieldName.PAYING_ALIMONY_AMOUNT);
        request.setPayAlimonyCodebtor(payingAlimonyAmount != null && payingAlimonyAmount > 0);
        request.setAlimonyAmountPerMonthCodebtor(payingAlimonyAmount);
        
        // Co-applicant - Financial situation - Student loans
        Double studentLoansAmount = coApplicant.get().getFieldAsDouble(ApplicationFieldName.STUDENT_LOAN_AMOUNT);
        request.setHaveStudentLoanCodebtor(studentLoansAmount != null && studentLoansAmount > 0);
        request.setStudentLoanAmountCodebtor(studentLoansAmount);
    }

    private List<OtherProperties> getApplicantsOtherProperties(
            ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {

        Optional<GenericApplicationFieldGroup> applicant = ApplicationUtils.getApplicant(fieldGroupByName);

        List<GenericApplicationFieldGroup> fieldGroups = ApplicationUtils.getSubGroups(applicant,
                GenericApplicationFieldGroupNames.PROPERTY);

        if (fieldGroups == null) {
            return Lists.newArrayList();
        }

        List<OtherProperties> otherProperties = Lists.newArrayList();

        for (GenericApplicationFieldGroup fieldGroup : fieldGroups) {
            otherProperties.add(getOtherProperties(fieldGroup));
        }

        return otherProperties;
    }
    
    private OtherProperties getOtherProperties(GenericApplicationFieldGroup fieldGroup) {
        
        OtherProperties otherProperties = new OtherProperties();
        
        String propertyType = fieldGroup.getField(ApplicationFieldName.TYPE);
        
        switch (propertyType) {
        case ApplicationFieldOptionValues.APARTMENT: {
            otherProperties.setLoanAmountOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.LOAN_AMOUNT));
            otherProperties.setMarketValueOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.MARKET_VALUE));
            otherProperties.setMonthlyFeeOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.MONTHLY_COST));
            break;
        }
        case ApplicationFieldOptionValues.HOUSE: {
            otherProperties.setAssessmentValueOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.ASSESSED_VALUE));
            otherProperties.setLoanAmountOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.LOAN_AMOUNT));
            otherProperties.setMarketValueOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.MARKET_VALUE));
            otherProperties.setYearlyFeeOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.YEARLY_GROUND_RENT));
            break;
        }
        case ApplicationFieldOptionValues.VACATION_HOUSE: {
            otherProperties.setAssessmentValueOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.ASSESSED_VALUE));
            otherProperties.setLoanAmountOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.LOAN_AMOUNT));
            otherProperties.setMarketValueOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.MARKET_VALUE));
            break;
        }
        case ApplicationFieldOptionValues.TENANCY: {
            otherProperties.setMonthlyFeeOther(fieldGroup.getFieldAsDouble(ApplicationFieldName.MONTHLY_COST));
            break;
        }
        default:
            break;
        }
        
        otherProperties.setPropertyTypeOther(getPropertyType(propertyType).getKey());
        
        return otherProperties;
    }
    
    private void populateHousehold(LoanPostRequest request, ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        
        Optional<GenericApplicationFieldGroup> household = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.HOUSEHOLD);

        if (!household.isPresent()) {
            log.warn("Household information is not supplied.");
            return;
        }

        GenericApplicationFieldGroup group = household.get();

        // Household
        request.setNumberOfAdults(group.getFieldAsInteger(ApplicationFieldName.NUMBER_OF_ADULTS));
        request.setNumberOfChildren(group.getFieldAsInteger(ApplicationFieldName.NUMBER_OF_CHILDREN));

        // Household - Financial situation - Assets
        request.setEstimatedTotalValueOfSavings(null);
        
        // FIXME: Should "other properties" really be in the household group?
        List<OtherProperties> otherProperties = getApplicantsOtherProperties(fieldGroupByName);
        request.setNumberOfOtherProperties(otherProperties.size());
        request.setOtherPropertieses(otherProperties);

        // Household - Financial situation - Alimony
        Integer numberOfChildrenReceivingAlimony = group
                .getFieldAsInteger(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_ALIMONY);
        request.setRecieveAlimony(numberOfChildrenReceivingAlimony != null && numberOfChildrenReceivingAlimony > 0);
        request.setNumberOfChildrenReceivingAlimony(numberOfChildrenReceivingAlimony);

        // Household - Financial situation - Bailment
        Double bailmentAmount = group.getFieldAsDouble(ApplicationFieldName.BAILMENT_AMOUNT);
        request.setHasSuretyForSomeonesLoans(bailmentAmount != null && bailmentAmount > 0);
        request.setSuretyForSomeonesLoansAmount(bailmentAmount);

        // Household - Financial situation - Deferred capital gains tax
        Double deferredCapitalGainsTaxAmount = group
                .getFieldAsDouble(ApplicationFieldName.DEFERRED_CAPITAL_GAINS_TAX_AMOUNT);
        request.setHasDeferredCapitalGainsTax(deferredCapitalGainsTaxAmount != null
                && deferredCapitalGainsTaxAmount > 0);
        request.setDeferredCapitalGainsTaxAmount(deferredCapitalGainsTaxAmount);

        // Household - Financial situation - Loans
        Double otherLoansAmount = group.getFieldAsDouble(ApplicationFieldName.OTHER_LOANS_AMOUNT);
        request.setCreditCardAndVariousDebts(otherLoansAmount != null && otherLoansAmount > 0);
        request.setCreditCardAndVariousDebtsAmount(otherLoansAmount);
    }
    
    private void populateMortgage(LoanPostRequest request, ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        
        Optional<GenericApplicationFieldGroup> mortgage = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.CURRENT_MORTGAGE);

        if (!mortgage.isPresent()) {
            log.warn("Current mortgage information is not available.");
            return;
        }
        
        int numberOfApplicants = ApplicationUtils.getNumberOfApplicants(fieldGroupByName);
        
        GenericApplicationFieldGroup group = mortgage.get();
    
        // Mortgage
        request.setBankName(group.getField(ApplicationFieldName.LENDER));
        request.setCurrentNumberOfApplicants(numberOfApplicants);
        request.setLoanAmount(group.getFieldAsInteger(ApplicationFieldName.AMOUNT));
        request.setNumberOfApplicants(numberOfApplicants);

        // SEB has their interest rate represented as e.g. 3.5% -> 3.5 (not 0.035 as expected)
        Optional<Double> interestRate = group.tryGetFieldAsDouble(ApplicationFieldName.INTEREST_RATE);
        if (interestRate.isPresent()) {
            request.setInterestRate(Math.round(interestRate.get() * 10000d) / 100d);
        }
    }

    private void populateMortgageSecurity(LoanPostRequest request, ListMultimap<String, GenericApplicationFieldGroup> fieldGroupByName) {
        Optional<GenericApplicationFieldGroup> mortgageSecurity = ApplicationUtils.getFirst(fieldGroupByName,
                GenericApplicationFieldGroupNames.MORTGAGE_SECURITY);

        if (!mortgageSecurity.isPresent()) {
            log.warn("Current mortgage security information is not available.");
            return;
        }
        
        GenericApplicationFieldGroup group = mortgageSecurity.get();
        
        // Mortgage security - General
        request.setLivingSpace(group.getField(ApplicationFieldName.LIVING_AREA));
        request.setNumberOfRooms(group.getFieldAsInteger(ApplicationFieldName.NUMBER_OF_ROOMS));
        request.setMarketValue(group.getFieldAsInteger(ApplicationFieldName.ESTIMATED_MARKET_VALUE));
        request.setPropertyType(getPropertyType(group.getField(ApplicationFieldName.PROPERTY_TYPE)).getKey());
        request.setStreetProperty(group.getField(ApplicationFieldName.STREET_ADDRESS));
        request.setZipCodeAndCityProperty(String.format("%s %s",
                group.getField(ApplicationFieldName.POSTAL_CODE),
                group.getField(ApplicationFieldName.TOWN)));

        // Mortgage security - Apartment
        request.setApartmentDesignation(null); // Lägenhetsnummer
        request.setMonthlyFee(group.getFieldAsDouble(ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE));
        request.setTenantCorporateNumber(null); // Bostadsrättsföreningens organisationsnummer 
        request.setTenantName(group.getField(ApplicationFieldName.HOUSING_COMMUNITY_NAME));
        
        // Mortgage security - House
        request.setCadastral(group.getField(ApplicationFieldName.CADASTRAL)); // Fastighetsbeteckning
    }
    
    private static EmploymentType getEmploymentType(String employmentType) {
        if (Strings.isNullOrEmpty(employmentType)) {
            return EmploymentType.OTHER;
        }
        
        switch (employmentType) {
        case ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT:
            return EmploymentType.PERMANENT_EMPLOYMENT;
        case ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT:
            return EmploymentType.TEMPORARY_EMPLOYMENT;
        case ApplicationFieldOptionValues.SELF_EMPLOYED:
            return EmploymentType.SELF_EMPLOYED;
        case ApplicationFieldOptionValues.UNEMPLOYED:
            return EmploymentType.UNEMPLOYED;
        case ApplicationFieldOptionValues.STUDENT_RESEARCHER:
            return EmploymentType.STUDENT;
        case ApplicationFieldOptionValues.SENIOR:
            return EmploymentType.PENSIONER;
        case ApplicationFieldOptionValues.OTHER_OCCUPATION:
        default:
            return EmploymentType.OTHER;
        }
    }

    private static KycEmploymentType getKycEmploymentType(String employmentType) {
        if (Strings.isNullOrEmpty(employmentType)) {
            return KycEmploymentType.OTHER;
        }

        switch (employmentType) {
        case ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT:
        case ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT:
            return KycEmploymentType.EMPLOYED;
        case ApplicationFieldOptionValues.SELF_EMPLOYED:
            return KycEmploymentType.SELF_EMPLOYED;
        case ApplicationFieldOptionValues.UNEMPLOYED:
            return KycEmploymentType.UNEMPLOYED;
        case ApplicationFieldOptionValues.STUDENT_RESEARCHER:
            return KycEmploymentType.STUDENT;
        case ApplicationFieldOptionValues.SENIOR:
            return KycEmploymentType.PENSIONER;
        case ApplicationFieldOptionValues.OTHER_OCCUPATION:
        default:
            return KycEmploymentType.OTHER;
        }
    }

    private static PropertyType getPropertyType(String propertyType) {
        if (Strings.isNullOrEmpty(propertyType)) {
            return PropertyType.OTHERS;
        }

        switch (propertyType) {
        case ApplicationFieldOptionValues.APARTMENT:
            return PropertyType.APARTMENT;
        case ApplicationFieldOptionValues.HOUSE:
            return PropertyType.VILLA;
        case ApplicationFieldOptionValues.VACATION_HOUSE:
            return PropertyType.VACATION_HOUSE;
        case ApplicationFieldOptionValues.TENANCY:
            return PropertyType.TENANCY;
        default:
            return PropertyType.OTHERS;
        }
    }

    public static class NameToNames {
        private final List<String> allNames;

        public NameToNames(String name) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(name));

            List<String> names = Splitter.on(" ").splitToList(name);
            Preconditions.checkArgument(names.size() >= 2);

            this.allNames = names;
        }

        public String getFirstName() {
            return Iterables.getFirst(allNames, null);
        }

        public String getAllFirstNames() {
            return Joiner.on(" ").join(
                    Iterables.limit(allNames, allNames.size() - 1));
        }

        public Optional<String> getMiddleNames() {
            if (allNames.size() <= 2) {
                return Optional.empty();
            }

            return Optional.of(Joiner.on(" ").join(
                    FluentIterable.from(allNames)
                            .skip(1)
                            .limit(allNames.size() - 2)));
        }

        public String getLastName() {
            return Iterables.getLast(allNames);
        }
    }
}
