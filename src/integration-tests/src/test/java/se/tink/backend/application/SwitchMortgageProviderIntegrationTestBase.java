package se.tink.backend.application;

import com.google.api.client.util.Objects;
import java.util.List;
import java.util.Optional;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.backend.common.application.ApplicationNotValidException;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.exceptions.FeatureFlagNotEnabledException;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.product.ProductInstance;
import se.tink.backend.core.product.ProductTemplate;
import se.tink.backend.core.product.ProductTemplateStatus;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.main.controllers.ApplicationServiceController;
import se.tink.backend.rpc.application.CreateApplicationCommand;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.uuid.UUIDUtils;

public abstract class SwitchMortgageProviderIntegrationTestBase extends AbstractServiceIntegrationTest {

    public static final String SWITCH_MORTGAGE_PROVIDER_SEB_ID = "9056dbfa4cf946bd82ab3c3b9706ed75";
    public static final String SWITCH_MORTGAGE_PROVIDER_SBAB_ID = "387b8b7f51784de3bba857bd2204de70";

    private AuthenticatedUser authenticatedUser;

    protected Application createApplication(ApplicationServiceController applicationServiceController, Optional<String> userAgent)
            throws FeatureFlagNotEnabledException, ApplicationNotValidException {
        CreateApplicationCommand command = new CreateApplicationCommand(authenticatedUser.getUser(), userAgent,
                ApplicationType.SWITCH_MORTGAGE_PROVIDER);
        Application application = applicationServiceController.createApplication(command);

        return application;
    }

