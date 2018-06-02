package se.tink.backend.common.application.field;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Optional;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.backend.core.enums.ApplicationFieldType;
import se.tink.backend.utils.ApplicationUtils;

public class ApplicationFieldTemplate {

    public static final String DEPENDENCY_NAME_AND_VALUE_SEPARATOR = "=";
    public static final String DEPENDENCY_SEPARATOR = ",";

    public static final String REGEX_JSON_SIGNATURE = "^\\[\\[\\{[xy\\\"\\:\\d,\\}\\{\\[\\]]+\\}\\]\\]$";

    private static final String BOOLEAN_PATTERN = String.format("(%s|%s)", ApplicationFieldOptionValues.TRUE,
            ApplicationFieldOptionValues.FALSE);

    private static final ImmutableMap<String, FieldSpec> FIELD_SPEC_BY_NAME = initFieldSpecs();

    private static ImmutableMap<String, FieldSpec> initFieldSpecs() {
        ImmutableMap.Builder<String, FieldSpec> map = ImmutableMap.builder();

        map.put(ApplicationFieldName.IS_CORRECT_MORTGAGE, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.ESTIMATED_MARKET_VALUE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+"));

        map.put(ApplicationFieldName.CURRENT_MORTGAGE, FieldSpec.create(
                ApplicationFieldType.MULTI_SELECT,
                Optional.empty(), // Options will be dynamically populated.
                null));

        map.put(ApplicationFieldName.HAS_CO_APPLICANT, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.MORTGAGE_PRODUCT, FieldSpec.create(
                ApplicationFieldType.SELECT,
                Optional.empty(), // Options will be dynamically populated.
                null));

        map.put(ApplicationFieldName.MORTGAGE_COMPARISONS, FieldSpec.createOptional(
                ApplicationFieldType.SELECT,
                Optional.empty())); // Options will be dynamically populated.

        map.put(ApplicationFieldName.EMAIL, FieldSpec.create(
                ApplicationFieldType.EMAIL,
                "^[_a-z0-9-\\+]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9]+)*(\\.[a-z]{2,})$"));

        map.put(ApplicationFieldName.PHONE_NUMBER, FieldSpec.create(
                ApplicationFieldType.NUMERIC,
                "(00?|\\+)\\d{6,15}", // 0, 00 or + as prefix, followed by 6 to 15 digits.
                null));

        map.put(ApplicationFieldName.MONTHLY_INCOME, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+"));

        map.put(ApplicationFieldName.HAS_CSN_LOAN, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.CSN_LOAN_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.HAS_CSN_LOAN, ApplicationFieldOptionValues.YES),
                        dependsOn(ApplicationFieldName.CO_APPLICANT_CSN_LOAN, ApplicationFieldOptionValues.YES))));

