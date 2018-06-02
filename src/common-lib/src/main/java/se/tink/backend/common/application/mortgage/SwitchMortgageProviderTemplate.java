package se.tink.backend.common.application.mortgage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.Date;
import se.tink.backend.common.application.ApplicationTemplate;
import se.tink.backend.common.application.form.ApplicationFormTemplate;
import se.tink.backend.common.application.form.ApplicationFormTemplateMap;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationFormStatus;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationFormType;
import se.tink.libraries.application.ApplicationType;

public class SwitchMortgageProviderTemplate extends ApplicationTemplate {

    private static final ImmutableMap<String, ApplicationFormTemplate> FORM_TEMPLATES_BY_NAME = ApplicationFormTemplateMap
            .builder()

            .put(ApplicationFormName.MORTGAGE_SECURITY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_SECURITY,
                    ApplicationFieldName.IS_CORRECT_MORTGAGE,
                    ApplicationFieldName.DEFAULT_STREET_ADDRESS,
                    ApplicationFieldName.DEFAULT_POSTAL_CODE,
                    ApplicationFieldName.DEFAULT_TOWN,
                    ApplicationFieldName.DEFAULT_PROPERTY_TYPE,
                    ApplicationFieldName.MORTGAGE_SECURITY_STREET_ADDRESS,
                    ApplicationFieldName.MORTGAGE_SECURITY_POSTAL_CODE,
                    ApplicationFieldName.MORTGAGE_SECURITY_TOWN,
                    ApplicationFieldName.MORTGAGE_SECURITY_PROPERTY_TYPE)

            .put(ApplicationFormName.MORTGAGE_SECURITY_MARKET_VALUE,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PROPERTY_MARKET_VALUE,
                    ApplicationFieldName.ESTIMATED_MARKET_VALUE)

            .put(ApplicationFormName.CURRENT_MORTGAGES,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CURRENT_MORTGAGE,
                    ApplicationFieldName.CURRENT_MORTGAGE)

            .put(ApplicationFormName.HAS_AMORTIZATION_REQUIREMENT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HAS_AMORTIZATION_REQUIREMENT,
                    ApplicationFieldName.HAS_AMORTIZATION_REQUIREMENT)

            .put(ApplicationFormName.HAS_CO_APPLICANT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HAS_CO_APPLICANT,
                    ApplicationFieldName.HAS_CO_APPLICANT)

            .put(ApplicationFormName.MORTGAGE_PRODUCTS_LOADING,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PRODUCTS_LOADING)

            .put(ApplicationFormName.MORTGAGE_PRODUCTS_ALREADY_GOOD,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PRODUCTS_NONE)
                            
            .put(ApplicationFormName.MORTGAGE_PRODUCTS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PRODUCTS,
                    ApplicationFieldName.MORTGAGE_PRODUCT,
                    ApplicationFieldName.MORTGAGE_COMPARISONS)

            .put(ApplicationFormName.MORTGAGE_PRODUCT_DETAILS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PRODUCT_DETAILS)

            .put(ApplicationFormName.SWITCH_MORTGAGE_STATUS_COMPLETE_TINK_PROFILE,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_STATUS_MOVE_MORTGAGE)

            .put(ApplicationFormName.TINK_PROFILE_INTRODUCTION,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PROFILE_INTRODUCTION)

            .put(ApplicationFormName.APPLICANT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_APPLICANT,
                    ApplicationFieldName.NAME,
                    ApplicationFieldName.PERSONAL_NUMBER,
                    ApplicationFieldName.STREET_ADDRESS,
                    ApplicationFieldName.POSTAL_CODE,
                    ApplicationFieldName.TOWN,
                    ApplicationFieldName.EMAIL,
                    ApplicationFieldName.PHONE_NUMBER,
                    ApplicationFieldName.MONTHLY_INCOME)