    protected AuthenticatedUser getAuthenticatedUserWithoutMortgage() {
        if (authenticatedUser == null) {

            try {
                User user = getTestUser("karl.karlsson@tink.se");

                authenticatedUser = new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return authenticatedUser;
    }

    protected AuthenticatedUser getAuthenticatedUserWithMortgage() {
        if (authenticatedUser == null) {

            try {
                User user = registerTestUserWithDemoCredentialsAndData("201212121212");

                authenticatedUser = new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user);

                createMortgageProducts();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return authenticatedUser;
    }

    public void fillApplicationForSEBForm(ApplicationForm form) {
        fillForm(form, false, false, false, false, true);
    }

    public void fillApplicationForSBABForm(ApplicationForm form) {
        fillForm(form, false, false, false, false, false);
    }

    public void fillApplicationForSBABWithCoApplicantForm(ApplicationForm form) {
        fillForm(form, false, false, false, true, false);
    }

    public void fillApplicationForSEBWithCoApplicantForm(ApplicationForm form) {
        fillForm(form, false, false, false, true, true);
    }

    public void fillApplicationForSEBWithOtherLoans(ApplicationForm form) {
        fillForm(form, true, false, false, false, true);
    }

    public void fillApplicationForSEBWithOtherPropertiesApartment(ApplicationForm form) {
        fillForm(form, false, true, false, false, true);
    }

    public void fillApplicationForSEBWithOtherPropertiesHouse(ApplicationForm form) {
        fillForm(form, false, false, true, false, true);
    }

    public void fillApplicationForSEBWithCoApplicantsOtherLoan(ApplicationForm form) {
        fillForm(form, false, false, false, true, true);
    }

    public void fillApplicationFormWithCoApplicantsOtherPropertiesApartment(ApplicationForm form) {
        fillForm(form, false, false, false, true, true);
    }

    public void fillApplicationFormWithCoApplicantsOtherPropertiesHouse(ApplicationForm form) {
        fillForm(form, false, false, false, true, true);
    }

    public void fillApplicationFormWithDeferralCapitalGainTax(ApplicationForm form) {
        fillForm(form, false, false, false, false, true);
    }

    private void fillForm(
            ApplicationForm form,
            boolean hasOtherLoan,
            boolean hasOtherPropertiesApartment,
            boolean hasOtherPropertiesHouse,
            boolean hasCoApplicant,
            boolean isProviderSEB) {

        if (Objects.equal(form.getName(), ApplicationFormName.MORTGAGE_SECURITY)) {
            fillMortgageSecurityFieldsForApartment(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE)) {
            fillMortgageSecurityMarketValue(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.CURRENT_MORTGAGES)) {
            fillCurrentMortgage(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.HAS_CO_APPLICANT)) {
            fillHasCoApplicant(form, hasCoApplicant);

        } else if (Objects.equal(form.getName(), ApplicationFormName.MORTGAGE_PRODUCTS_LOADING)) {
            // has no fields.

        } else if (Objects.equal(form.getName(), ApplicationFormName.MORTGAGE_PRODUCTS)) {
            fillMortgageToSwitchTo(form, isProviderSEB);

        } else if (Objects.equal(form.getName(), ApplicationFormName.MORTGAGE_PRODUCT_DETAILS)) {
            // has no fields.

        } else if (Objects.equal(form.getName(), ApplicationFormName.SWITCH_MORTGAGE_STATUS_COMPLETE_TINK_PROFILE)) {
            // has no fields.

        } else if (Objects.equal(form.getName(), ApplicationFormName.TINK_PROFILE_INTRODUCTION)) {
            // has no fields.

        } else if (Objects.equal(form.getName(), ApplicationFormName.APPLICANT)) {
            fillApplicantForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_APPLICANT)) {
            fillSbabApplicantForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_CSN_LOAN)) {
            fillCsnLoanForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SEB_CSN_LOAN)) {
            fillCsnLoanForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_CO_APPLICANT_CSN_LOAN)) {
            fillCsnLoanForCoApplicantForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SEB_CO_APPLICANT_CSN_LOAN)) {
            fillCsnLoanForCoApplicantForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OTHER_LOANS)) {
            fillCurrentLoansForm(form, hasOtherLoan); // TODO hasOtherLoan

        } else if (Objects.equal(form.getName(), ApplicationFormName.BAILMENT)) {
            fillBailmentForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.DEFERRAL_CAPITAL_GAINS_TAX)) {
            fillApplicantHasDeferralCapitalGainTax(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.PAYING_ALIMONY)) {
            fillPayingAlimonyForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_INTRODUCTION)) {
            // has no fields.

        } else if (Objects.equal(form.getName(), ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_DETAILS)) {
            fillApartmentDetailsForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_MORTGAGE_SECURITY_APARTMENT_DETAILS)) {
            fillApartmentDetailsForSbabForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.OTHER_PROPERTIES)) { // TODO add more property types
            if (hasOtherPropertiesApartment) {
                fillApplicantsOtherPropertiesApartment(form);
            } else if (hasOtherPropertiesHouse) {
                fillApplicantsOtherPropertiesHouse(form);
            } else {
                fillOtherProperties(form);
            }

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_OTHER_PROPERTIES)) { // TODO add more property types
            fillSbabOtherProperties(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_HOUSEHOLD_CHILDREN)) {
            fillSbabHouseholdChildrenForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.EMPLOYMENT)) {
            fillEmploymentTypeForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_EMPLOYMENT)) {
            fillSbabEmploymentForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.TAXABLE_IN_SWEDEN)) {
            fillTaxableInSwedenForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.TAXABLE_IN_USA)) {
            fillTaxableInUsaForm(form); // TODO add boolean

        } else if (Objects.equal(form.getName(), ApplicationFormName.TAXABLE_IN_OTHER_COUNTRY)) {
            fillTaxableInOtherCountryForm(form, false); // TODO add isTaxableInOtherCountry

        } else if (Objects.equal(form.getName(), ApplicationFormName.IS_PEP)) {
            fillIsPepForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.CO_APPLICANT_INTRODUCTION)) {
            // has no fields.

        } else if (Objects.equal(form.getName(), ApplicationFormName.CO_APPLICANT)) {
            fillCoApplicant(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_CO_APPLICANT)) {
            fillCoApplicantForSbabForm(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.CO_APPLICANT_EMPLOYMENT)) {
            fillCoApplicantEmploymentType(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_CO_APPLICANT_EMPLOYMENT)) {
            fillCoApplicantEmploymentSbab(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.CO_APPLICANT_ADDRESS)) {
            fillCoApplicantAddress(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_CO_APPLICANT_OTHER_PROPERTIES)) {
            fillCoApplicantOtherPropertiesSbab(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.CO_APPLICANT_PAYING_ALIMONY)) {
            fillCoApplicantPayingAlimony(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.HOUSEHOLD_CHILDREN)) {
            fillHouseholdChildren(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.HOUSEHOLD_ADULTS)) {
            fillHouseholdAdults(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SEB_CO_APPLICANT_OTHER_LOANS)) {
            fillHouseholdOtherLoan(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SEB_CO_APPLICANT_BAILMENT)) {
            fillHouseholdBailment(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX)) {
            fillHouseholdDeferralCapitalGainTax(form);

        } else if (Objects.equal(form.getName(), ApplicationFormName.SBAB_CONFIRMATION)) {
            fillConfirmationForm(form);
        } else if (Objects.equal(form.getName(), ApplicationFormName.SEB_CONFIRMATION)) {
            fillConfirmationForm(form);
        }
    }

    private void fillMortgageSecurityFieldsForApartment(ApplicationForm form) {
        populateField(form, ApplicationFieldName.IS_CORRECT_MORTGAGE, ApplicationFieldOptionValues.YES);
    }

    private void fillMortgageSecurityMarketValue(ApplicationForm form) {
        populateField(form, ApplicationFieldName.ESTIMATED_MARKET_VALUE, "5000000");
    }

    private void fillApplicantForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.PERSONAL_NUMBER, "201212121212");
        populateField(form, ApplicationFieldName.NAME, "Karl Karlsson");
        populateField(form, ApplicationFieldName.STREET_ADDRESS, "Kronobergsgatan 43");
        populateField(form, ApplicationFieldName.POSTAL_CODE, "11233");
        populateField(form, ApplicationFieldName.TOWN, "Stockholm");
        populateField(form, ApplicationFieldName.EMAIL, "karl.karlsson@tink.se");
        populateField(form, ApplicationFieldName.PHONE_NUMBER, "0730320320");
        populateField(form, ApplicationFieldName.MONTHLY_INCOME, "65000");
    }

    private void fillSbabApplicantForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.FIRST_NAME, "Karl");
        populateField(form, ApplicationFieldName.LAST_NAME, "Karlsson");
        populateField(form, ApplicationFieldName.STREET_ADDRESS, "Kronobergsgatan 43");
        populateField(form, ApplicationFieldName.POSTAL_CODE, "11233");
        populateField(form, ApplicationFieldName.TOWN, "Stockholm");
        populateField(form, ApplicationFieldName.EMAIL, "karl.karlsson@tink.se");
        populateField(form, ApplicationFieldName.PERSONAL_NUMBER, "201212121212");
        populateField(form, ApplicationFieldName.PHONE_NUMBER, "0730320320");
        populateField(form, ApplicationFieldName.MONTHLY_INCOME, "65000");
        populateField(form, ApplicationFieldName.RESIDENCE_PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT);
        populateField(form, ApplicationFieldName.RELATIONSHIP_STATUS, ApplicationFieldOptionValues.MARRIED);
    }

    private void fillCsnLoanForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.HAS_CSN_LOAN, ApplicationFieldOptionValues.NO);
    }

    private void fillCsnLoanForCoApplicantForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.CO_APPLICANT_CSN_LOAN, ApplicationFieldOptionValues.NO);
    }

    private void fillCurrentLoansForm(ApplicationForm form, boolean hasOtherLoan) { // TODO other loan
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.CURRENT_LOANS);
        if (!field.isPresent()) {
            return;
        }
        List<ApplicationFieldOption> options = field.get().getOptions();
        if (options == null || options.isEmpty()) {
            return;
        }
        String value = options.get(0).getValue();
        populateField(form, ApplicationFieldName.CURRENT_LOANS, "[\"" + value + "\"]");
        populateField(form, ApplicationFieldName.OTHER_LOAN, ApplicationFieldOptionValues.FALSE);
    }

    private void fillBailmentForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.BAILMENT, ApplicationFieldOptionValues.NO);
    }

    private void fillCurrentMortgage(ApplicationForm form) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.CURRENT_MORTGAGE);
        if (!field.isPresent()) {
            return;
        }
        List<ApplicationFieldOption> options = field.get().getOptions();
        if (options == null || options.isEmpty()) {
            return;
        }
        String value = options.get(0).getValue();
        populateField(form, ApplicationFieldName.CURRENT_MORTGAGE, "[\"" + value + "\"]");
    }

    private void fillHasCoApplicant(ApplicationForm form, boolean hasCoApplicant) {
        if (hasCoApplicant) {
            populateField(form, ApplicationFieldName.HAS_CO_APPLICANT, ApplicationFieldOptionValues.YES);
        } else {
            populateField(form, ApplicationFieldName.HAS_CO_APPLICANT, ApplicationFieldOptionValues.NO);
        }
    }

    private void fillMortgageToSwitchTo(ApplicationForm form, boolean isProviderSEB) {
        Optional<ApplicationField> field = form.getField(ApplicationFieldName.MORTGAGE_PRODUCT);

        if (!field.isPresent()) {
            return;
        }

        List<ApplicationFieldOption> options = field.get().getOptions();
        if (options == null || options.isEmpty()) {
            return;
        }

        String value = null;

        for (ApplicationFieldOption option : options) {
            if (isProviderSEB) {
                if (Objects.equal(option.getLabel(), "SEB")) {
                    value = option.getValue();
                    break;
                }
            } else {
                if (Objects.equal(option.getLabel(), "SBAB")) {
                    value = option.getValue();
                    break;
                }
            }
        }

        populateField(form, ApplicationFieldName.MORTGAGE_PRODUCT, value);
    }

    private void fillApartmentDetailsForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.HOUSING_COMMUNITY_NAME, "BRF Baronen");
        populateField(form, ApplicationFieldName.NUMBER_OF_ROOMS, "2");
        populateField(form, ApplicationFieldName.LIVING_AREA, "70");
        populateField(form, ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE, "1300");
    }

    private void fillApartmentDetailsForSbabForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.HOUSING_COMMUNITY_NAME, "BRF Baronen");
        populateField(form, ApplicationFieldName.MUNICIPALITY, "0180"); // Stockholm
        populateField(form, ApplicationFieldName.NUMBER_OF_ROOMS, "2");
        populateField(form, ApplicationFieldName.LIVING_AREA, "70");
        populateField(form, ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE, "1300");
        populateField(form, ApplicationFieldName.MONTHLY_AMORTIZATION, "1940");
    }

    private void fillApplicantsOtherPropertiesApartment(ApplicationForm form) {
        populateField(form, ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT);
        populateField(form, ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MARKET_VALUE, "7000000");
        populateField(form, ApplicationFieldName.OTHER_PROPERTY_APARTMENT_LOAN_AMOUNT, "5000000");
        populateField(form, ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MONTHLY_FEE, "2500");
    }

    private void fillApplicantsOtherPropertiesHouse(ApplicationForm form) {
        populateField(form, ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE);
        populateField(form, ApplicationFieldName.OTHER_PROPERTY_HOUSE_MARKET_VALUE, "Östergarn Tomase 1:12");
        populateField(form, ApplicationFieldName.OTHER_PROPERTY_HOUSE_ASSESSED_VALUE, "1500000");
        populateField(form, ApplicationFieldName.OTHER_PROPERTY_HOUSE_LOAN_AMOUNT, "5000000");
        populateField(form, ApplicationFieldName.OTHER_PROPERTY_HOUSE_GROUND_RENT, "2500");
    }

    private void fillOtherProperties(ApplicationForm form) {
        populateField(form, ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.NO_OTHER_PROPERTIES);
    }

    private void fillSbabOtherProperties(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_PROPERTY_TYPE, ApplicationFieldOptionValues.NO_OTHER_PROPERTIES);
    }

    private void fillSbabHouseholdChildrenForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.HOUSEHOLD_CHILDREN, ApplicationFieldOptionValues.NO);
    }

    private void fillEmploymentTypeForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.EMPLOYMENT_TYPE, ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT);
        populateField(form, ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, "Tink AB");
        populateField(form, ApplicationFieldName.EMPLOYEE_SINCE, "2015-01");
    }

    private void fillSbabEmploymentForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.EMPLOYMENT_TYPE, ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT);
        populateField(form, ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, "Tink AB");
        populateField(form, ApplicationFieldName.PROFESSION, "Butiksägare");
        populateField(form, ApplicationFieldName.SBAB_EMPLOYEE_SINCE, "2013-11");
    }

    private void fillTaxableInSwedenForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.TAXABLE_IN_SWEDEN, ApplicationFieldOptionValues.YES);
    }

    private void fillTaxableInUsaForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.TAXABLE_IN_USA, ApplicationFieldOptionValues.NO);