        map.put(ApplicationFieldName.CSN_MONTHLY_COST, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.HAS_CSN_LOAN, ApplicationFieldOptionValues.YES),
                        dependsOn(ApplicationFieldName.CO_APPLICANT_CSN_LOAN, ApplicationFieldOptionValues.YES))));

        map.put(ApplicationFieldName.CURRENT_ASSETS, FieldSpec.createOptional(
                ApplicationFieldType.MULTI_SELECT,
                Optional.empty())); // Options will be dynamically populated.

        map.put(ApplicationFieldName.ADDED_ASSETS, FieldSpec.createOptional(
                ApplicationFieldType.MULTI_SELECT,
                Optional.empty())); // Options will be dynamically populated.

        map.put(ApplicationFieldName.OTHER_ASSET, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.FALSE));

        map.put(ApplicationFieldName.ASSET_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.OTHER_ASSET, ApplicationFieldOptionValues.TRUE)));

        map.put(ApplicationFieldName.ASSET_VALUE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.OTHER_ASSET, ApplicationFieldOptionValues.TRUE)));

        map.put(ApplicationFieldName.CURRENT_LOANS, FieldSpec.createOptional(
                ApplicationFieldType.MULTI_SELECT,
                Optional.empty())); // Options will be dynamically populated.

        map.put(ApplicationFieldName.ADDED_LOANS, FieldSpec.createOptional(
                ApplicationFieldType.MULTI_SELECT,
                Optional.empty())); // Options will be dynamically populated.

        map.put(ApplicationFieldName.OTHER_LOAN, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.FALSE));

        map.put(ApplicationFieldName.LOAN_LENDER, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.OTHER_LOAN, ApplicationFieldOptionValues.TRUE)));

        map.put(ApplicationFieldName.LOAN_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.OTHER_LOAN, ApplicationFieldOptionValues.TRUE)));

        map.put(ApplicationFieldName.BAILMENT, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.BAILMENT_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.BAILMENT, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.HAS_DEFERED_CAPITAL_GAINS_TAX, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.DEFERRED_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.HAS_DEFERED_CAPITAL_GAINS_TAX, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.PAYING_ALIMONY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.ALIMONY_AMOUNT_PER_MONTH, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.PAYING_ALIMONY, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.RECEIVING_ALIMONY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.RECEIVING_ALIMONY_COUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.RECEIVING_ALIMONY, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.LIVING_AREA, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+"));

        map.put(ApplicationFieldName.NUMBER_OF_ROOMS, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+([,\\.]5)?")); // 2 | 2,5 | 2.5

        map.put(ApplicationFieldName.HOUSING_COMMUNITY_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+]+"));

        map.put(ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+"));

        map.put(ApplicationFieldName.MONTHLY_AMORTIZATION, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+"));

        map.put(ApplicationFieldName.CADASTRAL, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+\\:\\.]+"));

        map.put(ApplicationFieldName.HOUSE_PURCHASE_PRICE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+"));

        map.put(ApplicationFieldName.MONTHLY_OPERATING_COST, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+"));

        map.put(ApplicationFieldName.PROPERTY_TYPE, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.NO_OTHER_PROPERTIES,
                        ApplicationFieldOptionValues.HOUSE,
                        ApplicationFieldOptionValues.APARTMENT,
                        ApplicationFieldOptionValues.VACATION_HOUSE,
                        ApplicationFieldOptionValues.TENANCY)));

        map.put(ApplicationFieldName.RESIDENCE_PROPERTY_TYPE, FieldSpec.create(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.HOUSE,
                        ApplicationFieldOptionValues.APARTMENT,
                        ApplicationFieldOptionValues.TENANCY),
                ApplicationFieldOptionValues.APARTMENT));

        map.put(ApplicationFieldName.SBAB_PROPERTY_TYPE, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.NO_OTHER_PROPERTIES,
                        ApplicationFieldOptionValues.HOUSE,
                        ApplicationFieldOptionValues.APARTMENT)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MARKET_VALUE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_LOAN_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT),
                        dependsOn(ApplicationFieldName.SBAB_PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT))));

        map.put(ApplicationFieldName.OTHER_PROPERTY_APARTMENT_MONTHLY_FEE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT),
                        dependsOn(ApplicationFieldName.SBAB_PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT))));

        map.put(ApplicationFieldName.OTHER_PROPERTY_HOUSE_OPERATING_COST, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.SBAB_PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_HOUSE_MARKET_VALUE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_HOUSE_ASSESSED_VALUE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE),
                        dependsOn(ApplicationFieldName.SBAB_PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE))));

        map.put(ApplicationFieldName.OTHER_PROPERTY_HOUSE_MUNICIPALITY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Optional.empty(), // Options will be dynamically populated.
                dependsOn(ApplicationFieldName.SBAB_PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_HOUSE_LABEL, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+\\:]+",
                null,
                dependsOn(ApplicationFieldName.SBAB_PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_HOUSE_LOAN_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE),
                        dependsOn(ApplicationFieldName.SBAB_PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE))));

        map.put(ApplicationFieldName.OTHER_PROPERTY_HOUSE_GROUND_RENT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_VACATION_HOUSE_MARKET_VALUE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.VACATION_HOUSE)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_VACATION_HOUSE_ASSESSED_VALUE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.VACATION_HOUSE)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_VACATION_HOUSE_LOAN_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.VACATION_HOUSE)));

        map.put(ApplicationFieldName.OTHER_PROPERTY_TENANCY_MONTHLY_RENT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.TENANCY)));

        map.put(ApplicationFieldName.HOUSEHOLD_NUMBER_OF_CHILDREN_TO_RECEIVE_CHILD_BENEFIT_FOR, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.HOUSEHOLD_CHILDREN, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.APPLICANT_NUMBER_OF_CHILDREN_PAYING_ALIMONY_FOR, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.PAYING_ALIMONY, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.CO_APPLICANT_NUMBER_OF_CHILDREN_PAYING_ALIMONY_FOR, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.CO_APPLICANT_PAYING_ALIMONY, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.EMPLOYMENT_TYPE, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT,
                        ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT,
                        ApplicationFieldOptionValues.SELF_EMPLOYED,
                        ApplicationFieldOptionValues.UNEMPLOYED,
                        ApplicationFieldOptionValues.STUDENT_RESEARCHER,
                        ApplicationFieldOptionValues.SENIOR,
                        ApplicationFieldOptionValues.OTHER_OCCUPATION)));

        map.put(ApplicationFieldName.FIRST_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[a-zA-Z\\p{L} \\-\\+]+"));

        map.put(ApplicationFieldName.LAST_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[a-zA-Z\\p{L} \\-\\+]+"));

        map.put(ApplicationFieldName.EMPLOYER_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\.\\-\\+]{1,30}",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT))));

        map.put(ApplicationFieldName.COMPANY_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\.\\-\\+]{1,30}",
                null,
                dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE, ApplicationFieldOptionValues.SELF_EMPLOYED)));

        map.put(ApplicationFieldName.SELF_EMPLOYED_SINCE, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "(19|20)[0-9]{2}-((1[0-2])|(0[1-9]))", // YYYY-MM
                null,
                dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE, ApplicationFieldOptionValues.SELF_EMPLOYED)));

        map.put(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\.\\-\\+]{1,24}", // Max 24 chars (SBAB reqs is Company + Profession < 48 chars)
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.SELF_EMPLOYED),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.OTHER_OCCUPATION))));

        map.put(ApplicationFieldName.PROFESSION, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".{1,24}", // Max 24 chars (SBAB reqs is Company + Profession < 48 chars)
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.SELF_EMPLOYED),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.OTHER_OCCUPATION))));

        map.put(ApplicationFieldName.SBAB_EMPLOYEE_SINCE, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "(19|20)[0-9]{2}-((1[0-2])|(0[1-9]))", // YYYY-MM
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.SELF_EMPLOYED),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.OTHER_OCCUPATION))));

        map.put(ApplicationFieldName.EMPLOYEE_SINCE, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "(19|20)[0-9]{2}-((1[0-2])|(0[1-9]))", // YYYY-MM
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT),
                        dependsOn(ApplicationFieldName.EMPLOYMENT_TYPE,
                                ApplicationFieldOptionValues.TEMPORARY_EMPLOYMENT))));

        map.put(ApplicationFieldName.TAXABLE_IN_SWEDEN, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.TAXABLE_IN_USA, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_USA, FieldSpec.create(
                ApplicationFieldType.NUMERIC,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.TAXABLE_IN_USA, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.TAXABLE_IN_YET_ANOTHER_COUNTRY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER_OTHER_COUNTRY, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.YES),
                        dependsOn(ApplicationFieldName.TAXABLE_IN_YET_ANOTHER_COUNTRY,
                                ApplicationFieldOptionValues.YES))));

        map.put(ApplicationFieldName.SALARY_IN_FOREIGN_CURRENCY, FieldSpec.create(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO),
                null));

        map.put(ApplicationFieldName.CO_APPLICANT_ADDRESS, FieldSpec.create(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO),
                null));

        map.put(ApplicationFieldName.CO_APPLICANT_STREET_ADDRESS, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+]+",
                null,
                dependsOn(ApplicationFieldName.CO_APPLICANT_ADDRESS, ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.CO_APPLICANT_POSTAL_CODE, FieldSpec.create(
                ApplicationFieldType.NUMERIC,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.CO_APPLICANT_ADDRESS, ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.CO_APPLICANT_TOWN, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+]+",
                null,
                dependsOn(ApplicationFieldName.CO_APPLICANT_ADDRESS, ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.CO_APPLICANT_PAYING_ALIMONY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.CO_APPLICANT_ALIMONY_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.CO_APPLICANT_PAYING_ALIMONY, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.CO_APPLICANT_RECEIVING_ALIMONY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.CO_APPLICANT_RECEIVING_ALIMONY_COUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.CO_APPLICANT_RECEIVING_ALIMONY, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.CO_APPLICANT_CSN_LOAN, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.HOUSEHOLD_CHILDREN, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.HOUSEHOLD_CHILDREN_COUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.HOUSEHOLD_CHILDREN, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.HOUSEHOLD_CHILDREN_ALIMONY_COUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.HOUSEHOLD_CHILDREN, ApplicationFieldOptionValues.YES),
                        dependsOn(ApplicationFieldName.HOUSEHOLD_CHILDREN_UNDER_18,
                                ApplicationFieldOptionValues.YES))));

        map.put(ApplicationFieldName.HOUSEHOLD_ADULTS, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOANS, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOAN_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.SEB_CO_APPLICANT_OTHER_LOANS, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.SEB_CO_APPLICANT_BAILMENT, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.SEB_CO_APPLICANT_BAILMENT_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.SEB_CO_APPLICANT_BAILMENT, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX_AMOUNT, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null,
                dependsOn(ApplicationFieldName.SEB_CO_APPLICANT_DEFERRAL_CAPITAL_GAIN_TAX,
                        ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.STREET_ADDRESS, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+]+"));

        map.put(ApplicationFieldName.POSTAL_CODE, FieldSpec.create(
                ApplicationFieldType.NUMERIC,
                "[0-9]+"));

        map.put(ApplicationFieldName.ASSESSED_VALUE, FieldSpec.create(
                ApplicationFieldType.NUMBER,
                "[0-9]+"));

        map.put(ApplicationFieldName.TOWN, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+]+"));

        map.put(ApplicationFieldName.MORTGAGE_SECURITY_STREET_ADDRESS, FieldSpec.createRequired(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+]+",
                dependsOn(ApplicationFieldName.IS_CORRECT_MORTGAGE, ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.MORTGAGE_SECURITY_POSTAL_CODE, FieldSpec.createRequired(
                ApplicationFieldType.NUMERIC,
                "[0-9]+",
                dependsOn(ApplicationFieldName.IS_CORRECT_MORTGAGE, ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.MORTGAGE_SECURITY_TOWN, FieldSpec.createRequired(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+]+",
                dependsOn(ApplicationFieldName.IS_CORRECT_MORTGAGE, ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.MORTGAGE_SECURITY_PROPERTY_TYPE, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.HOUSE,
                        ApplicationFieldOptionValues.APARTMENT,
                        ApplicationFieldOptionValues.VACATION_HOUSE),
                dependsOn(ApplicationFieldName.IS_CORRECT_MORTGAGE, ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.MUNICIPALITY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Optional.empty())); // Options will be dynamically populated.

        map.put(ApplicationFieldName.DEFAULT_STREET_ADDRESS, FieldSpec.createReadOnly(
                ApplicationFieldType.HIDDEN,
                "[0-9\\p{L} \\-\\+]+"));

        map.put(ApplicationFieldName.DEFAULT_POSTAL_CODE, FieldSpec.createReadOnly(
                ApplicationFieldType.HIDDEN,
                "[0-9]+"));

        map.put(ApplicationFieldName.DEFAULT_TOWN, FieldSpec.createReadOnly(
                ApplicationFieldType.HIDDEN,
                "[0-9\\p{L} \\-\\+]+"));

        map.put(ApplicationFieldName.DEFAULT_PROPERTY_TYPE, FieldSpec.createReadOnly(
                ApplicationFieldType.HIDDEN,
                String.format("(%s|%s)", ApplicationFieldOptionValues.HOUSE, ApplicationFieldOptionValues.APARTMENT)));

        map.put(ApplicationFieldName.IS_PEP, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.ON_OWN_BEHALF, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                "[0-9\\p{L} \\-\\+]+"));

        map.put(ApplicationFieldName.PERSONAL_NUMBER, FieldSpec.create(
                ApplicationFieldType.NUMERIC,
                "(19|20)[0-9]{6}(\\-)?[0-9]{4}"));

        map.put(ApplicationFieldName.RELATIONSHIP_STATUS, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.MARRIED,
                        ApplicationFieldOptionValues.COHABITANT,
                        ApplicationFieldOptionValues.SINGLE)));

        map.put(ApplicationFieldName.SWEDISH_CITIZEN, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.SAVINGS_PRODUCT, FieldSpec.create(
                ApplicationFieldType.SELECT,
                Optional.empty(), // Options will be dynamically populated.
                null));

        map.put(ApplicationFieldName.COLLECTOR_SAVINGS_PURPOSE, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.PURPOSE_PRIVATE_CONSUMPTION,
                        ApplicationFieldOptionValues.PURPOSE_ONGOING_EXPENSES,
                        ApplicationFieldOptionValues.PURPOSE_PENSION,
                        ApplicationFieldOptionValues.PURPOSE_LONG_TERM_INVESTMENT,
                        ApplicationFieldOptionValues.PURPOSE_SAVING_FOR_RELATED_PARTIES,
                        ApplicationFieldOptionValues.PURPOSE_INVESTMENT_OF_SURPLUS_LIQUIDITY,
                        ApplicationFieldOptionValues.PURPOSE_FUTURE_TAX_PAYMENTS)));

        map.put(ApplicationFieldName.COLLECTOR_INITIAL_DEPOSIT, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.LESS_THAN_10000,
                        ApplicationFieldOptionValues.BETWEEN_10000_AND_25000,
                        ApplicationFieldOptionValues.BETWEEN_25000_AND_50000,
                        ApplicationFieldOptionValues.MORE_THAN_50000)));

        map.put(ApplicationFieldName.COLLECTOR_SAVINGS_FREQUENCY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH,
                        ApplicationFieldOptionValues.ONE_TO_FIVE_TIMES_A_MONTH,
                        ApplicationFieldOptionValues.MORE_THAN_FIVE_TIMES_A_MONTH)));

        map.put(ApplicationFieldName.COLLECTOR_SAVINGS_SOURCES, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.MY_ACCOUNT_IN_SWEDISH_BANK,
                        ApplicationFieldOptionValues.OTHERS_ACCOUNT_IN_SWEDISH_BANK,
                        ApplicationFieldOptionValues.ACCOUNT_IN_FOREIGN_BANK)));

        map.put(ApplicationFieldName.COLLECTOR_SAVINGS_SOURCES_REASON, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.SALARY_OR_PENSION,
                        ApplicationFieldOptionValues.GIFT_OR_INHERITANCE,
                        ApplicationFieldOptionValues.SWITCH_OF_BANK_OR_SAVINGS_FORM,
                        ApplicationFieldOptionValues.LOTTERY_OR_GAMBLING,
                        ApplicationFieldOptionValues.SALE_OF_CAPITAL_GOODS,
                        ApplicationFieldOptionValues.SALE_OF_PROPERTY,
                        ApplicationFieldOptionValues.SALE_OF_COMPANY,
                        ApplicationFieldOptionValues.SURPLUS_LIQUIDITY,
                        ApplicationFieldOptionValues.SECURITIES_TRADING,
                        ApplicationFieldOptionValues.TAX_REFUND)));

        map.put(ApplicationFieldName.COLLECTOR_MONEY_WITHDRAWAL, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH,
                        ApplicationFieldOptionValues.ONE_TO_FIVE_TIMES_A_MONTH,
                        ApplicationFieldOptionValues.MORE_THAN_FIVE_TIMES_A_MONTH)));

        map.put(ApplicationFieldName.COLLECTOR_ACCOUNT_FOR_MONEY_WITHDRAWAL, FieldSpec.create(
                ApplicationFieldType.SELECT,
                Optional.empty(), // Options will be dynamically populated.
                null));

        map.put(ApplicationFieldName.SBAB_SAVINGS_PURPOSE, FieldSpec.createRequired(
                ApplicationFieldType.MULTI_SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.PURPOSE_PAYMENTS_OF_LOAN_AT_SBAB,
                        ApplicationFieldOptionValues.PURPOSE_OPERATING_COSTS_OF_MY_RESIDENCE,
                        ApplicationFieldOptionValues.PURPOSE_FINANCIAL_BUFFER,
                        ApplicationFieldOptionValues.PURPOSE_ONGOING_EXPENSES,
                        ApplicationFieldOptionValues.PURPOSE_SAVING_FOR_RELATED_PARTIES,
                        ApplicationFieldOptionValues.OTHER)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_PURPOSE_OTHER_VALUE, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_PURPOSE, ApplicationFieldOptionValues.OTHER)));

        map.put(ApplicationFieldName.SBAB_INITIAL_DEPOSIT, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.LESS_THAN_50000,
                        ApplicationFieldOptionValues.BETWEEN_50000_AND_150000,
                        ApplicationFieldOptionValues.BETWEEN_150000_AND_250000,
                        ApplicationFieldOptionValues.BETWEEN_250000_AND_350000,
                        ApplicationFieldOptionValues.MORE_THAN_350000)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_FREQUENCY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH,
                        ApplicationFieldOptionValues.ONE_TO_FIVE_TIMES_A_MONTH,
                        ApplicationFieldOptionValues.SIX_TO_TEN_TIMES_A_MONTH,
                        ApplicationFieldOptionValues.MORE_THAN_TEN_TIMES_A_MONTH)));

        map.put(ApplicationFieldName.SBAB_PERSONS_SAVING, FieldSpec.createRequired(
                ApplicationFieldType.MULTI_SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.PERSONS_SAVING_ME,
                        ApplicationFieldOptionValues.PERSONS_SAVING_OTHER)));

        map.put(ApplicationFieldName.SBAB_PERSONS_SAVING_OTHER_VALUE, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_PERSONS_SAVING,
                        ApplicationFieldOptionValues.PERSONS_SAVING_OTHER)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES, FieldSpec.createRequired(
                ApplicationFieldType.MULTI_SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.MY_ACCOUNT_IN_SWEDISH_BANK,
                        ApplicationFieldOptionValues.OTHER_WAY)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK, FieldSpec.createRequired(
                ApplicationFieldType.MULTI_SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.SWEDBANK,
                        ApplicationFieldOptionValues.HANDELSBANKEN,
                        ApplicationFieldOptionValues.NORDEA,
                        ApplicationFieldOptionValues.SEB,
                        ApplicationFieldOptionValues.DANSKE_BANK,
                        ApplicationFieldOptionValues.OTHER),
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES,
                        ApplicationFieldOptionValues.MY_ACCOUNT_IN_SWEDISH_BANK)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK_OTHER_VALUE, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK,
                        ApplicationFieldOptionValues.OTHER)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_OTHER_WAY_VALUE, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES, ApplicationFieldOptionValues.OTHER_WAY)));

        map.put(ApplicationFieldName.SBAB_MONEY_WITHDRAWAL, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH,
                        ApplicationFieldOptionValues.ONE_TO_FIVE_TIMES_A_MONTH,
                        ApplicationFieldOptionValues.FIVE_TO_TEN_TIMES_A_MONTH,
                        ApplicationFieldOptionValues.MORE_THAN_TEN_TIMES_A_MONTH)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_AMOUNT_PER_MONTH, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.LESS_THAN_10000,
                        ApplicationFieldOptionValues.BETWEEN_10000_AND_20000,
                        ApplicationFieldOptionValues.BETWEEN_20000_AND_30000,
                        ApplicationFieldOptionValues.BETWEEN_30000_AND_40000,
                        ApplicationFieldOptionValues.MORE_THAN_40000
                )));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON, FieldSpec.createRequired(
                ApplicationFieldType.MULTI_SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.SALARY_OR_PENSION,
                        ApplicationFieldOptionValues.OWN_BUSINESS_OR_DIVIDEND,
                        ApplicationFieldOptionValues.OTHER_SAVINGS,
                        ApplicationFieldOptionValues.SALE_OF_ASSETS,
                        ApplicationFieldOptionValues.GIFT_OR_INHERITANCE,
                        ApplicationFieldOptionValues.OTHER)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.INDUSTRY_IT,
                        ApplicationFieldOptionValues.INDUSTRY_SALES,
                        ApplicationFieldOptionValues.INDUSTRY_HOTEL,
                        ApplicationFieldOptionValues.INDUSTRY_CULTURE,
                        ApplicationFieldOptionValues.INDUSTRY_RESTAURANT,
                        ApplicationFieldOptionValues.INDUSTRY_PAYMENTS,
                        ApplicationFieldOptionValues.INDUSTRY_GAMING,
                        ApplicationFieldOptionValues.INDUSTRY_HAIRDRESSER,
                        ApplicationFieldOptionValues.INDUSTRY_HEALTH,
                        ApplicationFieldOptionValues.INDUSTRY_CONSTRUCTION,
                        ApplicationFieldOptionValues.INDUSTRY_WEAPON,
                        ApplicationFieldOptionValues.INDUSTRY_TECH,
                        ApplicationFieldOptionValues.OTHER),
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON,
                        ApplicationFieldOptionValues.OWN_BUSINESS_OR_DIVIDEND)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_RESTAURANT_COMPANY_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                        ApplicationFieldOptionValues.INDUSTRY_RESTAURANT)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_RESTAURANT_COMPANY_REGISTRATION_NUMBER,
                FieldSpec.create(
                        ApplicationFieldType.TEXT,
                        ".+",
                        null,
                        dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                                ApplicationFieldOptionValues.INDUSTRY_RESTAURANT)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_PAYMENT_COMPANY_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                        ApplicationFieldOptionValues.INDUSTRY_PAYMENTS)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_PAYMENT_COMPANY_REGISTRATION_NUMBER,
                FieldSpec.create(
                        ApplicationFieldType.TEXT,
                        ".+",
                        null,
                        dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                                ApplicationFieldOptionValues.INDUSTRY_PAYMENTS)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_GAMING_COMPANY_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                        ApplicationFieldOptionValues.INDUSTRY_GAMING)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_GAMING_COMPANY_REGISTRATION_NUMBER,
                FieldSpec.create(
                        ApplicationFieldType.TEXT,
                        ".+",
                        null,
                        dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                                ApplicationFieldOptionValues.INDUSTRY_GAMING)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_HAIRDRESSER_COMPANY_NAME,
                FieldSpec.create(
                        ApplicationFieldType.TEXT,
                        ".+",
                        null,
                        dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                                ApplicationFieldOptionValues.INDUSTRY_HAIRDRESSER)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_HAIRDRESSER_COMPANY_REGISTRATION_NUMBER,
                FieldSpec.create(
                        ApplicationFieldType.TEXT,
                        ".+",
                        null,
                        dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                                ApplicationFieldOptionValues.INDUSTRY_HAIRDRESSER)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_CONSTRUCTION_COMPANY_NAME,
                FieldSpec.create(
                        ApplicationFieldType.TEXT,
                        ".+",
                        null,
                        dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                                ApplicationFieldOptionValues.INDUSTRY_CONSTRUCTION)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_CONSTRUCTION_COMPANY_REGISTRATION_NUMBER,
                FieldSpec.create(
                        ApplicationFieldType.TEXT,
                        ".+",
                        null,
                        dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                                ApplicationFieldOptionValues.INDUSTRY_CONSTRUCTION)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_WEAPON_COMPANY_NAME, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                        ApplicationFieldOptionValues.INDUSTRY_WEAPON)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_WEAPON_COMPANY_REGISTRATION_NUMBER,
                FieldSpec.create(
                        ApplicationFieldType.TEXT,
                        ".+",
                        null,
                        dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                                ApplicationFieldOptionValues.INDUSTRY_WEAPON)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_OTHER_VALUE, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OWN_BUSINESS_INDUSTRY,
                        ApplicationFieldOptionValues.OTHER)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON_OTHER_VALUE, FieldSpec.create(
                ApplicationFieldType.TEXT,
                ".+",
                null,
                dependsOn(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON, ApplicationFieldOptionValues.OTHER)));

        map.put(ApplicationFieldName.SBAB_SAVINGS_MONTHLY_INCOME, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.LESS_THAN_20000,
                        ApplicationFieldOptionValues.BETWEEN_20000_AND_35000,
                        ApplicationFieldOptionValues.BETWEEN_35000_AND_50000,
                        ApplicationFieldOptionValues.BETWEEN_50000_AND_70000,
                        ApplicationFieldOptionValues.MORE_THAN_70000)));

        map.put(ApplicationFieldName.CITIZENSHIP_IN_OTHER_COUNTRY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.CITIZENSHIP_IN_YET_ANOTHER_COUNTRY, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.CITIZENSHIP_COUNTRY, FieldSpec.create(
                ApplicationFieldType.SELECT,
                Optional.empty(), // Options will be dynamically populated.
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.CITIZENSHIP_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.YES),
                        dependsOn(ApplicationFieldName.CITIZENSHIP_IN_YET_ANOTHER_COUNTRY,
                                ApplicationFieldOptionValues.YES))));

        map.put(ApplicationFieldName.TAXABLE_COUNTRY, FieldSpec.create(
                ApplicationFieldType.SELECT,
                Optional.empty(), // Options will be dynamically populated.
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.YES),
                        dependsOn(ApplicationFieldName.TAXABLE_IN_YET_ANOTHER_COUNTRY,
                                ApplicationFieldOptionValues.YES))));

        map.put(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER, FieldSpec.create(
                ApplicationFieldType.NUMERIC,
                "[0-9]+",
                null,
                dependsOnAny(
                        dependsOn(ApplicationFieldName.TAXABLE_IN_OTHER_COUNTRY, ApplicationFieldOptionValues.YES),
                        dependsOn(ApplicationFieldName.TAXABLE_IN_YET_ANOTHER_COUNTRY,
                                ApplicationFieldOptionValues.YES))));

        map.put(ApplicationFieldName.DIRECT_DEBIT_ACCOUNT, FieldSpec.create(
                ApplicationFieldType.SELECT,
                Optional.empty(),
                null));

        map.put(ApplicationFieldName.DIRECT_DEBIT_CONFIRM, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.YES));

        map.put(ApplicationFieldName.CONFIRM_CREDIT_REPORT, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.FALSE));

        map.put(ApplicationFieldName.CONFIRM_EMPLOYER_CONTACT, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.FALSE));

        map.put(ApplicationFieldName.CONFIRM_POWER_OF_ATTORNEY, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.FALSE));

        map.put(ApplicationFieldName.CONFIRM_PUL, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.FALSE));

        map.put(ApplicationFieldName.CONFIRM_SALARY_EXTRACT, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.FALSE));

        map.put(ApplicationFieldName.SIGNATURE, FieldSpec.create(
                ApplicationFieldType.SIGNATURE,
                REGEX_JSON_SIGNATURE));

        map.put(ApplicationFieldName.COLLECTOR_SAVINGS_CONFIRMATION, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.FALSE));

        map.put(ApplicationFieldName.SBAB_SAVINGS_CONFIRMATION, FieldSpec.create(
                ApplicationFieldType.CHECKBOX,
                BOOLEAN_PATTERN,
                ApplicationFieldOptionValues.FALSE));

        map.put(ApplicationFieldName.SEB_TRANSFER_SAVINGS, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.SEB_TRANSFER_ORIGIN, FieldSpec.create(
                ApplicationFieldType.MULTI_SELECT,
                Optional.of(Lists.newArrayList(
                        ApplicationFieldOptionValues.SALARY,
                        ApplicationFieldOptionValues.PENSION,
                        ApplicationFieldOptionValues.INSURANCE_PAYOUT,
                        ApplicationFieldOptionValues.GIFT_OR_INHERITANCE,
                        ApplicationFieldOptionValues.PROPERTY_SALE,
                        ApplicationFieldOptionValues.DIVIDENDS_FROM_SMALL_BUSINESS,
                        ApplicationFieldOptionValues.SALE_OF_COMPANY,
                        ApplicationFieldOptionValues.OTHER
                )),
                null,
                dependsOn(ApplicationFieldName.SEB_TRANSFER_SAVINGS, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.SEB_OTHER_SERVICES_INTEREST, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        map.put(ApplicationFieldName.SEB_OTHER_SERVICES_OPTIONS, FieldSpec.create(
                ApplicationFieldType.MULTI_SELECT,
                Optional.of(Lists.newArrayList(
                        ApplicationFieldOptionValues.SAVINGS,
                        ApplicationFieldOptionValues.CAPITAL_MANAGEMENT,
                        ApplicationFieldOptionValues.CARDS,
                        ApplicationFieldOptionValues.PENSION,
                        ApplicationFieldOptionValues.PENSION_INSURANCE,
                        ApplicationFieldOptionValues.INVESTING
                )),
                null,
                dependsOn(ApplicationFieldName.SEB_OTHER_SERVICES_INTEREST, ApplicationFieldOptionValues.YES)));

        map.put(ApplicationFieldName.VALUATION_RESIDENCE_TYPE, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.APARTMENT,
                        ApplicationFieldOptionValues.HOUSE,
                        ApplicationFieldOptionValues.VACATION_HOUSE,
                        ApplicationFieldOptionValues.TERRACE_HOUSE,
                        ApplicationFieldOptionValues.CHAIN_TERRACE_HOUSE,
                        ApplicationFieldOptionValues.SEMI_DETACHED_HOUSE)));

        map.put(ApplicationFieldName.CONSTRUCTION_YEAR, FieldSpec.createRequired(
                ApplicationFieldType.NUMERIC,
                "[0-9]+",
                null));

        map.put(ApplicationFieldName.APARTMENT_NUMBER, FieldSpec.createRequired(
                ApplicationFieldType.NUMERIC,
                "([1-9][0-9]{3})|(0[1-9][0-9]{2})",
                null));

        map.put(ApplicationFieldName.VALUATION_MONTHLY_HOUSING_COMMUNITY_FEE, FieldSpec.createRequired(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null));

        map.put(ApplicationFieldName.PLOT_AREA, FieldSpec.createRequired(
                ApplicationFieldType.NUMERIC,
                "[0-9]+",
                null));

        map.put(ApplicationFieldName.ADDITIONAL_AREA, FieldSpec.createRequired(
                ApplicationFieldType.NUMERIC,
                "[0-9]+",
                null));

        map.put(ApplicationFieldName.VALUATION_MONTHLY_OPERATING_COST, FieldSpec.createRequired(
                ApplicationFieldType.NUMBER,
                "[0-9]+",
                null));

        map.put(ApplicationFieldName.HAS_AMORTIZATION_REQUIREMENT, FieldSpec.createRequired(
                ApplicationFieldType.SELECT,
                Lists.newArrayList(
                        ApplicationFieldOptionValues.YES,
                        ApplicationFieldOptionValues.NO)));

        return map.build();
    }

    private static String dependsOn(String fieldName, String fieldValue) {
        return fieldName + DEPENDENCY_NAME_AND_VALUE_SEPARATOR + fieldValue;
    }

    private static String dependsOnAny(String... dependencies) {
        return Joiner.on(DEPENDENCY_SEPARATOR).join(dependencies);
    }

    public static boolean hasDependency(ApplicationField field) {
        if (!Strings.isNullOrEmpty(field.getDependency())) {
            return true;
        }
        return false;
    }

    public static boolean isDependencySatisfied(ApplicationField field, ApplicationForm form) {
        if (hasDependency(field)) {
            Multimap<String, String> dependencyMap = getDependencyMap(field);

            for (String dependencyName : dependencyMap.keySet()) {
                Optional<ApplicationField> dependencyField = form.getField(dependencyName);

                if (!dependencyField.isPresent()) {
                    continue;
                }

                if (ApplicationUtils.isFieldContainingExpectedValue(
                        dependencyField.get(),
                        dependencyMap.get(dependencyName))) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private static Multimap<String, String> getDependencyMap(ApplicationField field) {
        Multimap<String, String> dependencyMap = ArrayListMultimap.create();

        String[] dependencies = field.getDependency().split(DEPENDENCY_SEPARATOR);
        for (String dependency : dependencies) {
            if (dependency == null) {
                continue;
            }

            String[] dependencyParts = dependency.split(DEPENDENCY_NAME_AND_VALUE_SEPARATOR);

            if (dependencyParts.length != 2 ||
                    Strings.isNullOrEmpty(dependencyParts[0]) ||
                    Strings.isNullOrEmpty(dependencyParts[1])) {
                continue;
            }

            dependencyMap.put(dependencyParts[0], dependencyParts[1]);
        }

        return dependencyMap;
    }

    public ApplicationFieldTemplate() {

    }

    public FieldSpec getSpecForName(String fieldName) {
        FieldSpec fieldSpec = FIELD_SPEC_BY_NAME.get(fieldName);
        return fieldSpec;
    }

    static class FieldSpec {
        public String defaultValue;
        public String pattern;
        public ApplicationFieldType type;
        public Optional<List<String>> options;
        public boolean required = true;
        public String dependency;
        public boolean readOnly;

        public static FieldSpec createOptional(ApplicationFieldType type, String pattern) {
            return createOptional(type, pattern, null);
        }

        public static FieldSpec createOptional(ApplicationFieldType type, String pattern, String defaultValue) {
            FieldSpec spec = create(type, pattern, defaultValue);
            spec.required = false;
            return spec;
        }

        public static FieldSpec create(ApplicationFieldType type, String pattern) {
            return create(type, pattern, null);
        }

        public static FieldSpec create(ApplicationFieldType type, String pattern, String defaultValue) {
            FieldSpec spec = new FieldSpec();
            spec.type = type;
            spec.pattern = pattern;
            spec.defaultValue = defaultValue;
            spec.options = Optional.empty();
            return spec;
        }

        public static FieldSpec create(ApplicationFieldType type, String pattern, String defaultValue,
                String dependency) {
            FieldSpec spec = create(type, pattern, defaultValue);
            spec.dependency = dependency;
            return spec;
        }

        public static FieldSpec createRequired(ApplicationFieldType type, String pattern, String dependency) {
            FieldSpec spec = create(type, pattern, null);
            spec.dependency = dependency;
            return spec;
        }

        public static FieldSpec createRequired(ApplicationFieldType type, List<String> options) {
            return create(type, Optional.of(options), null);
        }

        public static FieldSpec createRequired(ApplicationFieldType type, List<String> options, String dependency) {
            return createRequired(type, Optional.of(options), dependency);
        }

        public static FieldSpec createRequired(ApplicationFieldType type, Optional<List<String>> options) {
            return create(type, options, null);
        }

        public static FieldSpec createRequired(ApplicationFieldType type, Optional<List<String>> options,
                String dependency) {
            FieldSpec spec = createRequired(type, options);
            spec.dependency = dependency;
            return spec;
        }

        public static FieldSpec create(ApplicationFieldType type, Optional<List<String>> options, String defaultValue) {
            FieldSpec spec = new FieldSpec();
            spec.type = type;
            spec.options = options;
            spec.defaultValue = defaultValue;
            return spec;
        }

        public static FieldSpec create(ApplicationFieldType type, Optional<List<String>> options, String defaultValue,
                String dependency) {
            FieldSpec spec = create(type, options, defaultValue);
            spec.dependency = dependency;
            return spec;
        }

        public static FieldSpec create(ApplicationFieldType type, List<String> options, String defaultValue) {
            return create(type, Optional.ofNullable(options), defaultValue);
        }

        public static FieldSpec createOptional(ApplicationFieldType type, List<String> options) {
            return createOptional(type, Optional.of(options), null);
        }

        public static FieldSpec createOptional(ApplicationFieldType type, Optional<List<String>> options) {
            return createOptional(type, options, null);
        }

        public static FieldSpec createOptional(ApplicationFieldType type, Optional<List<String>> options,
                String defaultValue) {
            FieldSpec spec = new FieldSpec();
            spec.type = type;
            spec.options = options;
            spec.defaultValue = defaultValue;
            spec.required = false;
            return spec;
        }

        public static FieldSpec createReadOnly(ApplicationFieldType type, String pattern) {
            return createReadOnly(type, pattern, null);
        }

        public static FieldSpec createReadOnly(ApplicationFieldType type, String pattern, String defaultValue) {
            FieldSpec spec = create(type, pattern, defaultValue);
            spec.readOnly = true;
            return spec;
        }
    }
}