            .put(ApplicationFormName.SBAB_APPLICANT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_APPLICANT,
                    ApplicationFieldName.FIRST_NAME,
                    ApplicationFieldName.LAST_NAME,
                    ApplicationFieldName.PERSONAL_NUMBER,
                    ApplicationFieldName.STREET_ADDRESS,
                    ApplicationFieldName.POSTAL_CODE,
                    ApplicationFieldName.TOWN,
                    ApplicationFieldName.RESIDENCE_PROPERTY_TYPE,
                    ApplicationFieldName.EMAIL,
                    ApplicationFieldName.PHONE_NUMBER,
                    ApplicationFieldName.MONTHLY_INCOME,
                    ApplicationFieldName.RELATIONSHIP_STATUS)

            .put(ApplicationFormName.EMPLOYMENT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_EMPLOYMENT_TYPE,
                    ApplicationFieldName.EMPLOYMENT_TYPE,
                    ApplicationFieldName.EMPLOYER_NAME,
                    ApplicationFieldName.EMPLOYEE_SINCE,
                    ApplicationFieldName.COMPANY_NAME,
                    ApplicationFieldName.SELF_EMPLOYED_SINCE)

            .put(ApplicationFormName.SBAB_EMPLOYMENT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_EMPLOYMENT_TYPE,
                    ApplicationFieldName.EMPLOYMENT_TYPE,
                    ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME,
                    ApplicationFieldName.PROFESSION,
                    ApplicationFieldName.SBAB_EMPLOYEE_SINCE)

            .put(ApplicationFormName.PAYING_ALIMONY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PAYING_ALIMONY,
                    ApplicationFieldName.PAYING_ALIMONY,
                    ApplicationFieldName.ALIMONY_AMOUNT_PER_MONTH)

            .put(ApplicationFormName.RECEIVING_ALIMONY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PAYING_ALIMONY,
                    ApplicationFieldName.RECEIVING_ALIMONY,
                    ApplicationFieldName.RECEIVING_ALIMONY_COUNT)

            .put(ApplicationFormName.SBAB_PAYING_ALIMONY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PAYING_ALIMONY,
                    ApplicationFieldName.PAYING_ALIMONY,
                    ApplicationFieldName.APPLICANT_NUMBER_OF_CHILDREN_PAYING_ALIMONY_FOR)

            .put(ApplicationFormName.SBAB_CSN_LOAN,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CSN_LOAN,
                    ApplicationFieldName.HAS_CSN_LOAN,
                    ApplicationFieldName.CSN_MONTHLY_COST)

            .put(ApplicationFormName.SEB_CSN_LOAN,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CSN_LOAN,
                    ApplicationFieldName.HAS_CSN_LOAN,
                    ApplicationFieldName.CSN_LOAN_AMOUNT)

            .put(ApplicationFormName.OTHER_LOANS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CURRENT_LOANS,
                    ApplicationFieldName.CURRENT_LOANS,
                    ApplicationFieldName.ADDED_LOANS,
                    ApplicationFieldName.OTHER_LOAN,
                    ApplicationFieldName.LOAN_LENDER,
                    ApplicationFieldName.LOAN_AMOUNT)

            .put(ApplicationFormName.BAILMENT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_BAILMENT,
                    ApplicationFieldName.BAILMENT,
                    ApplicationFieldName.BAILMENT_AMOUNT)

            .put(ApplicationFormName.DEFERRAL_CAPITAL_GAINS_TAX,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_DEFERRAL_CAPITAL_GAINS_TAX,
                    ApplicationFieldName.HAS_DEFERED_CAPITAL_GAINS_TAX,
                    ApplicationFieldName.DEFERRED_AMOUNT)
                    
            .put(ApplicationFormName.SBAB_TAXABLE_IN_SWEDEN,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_TAXABLE_IN_SWEDEN,
                    ApplicationFieldName.TAXABLE_IN_SWEDEN)

