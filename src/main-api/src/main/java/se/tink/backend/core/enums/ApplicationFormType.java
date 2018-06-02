package se.tink.backend.core.enums;

public enum ApplicationFormType {

    FORM("form"), // Clients should default to this type if type is not known to the client
    SWITCH_MORTGAGE_PROVIDER_APARTMENT_DETAILS("mortgage/apartment-details"),
    SWITCH_MORTGAGE_PROVIDER_APPLICANT("mortgage/applicant"),
    SWITCH_MORTGAGE_PROVIDER_APPLICANT_OTHER_LOANS("mortgage/applicant/other-loans"),
    SWITCH_MORTGAGE_PROVIDER_APPLICANT_OTHER_PROPERTIES("mortgage/applicant/other-properties"),
    SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT("mortgage/co-applicant"),
    SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_ADDRESS("mortgage/co-applicant-address"),
    SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_OTHER_LOANS("mortgage/co-applicant/other-loans"),
    SWITCH_MORTGAGE_PROVIDER_CONFIRMATION("mortgage/confirmation"),
    SWITCH_MORTGAGE_PROVIDER_CURRENT_MORTGAGE("mortgage/current-mortgage"),
    SWITCH_MORTGAGE_PROVIDER_HAS_AMORTIZATION_REQUIREMENT("mortgage/has-amortization-requirement"),
    SWITCH_MORTGAGE_PROVIDER_DEFERRAL_CAPITAL_GAINS_TAX("mortgage/deferral-capital-gains-tax"),
    SWITCH_MORTGAGE_PROVIDER_HOUSE_DETAILS("mortgage/house-details"),
    SWITCH_MORTGAGE_PROVIDER_PRODUCTS("mortgage/products"),
    SWITCH_MORTGAGE_PROVIDER_PRODUCTS_LOADING("mortgage/products-loading"),
    SWITCH_MORTGAGE_PROVIDER_PRODUCTS_NONE("mortgage/products-none"),
    SWITCH_MORTGAGE_PROVIDER_PRODUCT_DETAILS("mortgage/product-details"),
    SWITCH_MORTGAGE_PROVIDER_SECURITY("mortgage/security"),
    SWITCH_MORTGAGE_PROVIDER_KYC("mortgage/kyc"),
    SWITCH_MORTGAGE_PROVIDER_TAXABLE_IN_OTHER_COUNTRY("mortgage/taxable-in-other-country"),
    SWITCH_MORTGAGE_PROVIDER_PROPERTY_MARKET_VALUE("mortgage/property-market-value"),
    SWITCH_MORTGAGE_PROVIDER_HAS_CO_APPLICANT("mortgage/has-co-applicant"),
    SWITCH_MORTGAGE_PROVIDER_BAILMENT("mortgage/bailment"),
    SWITCH_MORTGAGE_PROVIDER_PAYING_ALIMONY("mortgage/paying-alimony"),
    SWITCH_MORTGAGE_PROVIDER_GETTING_ALIMONY("mortgage/getting-alimony"),
    SWITCH_MORTGAGE_PROVIDER_CURRENT_LOANS("mortgage/current-loans"),
    SWITCH_MORTGAGE_PROVIDER_STATUS_MOVE_MORTGAGE("mortgage/status-move-mortgage"),
    SWITCH_MORTGAGE_PROVIDER_STATUS_BANK_QUESTIONS("mortgage/status-bank-questions"),
    SWITCH_MORTGAGE_PROVIDER_PROFILE_INTRODUCTION("mortgage/profile-introduction"),
    SWITCH_MORTGAGE_PROVIDER_CSN_LOAN("mortgage/csn-loan"),
    SWITCH_MORTGAGE_PROVIDER_SECURITY_INTRODUCTION("mortgage/security-introduction"),
    SWITCH_MORTGAGE_PROVIDER_OTHER_PROPERTIES("mortgage/other-properties"),
    SWITCH_MORTGAGE_PROVIDER_EMPLOYMENT_TYPE("mortgage/employment-type"),
    SWITCH_MORTGAGE_PROVIDER_TAXABLE_IN_SWEDEN("mortgage/taxable-in-sweden"),
    SWITCH_MORTGAGE_PROVIDER_TAXABLE_IN_USA("mortgage/taxable-in-usa"),
    SWITCH_MORTGAGE_PROVIDER_IS_PEP("mortgage/is-pep"),
    SWITCH_MORTGAGE_PROVIDER_ON_OWN_BEHALF("mortgage/on-own-behalf"),
    SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_INTRODUCTION("mortgage/co-applicant-introduction"),
    SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_EMPLOYMENT_TYPE("mortgage/co-applicant-employment-type"),
    SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_PAYING_ALIMONY("mortgage/co-applicant-paying-alimony"),
    SWITCH_MORTGAGE_PROVIDER_CO_APPLICANT_CSN_LOAN("mortgage/co-applicant-csn-loan"),
    SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_INTRODUCTION("mortgage/household-introduction"),
    SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_CHILDREN("mortgage/household-children"),
    SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_ADULTS("mortgage/household-adults"),
    SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_OTHER_LOANS("mortgage/household-other-loans"),
    SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_BAILMENT("mortgage/household-bailment"),
    SWITCH_MORTGAGE_PROVIDER_HOUSEHOLD_DEFERRAL_CAPITAL_GAIN_TAX("mortgage/household-deferral-capital-gain-tax"),
    SWITCH_MORTGAGE_PROVIDER_STATUS_FINAL("mortgage/status-final"),
    SWITCH_MORTGAGE_PROVIDER_DIRECT_DEBIT("mortgage/direct-debit"),
    OPEN_SAVINGS_ACCOUNT_APPLICANT("savings/applicant"),
    OPEN_SAVINGS_ACCOUNT_PRODUCTS("savings/products"),
    OPEN_SAVINGS_ACCOUNT_PRODUCT_DETAILS("savings/product-details"),
    OPEN_SAVINGS_ACCOUNT_WITHDRAWAL_ACCOUNT("savings/withdrawal-account"),
    OPEN_SAVINGS_ACCOUNT_KYC("savings/kyc"),
    OPEN_SAVINGS_ACCOUNT_KYC_MULTI_SELECT_WITH_DEPENDENCIES("savings/kyc/multi-select-with-dependencies"),
    OPEN_SAVINGS_ACCOUNT_KYC_CITIZENSHIP("savings/kyc/citizenship"),
    OPEN_SAVINGS_ACCOUNT_KYC_PEP("savings/kyc/pep"),
    OPEN_SAVINGS_ACCOUNT_KYC_TAX("savings/kyc/tax"),
    OPEN_SAVINGS_ACCOUNT_CONFIRMATION("savings/confirmation"),
    RESIDENCE_VALUATION_RESIDENCE_TYPE("valuation/residence-type"),
    RESIDENCE_VALUATION_RESIDENCE_PARAMETERS("valuation/residence-parameters"),
    RESIDENCE_VALUATION_LOADING("valuation/loading"),
    SIGNATURE("signature"),
    SWITCH_MORTGAGE_PROVIDER_SALARY_IN_FOREIGN_CURRENCY("mortgage/salary-in-foreign-currency"),
    SWITCH_MORTGAGE_PROVIDER_TRANSFER_SAVINGS("mortgage/transfer-savings"),
    SWITCH_MORTGAGE_PROVIDER_OTHER_SERVICES("mortgage/other-services"),
    SWITCH_MORTGAGE_PROVIDER_LOAN_TERMS_CONFIRMATION("mortgage/loan-terms-confirmation"),
    SWITCH_MORTGAGE_PROVIDER_OTHER_ASSETS("mortgage/other-assets");