//        populateField(form, ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_USA, "12234343431233"); TODO
    }

    private void fillTaxableInOtherCountryForm(ApplicationForm form, boolean isTaxableInOtherCountry) {
        populateField(form, ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.NO);
//        if (isTaxableInOtherCountry) {
//            populateField(form, ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.YES);
//            populateField(form, ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_OTHER_COUNTRY, "12234343431233");
//        } else {
//            populateField(form, ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.NO);
//        }
    }

    private void fillIsPepForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.IS_PEP, ApplicationFieldOptionValues.NO);
    }

    private void fillCoApplicant(ApplicationForm form) {
        populateField(form, ApplicationFieldName.NAME, "Maria Mariasson");
        populateField(form, ApplicationFieldName.PHONE_NUMBER, "0730320320");
        populateField(form, ApplicationFieldName.PERSONAL_NUMBER, "20121211-1111");
        populateField(form, ApplicationFieldName.EMAIL, "maria.mariasson@tink.se");
        populateField(form, ApplicationFieldName.MONTHLY_INCOME, "65000");
        populateField(form, ApplicationFieldName.RELATIONSHIP_STATUS, ApplicationFieldOptionValues.MARRIED);
    }

    private void fillCoApplicantForSbabForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.FIRST_NAME, "Maria");
        populateField(form, ApplicationFieldName.LAST_NAME, "Karlsson");
        populateField(form, ApplicationFieldName.EMAIL, "maria.karlsson@tink.se");
        populateField(form, ApplicationFieldName.PERSONAL_NUMBER, "20121211-1111");
        populateField(form, ApplicationFieldName.PHONE_NUMBER, "0730320320");
        populateField(form, ApplicationFieldName.MONTHLY_INCOME, "65000");
        populateField(form, ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT);
        populateField(form, ApplicationFieldName.RELATIONSHIP_STATUS, ApplicationFieldOptionValues.MARRIED);
    }

    private void fillCoApplicantEmploymentType(ApplicationForm form) {
        populateField(form, ApplicationFieldName.EMPLOYMENT_TYPE, ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT);
        populateField(form, ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, "Stockholm AB");
        populateField(form, ApplicationFieldName.EMPLOYEE_SINCE, "2011-07");
    }

    private void fillCoApplicantEmploymentSbab(ApplicationForm form) {
        populateField(form, ApplicationFieldName.EMPLOYMENT_TYPE, ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT);
        populateField(form, ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, "Stockholm AB");
        populateField(form, ApplicationFieldName.PROFESSION, "Kock");
        populateField(form, ApplicationFieldName.SBAB_EMPLOYEE_SINCE, "2011-11");
    }

    private void fillCoApplicantAddress(ApplicationForm form) {
        populateField(form, ApplicationFieldName.CO_APPLICANT_ADDRESS, ApplicationFieldOptionValues.NO);
        populateField(form, ApplicationFieldName.CO_APPLICANT_STREET_ADDRESS, "Gatanvägen 4");
        populateField(form, ApplicationFieldName.CO_APPLICANT_POSTAL_CODE, "11233");
        populateField(form, ApplicationFieldName.CO_APPLICANT_TOWN, "Stockholm");
    }

    private void fillCoApplicantOtherPropertiesSbab(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SBAB_PROPERTY_TYPE, ApplicationFieldOptionValues.NO_OTHER_PROPERTIES);
    }

    private void fillCoApplicantPayingAlimony(ApplicationForm form) {
        populateField(form, ApplicationFieldName.CO_APPLICANT_PAYING_ALIMONY, ApplicationFieldOptionValues.NO);
    }

    private void fillHouseholdChildren(ApplicationForm form) {
        populateField(form, ApplicationFieldName.HOUSEHOLD_CHILDREN, ApplicationFieldOptionValues.NO);
    }

    private void fillHouseholdAdults(ApplicationForm form) {
        populateField(form, ApplicationFieldName.HOUSEHOLD_ADULTS, ApplicationFieldOptionValues.NO);
    }

    private void fillHouseholdOtherLoan(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOANS, ApplicationFieldOptionValues.NO);
    }

    private void fillHouseholdBailment(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SEB_CO_APPLICANT_BAILMENT, ApplicationFieldOptionValues.NO);
    }

    private void fillHouseholdDeferralCapitalGainTax(ApplicationForm form) {
        populateField(form, ApplicationFieldName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX, ApplicationFieldOptionValues.NO);
    }

    private void fillApplicantHasDeferralCapitalGainTax(ApplicationForm form) {
        populateField(form, ApplicationFieldName.HAS_DEFERED_CAPITAL_GAINS_TAX, ApplicationFieldOptionValues.NO);
//        populateField(form, ApplicationFieldName.DEFERRAL_AMOUNT, "100000");
    }

    private void fillPayingAlimonyForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.PAYING_ALIMONY, ApplicationFieldOptionValues.NO);
    }

    private void fillConfirmationForm(ApplicationForm form) {
        populateField(form, ApplicationFieldName.CONFIRM_CREDIT_REPORT, ApplicationFieldOptionValues.TRUE);
        populateField(form, ApplicationFieldName.CONFIRM_EMPLOYER_CONTACT, ApplicationFieldOptionValues.TRUE);
        populateField(form, ApplicationFieldName.CONFIRM_POWER_OF_ATTORNEY, ApplicationFieldOptionValues.TRUE);
        populateField(form, ApplicationFieldName.CONFIRM_PUL, ApplicationFieldOptionValues.TRUE);
        populateField(form, ApplicationFieldName.CONFIRM_SALARY_EXTRACT, ApplicationFieldOptionValues.TRUE);
    }

    private void populateField(ApplicationForm form, String name, String value) {
        Optional<ApplicationField> field = form.getField(name);
        if (field.isPresent()) {
            field.get().setValue(value);
        }
    }

    private void createMortgageProducts() {
        ProductDAO productDAO = serviceContext.getDao(ProductDAO.class);

        ProductTemplate sebTemplate = new ProductTemplate();
        sebTemplate.setId(UUIDUtils.fromTinkUUID(SWITCH_MORTGAGE_PROVIDER_SEB_ID));
        sebTemplate.setProviderName("seb-bankid");
        sebTemplate.setName("SEB");
        sebTemplate.setType(ProductType.MORTGAGE);
        sebTemplate.setStatus(ProductTemplateStatus.ENABLED);
        ProductInstance sebInstance = new ProductInstance();
        sebInstance.setUserId(UUIDUtils.fromTinkUUID(authenticatedUser.getUser().getId()));
        sebInstance.setTemplateId(sebTemplate.getId());
        productDAO.save(sebInstance);
        productDAO.save(sebTemplate);

        ProductTemplate sbabTemplate = new ProductTemplate();
        sbabTemplate.setId(UUIDUtils.fromTinkUUID(SWITCH_MORTGAGE_PROVIDER_SBAB_ID));
        sbabTemplate.setProviderName("sbab-bankid");
        sbabTemplate.setName("SBAB");
        sbabTemplate.setType(ProductType.MORTGAGE);
        sbabTemplate.setStatus(ProductTemplateStatus.ENABLED);
        ProductInstance sbabInstance = new ProductInstance();
        sbabInstance.setUserId(UUIDUtils.fromTinkUUID(authenticatedUser.getUser().getId()));
        sbabInstance.setTemplateId(sbabTemplate.getId());
        productDAO.save(sbabInstance);
        productDAO.save(sbabTemplate);
    }

}