            .put(ApplicationFormName.SBAB_TAXABLE_IN_USA,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_TAXABLE_IN_USA,
                    ApplicationFieldName.TAXABLE_IN_USA,
                    ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_USA)

            .put(ApplicationFormName.SBAB_TAXABLE_IN_OTHER_COUNTRY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_TAXABLE_IN_OTHER_COUNTRY,
                    ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY,
                    ApplicationFieldName.TAXABLE_COUNTRY,
                    ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_OTHER_COUNTRY)

            .put(ApplicationFormName.TAXABLE_IN_SWEDEN,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_TAXABLE_IN_SWEDEN,
                    ApplicationFieldName.TAXABLE_IN_SWEDEN)

            .put(ApplicationFormName.TAXABLE_IN_USA,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_TAXABLE_IN_USA,
                    ApplicationFieldName.TAXABLE_IN_USA,
                    ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_USA)

            .put(ApplicationFormName.TAXABLE_IN_OTHER_COUNTRY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_TAXABLE_IN_OTHER_COUNTRY,
                    ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY,
                    ApplicationFieldName.TAXABLE_COUNTRY,
                    ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_OTHER_COUNTRY)

            .put(ApplicationFormName.SALARY_IN_FOREIGN_CURRENCY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_SALARY_IN_FOREIGN_CURRENCY,
                    ApplicationFieldName.SALARY_IN_FOREIGN_CURRENCY)

            .put(ApplicationFormName.SBAB_IS_PEP,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_IS_PEP,
                    ApplicationFieldName.IS_PEP)

            .put(ApplicationFormName.IS_PEP,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_IS_PEP,
                    ApplicationFieldName.IS_PEP)

            .put(ApplicationFormName.ON_OWN_BEHALF,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_ON_OWN_BEHALF,
                    ApplicationFieldName.ON_OWN_BEHALF)

            .put(ApplicationFormName.SBAB_OTHER_PROPERTIES,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_OTHER_PROPERTIES,
                    ApplicationFieldName.SBAB_PROPERTY_TYPE,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_OPERATING_COST,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_ASSESSED_VALUE,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_LOAN_AMOUNT,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_MUNICIPALITY,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_LABEL,
                    ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MONTHLY_FEE,
                    ApplicationFieldName.OTHER_PROPERTY_APARTMENT_LOAN_AMOUNT)

            .put(ApplicationFormName.CO_APPLICANT_INTRODUCTION,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_INTRODUCTION)

            .put(ApplicationFormName.CO_APPLICANT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT,
                    ApplicationFieldName.NAME,
                    ApplicationFieldName.PERSONAL_NUMBER,
                    ApplicationFieldName.PHONE_NUMBER,
                    ApplicationFieldName.EMAIL,
                    ApplicationFieldName.MONTHLY_INCOME,
                    ApplicationFieldName.RELATIONSHIP_STATUS)

            .put(ApplicationFormName.SBAB_CO_APPLICANT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT,
                    ApplicationFieldName.FIRST_NAME,
                    ApplicationFieldName.LAST_NAME,
                    ApplicationFieldName.EMAIL,
                    ApplicationFieldName.PERSONAL_NUMBER,
                    ApplicationFieldName.PHONE_NUMBER,
                    ApplicationFieldName.MONTHLY_INCOME,
                    ApplicationFieldName.RESIDENCE_PROPERTY_TYPE,
                    ApplicationFieldName.RELATIONSHIP_STATUS)

            .put(ApplicationFormName.CO_APPLICANT_ADDRESS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_ADDRESS,
                    ApplicationFieldName.CO_APPLICANT_ADDRESS,
                    ApplicationFieldName.CO_APPLICANT_STREET_ADDRESS,
                    ApplicationFieldName.CO_APPLICANT_POSTAL_CODE,
                    ApplicationFieldName.CO_APPLICANT_TOWN)