    public static final String DOCUMENTED = "form,mortgage/apartment-details,mortgage/applicant,"
            + "mortgage/applicant-other-loans,mortgage/applicant-other-properties,mortgage/co-applicant,"
            + "mortgage/verify-mortgage-date"
            + "mortgage/co-applicant-other-loans,mortgage/co-applicant-other-properties,mortgage/confirmation,"
            + "mortgage/current-mortgage,mortgage/deferral-capital-gains-tax,mortgage/house-details,mortgage/products,"
            + "mortgage/products-loading,mortgage/security,"
            + "mortgage/status-final,"
            + "mortgage/salary-in-foreign-currency,"
            + "savings/applicant,"
            + "savings/products,"
            + "savings/product-details,"
            + "savings/kyc,"
            + "savings/kyc/citizenship,"
            + "savings/kyc/pep,"
            + "savings/kyc/tax,"
            + "savings/confirmation,"
            + "valuation/residence-type,"
            + "valuation/residence-parameters,"
            + "valuation/loading,"
            + "mortgage/transfer-savings,"
            + "mortgage/other-services,"
            + "mortgage/has-amortization-requirement,"
            + "mortgage/salary-in-foreign-currency,"
            + "mortgage/loan-terms-confirmation";

    private String formType;

    ApplicationFormType(String formType) {
        this.formType = formType;
    }

    @Override
    public String toString() {
        return formType;
    }

    public static ApplicationFormType fromScheme(String scheme) {
        if (scheme != null) {
            for (ApplicationFormType type : ApplicationFormType.values()) {
                if (scheme.equalsIgnoreCase(type.formType)) {
                    return type;
                }
            }
        }
        return null;
    }
}