            .put(ApplicationFormName.CO_APPLICANT_EMPLOYMENT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_EMPLOYMENT_TYPE,
                    ApplicationFieldName.EMPLOYMENT_TYPE,
                    ApplicationFieldName.EMPLOYER_NAME,
                    ApplicationFieldName.EMPLOYEE_SINCE,
                    ApplicationFieldName.COMPANY_NAME,
                    ApplicationFieldName.SELF_EMPLOYED_SINCE)

            .put(ApplicationFormName.SBAB_CO_APPLICANT_EMPLOYMENT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_EMPLOYMENT_TYPE,
                    ApplicationFieldName.EMPLOYMENT_TYPE,
                    ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME,
                    ApplicationFieldName.PROFESSION,
                    ApplicationFieldName.SBAB_EMPLOYEE_SINCE)

            .put(ApplicationFormName.SBAB_CO_APPLICANT_PAYING_ALIMONY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_PAYING_ALIMONY,
                    ApplicationFieldName.CO_APPLICANT_PAYING_ALIMONY,
                    ApplicationFieldName.CO_APPLICANT_NUMBER_OF_CHILDREN_PAYING_ALIMONY_FOR)

            .put(ApplicationFormName.CO_APPLICANT_PAYING_ALIMONY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_PAYING_ALIMONY,
                    ApplicationFieldName.CO_APPLICANT_PAYING_ALIMONY,
                    ApplicationFieldName.CO_APPLICANT_ALIMONY_AMOUNT)

            .put(ApplicationFormName.CO_APPLICANT_RECEIVING_ALIMONY,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_PAYING_ALIMONY,
                    ApplicationFieldName.CO_APPLICANT_RECEIVING_ALIMONY,
                    ApplicationFieldName.CO_APPLICANT_RECEIVING_ALIMONY_COUNT)

            .put(ApplicationFormName.SBAB_CO_APPLICANT_CSN_LOAN,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_CSN_LOAN,
                    ApplicationFieldName.CO_APPLICANT_CSN_LOAN,
                    ApplicationFieldName.CSN_MONTHLY_COST)

            .put(ApplicationFormName.SEB_CO_APPLICANT_CSN_LOAN,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_CSN_LOAN,
                    ApplicationFieldName.CO_APPLICANT_CSN_LOAN,
                    ApplicationFieldName.CSN_LOAN_AMOUNT)

            .put(ApplicationFormName.CO_APPLICANT_OTHER_LOANS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CURRENT_LOANS,
                    ApplicationFieldName.CURRENT_LOANS,
                    ApplicationFieldName.ADDED_LOANS,
                    ApplicationFieldName.OTHER_LOAN,
                    ApplicationFieldName.LOAN_LENDER,
                    ApplicationFieldName.LOAN_AMOUNT)

            .put(ApplicationFormName.SEB_CO_APPLICANT_OTHER_LOANS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_OTHER_LOANS,
                    ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOANS,
                    ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOAN_AMOUNT)

            .put(ApplicationFormName.SBAB_CO_APPLICANT_OTHER_PROPERTIES,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_OTHER_PROPERTIES,
                    ApplicationFieldName.SBAB_PROPERTY_TYPE,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_OPERATING_COST,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_ASSESSED_VALUE,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_LOAN_AMOUNT,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_MUNICIPALITY,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_LABEL,
                    ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MONTHLY_FEE,
                    ApplicationFieldName.OTHER_PROPERTY_APARTMENT_LOAN_AMOUNT)

            .put(ApplicationFormName.SEB_CO_APPLICANT_BAILMENT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_BAILMENT,
                    ApplicationFieldName.SEB_CO_APPLICANT_BAILMENT,
                    ApplicationFieldName.SEB_CO_APPLICANT_BAILMENT_AMOUNT)

            .put(ApplicationFormName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_DEFERRAL_CAPITAL_GAIN_TAX,
                    ApplicationFieldName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX,
                    ApplicationFieldName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX_AMOUNT)

            .put(ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_INTRODUCTION,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_SECURITY_INTRODUCTION)

            .put(ApplicationFormName.MORTGAGE_SECURITY_APARTMENT_DETAILS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_APARTMENT_DETAILS,
                    ApplicationFieldName.HOUSING_COMMUNITY_NAME,
                    ApplicationFieldName.NUMBER_OF_ROOMS,
                    ApplicationFieldName.LIVING_AREA,
                    ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE,
                    ApplicationFieldName.MONTHLY_AMORTIZATION)

            .put(ApplicationFormName.SBAB_MORTGAGE_SECURITY_APARTMENT_DETAILS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_APARTMENT_DETAILS,
                    ApplicationFieldName.HOUSING_COMMUNITY_NAME,
                    ApplicationFieldName.MUNICIPALITY,
                    ApplicationFieldName.NUMBER_OF_ROOMS,
                    ApplicationFieldName.LIVING_AREA,
                    ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE,
                    ApplicationFieldName.MONTHLY_AMORTIZATION)

            .put(ApplicationFormName.MORTGAGE_SECURITY_HOUSE_INTRODUCTION,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_SECURITY_INTRODUCTION)

            .put(ApplicationFormName.MORTGAGE_SECURITY_HOUSE_DETAILS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HOUSE_DETAILS,
                    ApplicationFieldName.MUNICIPALITY,
                    ApplicationFieldName.CADASTRAL,
                    ApplicationFieldName.MONTHLY_AMORTIZATION)

            .put(ApplicationFormName.SBAB_MORTGAGE_SECURITY_HOUSE_DETAILS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HOUSE_DETAILS,
                    ApplicationFieldName.MUNICIPALITY,
                    ApplicationFieldName.CADASTRAL,
                    ApplicationFieldName.LIVING_AREA,
                    ApplicationFieldName.MONTHLY_OPERATING_COST,
                    ApplicationFieldName.HOUSE_PURCHASE_PRICE,
                    ApplicationFieldName.ASSESSED_VALUE,
                    ApplicationFieldName.MONTHLY_AMORTIZATION)

            .put(ApplicationFormName.SBAB_HOUSEHOLD_CHILDREN,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_CHILDREN,
                    ApplicationFieldName.HOUSEHOLD_CHILDREN,
                    ApplicationFieldName.HOUSEHOLD_NUMBER_OF_CHILDREN_TO_RECEIVE_CHILD_BENEFIT_FOR,
                    ApplicationFieldName.HOUSEHOLD_CHILDREN_ALIMONY_COUNT)

            .put(ApplicationFormName.HOUSEHOLD_CHILDREN,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_CHILDREN,
                    ApplicationFieldName.HOUSEHOLD_CHILDREN,
                    ApplicationFieldName.HOUSEHOLD_CHILDREN_COUNT)

            .put(ApplicationFormName.HOUSEHOLD_ADULTS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_ADULTS,
                    ApplicationFieldName.HOUSEHOLD_ADULTS)

            .put(ApplicationFormName.OTHER_PROPERTIES,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_OTHER_PROPERTIES,
                    ApplicationFieldName.PROPERTY_TYPE,
                    ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MARKET_VALUE,
                    ApplicationFieldName.OTHER_PROPERTY_APARTMENT_LOAN_AMOUNT,
                    ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MONTHLY_FEE,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_MARKET_VALUE,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_ASSESSED_VALUE,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_LOAN_AMOUNT,
                    ApplicationFieldName.OTHER_PROPERTY_HOUSE_GROUND_RENT,
                    ApplicationFieldName.OTHER_PROPERTY_VACATION_HOUSE_MARKET_VALUE,
                    ApplicationFieldName.OTHER_PROPERTY_VACATION_HOUSE_ASSESSED_VALUE,
                    ApplicationFieldName.OTHER_PROPERTY_VACATION_HOUSE_LOAN_AMOUNT,
                    ApplicationFieldName.OTHER_PROPERTY_TENANCY_MONTHLY_RENT)

            .put(ApplicationFormName.OTHER_ASSETS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_OTHER_ASSETS,
                    ApplicationFieldName.CURRENT_ASSETS,
                    ApplicationFieldName.ADDED_ASSETS,
                    ApplicationFieldName.OTHER_ASSET,
                    ApplicationFieldName.ASSET_NAME,
                    ApplicationFieldName.ASSET_VALUE)

            .put(ApplicationFormName.SEB_TRANSFER_SAVINGS,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_TRANSFER_SAVINGS,
                    ApplicationFieldName.SEB_TRANSFER_SAVINGS,
                    ApplicationFieldName.SEB_TRANSFER_ORIGIN)

            .put(ApplicationFormName.SEB_OTHER_SERVICES,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_OTHER_SERVICES,
                    ApplicationFieldName.SEB_OTHER_SERVICES_INTEREST,
                    ApplicationFieldName.SEB_OTHER_SERVICES_OPTIONS)

            .put(ApplicationFormName.DIRECT_DEBIT,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_DIRECT_DEBIT,
                    ApplicationFieldName.DIRECT_DEBIT_ACCOUNT,
                    ApplicationFieldName.DIRECT_DEBIT_CONFIRM)

            .put(ApplicationFormName.LOAN_TERMS_CONFIRMATION,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_LOAN_TERMS_CONFIRMATION)

            .put(ApplicationFormName.SWITCH_MORTGAGE_STATUS_CONFIRMATION,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_STATUS_FINAL)

            .put(ApplicationFormName.SBAB_CONFIRMATION,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CONFIRMATION,
                    ApplicationFieldName.CONFIRM_PUL,
                    ApplicationFieldName.CONFIRM_CREDIT_REPORT,
                    ApplicationFieldName.CONFIRM_POWER_OF_ATTORNEY,
                    ApplicationFieldName.CONFIRM_SALARY_EXTRACT,
                    ApplicationFieldName.CONFIRM_EMPLOYER_CONTACT)

            .put(ApplicationFormName.SEB_CONFIRMATION,
                    ApplicationFormType.SWITCH_MORTGAGE_PROVIDER_CONFIRMATION,
                    ApplicationFieldName.CONFIRM_PUL,
                    ApplicationFieldName.CONFIRM_CREDIT_REPORT,
                    ApplicationFieldName.CONFIRM_POWER_OF_ATTORNEY)

            .put(ApplicationFormName.SIGNATURE,
                    ApplicationFormType.SIGNATURE,
                    ApplicationFieldName.SIGNATURE)

            .build();

    @Override
    public ApplicationType getType() {
        return ApplicationType.SWITCH_MORTGAGE_PROVIDER;
    }

    @Override
    public ImmutableSet<String> getFormNames() {
        return FORM_TEMPLATES_BY_NAME.keySet();
    }

    @Override
    public ImmutableSet<String> getFieldNames(String formName) {
        return FORM_TEMPLATES_BY_NAME.get(formName).fieldNames;
    }
    
    @Override
    public ApplicationFormType getFormType(String formName) {
        return FORM_TEMPLATES_BY_NAME.get(formName).type;
    }

    @Override
    public ApplicationForm createEmptyForm(Application application, String formName) {
        ApplicationFormStatus status = new ApplicationFormStatus();
        status.setUpdated(new Date());
        status.setKey(ApplicationFormStatusKey.CREATED);

        ApplicationForm form = new ApplicationForm();
        form.setName(formName);
        form.setType(FORM_TEMPLATES_BY_NAME.get(formName).type);
        form.setStatus(status);
        form.setApplicationId(application.getId());
        form.setUserId(application.getUserId());

        return form;
    }

    @Override
    public Comparator<ApplicationForm> getFormComparator() {
        return (f1, f2) -> FORM_TEMPLATES_BY_NAME.get(f1.getName()).order.compareTo(FORM_TEMPLATES_BY_NAME.get(f2.getName()).order);
    }
}
