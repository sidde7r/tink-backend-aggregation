package se.tink.backend.aggregation.agents.banks.seb.mortgage.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.banks.seb.mortgage.model.LoanPostRequest;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;

@RunWith(Enclosed.class)
public class ApplicationToLoanPostRequestMapperTest {
    public static class Mapper {
        @Test(expected = IllegalArgumentException.class)
        public void nullThrows() {
            ApplicationToLoanPostRequestMapper mapper = new ApplicationToLoanPostRequestMapperImpl(
                    mock(AggregationLogger.class));
            mapper.toLoanRequest(null);
        }

        @Test
        public void ensureExampleMappingIsIntact() throws IOException, IllegalAccessException {
            GenericApplication application = new GenericApplication();
            // Irrelevant for mapping but they were in the object sent in
            application.setApplicationId(UUID.fromString("39a6a27b-68b0-4a19-942f-6985da38c628"));
            application.setPersonalNumber("198607015537");
            application.setCredentialsId(UUID.fromString("7ee9b7bd-2851-4bf1-8f08-ce6183cb1a48"));
            application.setProductId(UUID.fromString("1f5a0fcd-9c26-49d0-9580-cbbaf4288f2f"));
            application.setType(ApplicationType.SWITCH_MORTGAGE_PROVIDER);

            // All values come from these
            application.setFieldGroups(Lists.newArrayList(
                    createProduct(),
                    createCurrentMortgage(),
                    createApplicants(),
                    createHouseHold(),
                    createMortgageSecurity()));

            ApplicationToLoanPostRequestMapperImpl mapper =
                    new ApplicationToLoanPostRequestMapperImpl(mock(AggregationLogger.class));
            LoanPostRequest loanPostRequest = mapper.toLoanRequest(application);

            LoanPostRequest expectedRequest = new ObjectMapper().readValue(
                    "{\"alimony_amount_per_month\":0.0,\"alimony_amount_per_month_codebtor\":0.0,\"all_first_names\":\"Malin\",\"all_first_names_codebtor\":\"Medsok\",\"apartment_designation\":null,\"approve\":true,\"bank_name\":\"Danske Bank\",\"cadastral\":null,\"cell_phone_no\":\"0701122333\",\"cell_phone_no_codebtor\":\"0701123112\",\"city\":\"Knivsta\",\"city_codebtor\":\"Stockholm\",\"co\":null,\"co_codebtor\":null,\"codebtor_customer_number\":\"201212121212\",\"country\":\"SE\",\"country_codebtor\":\"SE\",\"credit_card_and_various_debts\":false,\"credit_card_and_various_debts_amount\":0.0,\"current_number_of_applicants\":2,\"customer_number\":\"198607015537\",\"deferred_capital_gains_tax_amount\":0.0,\"email\":\"daniel.lervik@tink.se\",\"email_codebtor\":\"daniel.lervik_1@tink.se\",\"employer\":\"Tink Ab\",\"employer_codebtor\":\"Self employee company\",\"employment_since\":\"2015-12\",\"employment_since_codebtor\":\"2010-10\",\"employment_type\":\"TILLSVIDARE\",\"employment_type_codebtor\":\"EGENFÖRETAGARE\",\"estimated_total_value_of_savings\":null,\"filter\":\"Unit test version 123\",\"first_name\":\"Malin\",\"first_name_codebtor\":\"Medsok\",\"has_deferred_capital_gains_tax\":false,\"has_surety_for_someones_loans\":false,\"have_student_loan\":true,\"have_student_loan_codebtor\":false,\"interest_rate\":2.1,\"kyc_additional_tax_residency\":true,\"kyc_alt_city\":null,\"kyc_alt_co\":null,\"kyc_alt_country\":null,\"kyc_alt_firstname\":null,\"kyc_alt_street_address\":null,\"kyc_alt_tax_identification_number\":\"12313011\",\"kyc_alt_tax_identification_number_is_missing\":false,\"kyc_alt_zip_code\":null,\"kyc_ambassador\":null,\"kyc_assets\":false,\"kyc_board_member_of_an_international_organization\":null,\"kyc_card\":false,\"kyc_cell_phone_no\":\"0701122333\",\"kyc_child\":null,\"kyc_childs_wife_or_husband_partner_or_cohabitant\":null,\"kyc_city\":\"Knivsta\",\"kyc_co\":null,\"kyc_country\":\"SE\",\"kyc_country_alternatively_organization\":null,\"kyc_custody\":false,\"kyc_customer_number\":\"198607015537\",\"kyc_deputy_director_of_an_intern_organization\":null,\"kyc_diplomatic_envoys\":null,\"kyc_director_of_an_international_organization\":null,\"kyc_director_of_the_central_bank\":null,\"kyc_email\":\"daniel.lervik@tink.se\",\"kyc_employer_company_name\":\"Tink Ab\",\"kyc_employment_type\":\"ANSTÄLLD\",\"kyc_financing\":true,\"kyc_firstname\":\"Malin\",\"kyc_give_alt_pep_name\":null,\"kyc_give_country\":\"FI\",\"kyc_give_reason\":null,\"kyc_high_officers_in_the_armed_forces\":null,\"kyc_husband_or_wife\":null,\"kyc_i_myself_am_a_politically_exposed_person\":null,\"kyc_income_gain\":false,\"kyc_investment\":false,\"kyc_is_alt_mailing_address\":false,\"kyc_is_pep\":false,\"kyc_is_tax_resident_in_sweden\":true,\"kyc_is_tax_resident_in_usa\":false,\"kyc_judge_in_another_court\":null,\"kyc_judge_of_the_supreme_court\":null,\"kyc_known_employee\":null,\"kyc_living_economy\":true,\"kyc_mep\":null,\"kyc_middlename\":null,\"kyc_minister\":null,\"kyc_official_at_the_audit_office\":null,\"kyc_other_tax_residencies\":false,\"kyc_parent\":null,\"kyc_partner\":null,\"kyc_person_with_high_post_in_state_owned_company\":null,\"kyc_phone_no\":\"0701122333\",\"kyc_registrered_partner\":null,\"kyc_risk_cover\":false,\"kyc_savings\":false,\"kyc_secondname\":\"Hansson\",\"kyc_special_administration\":false,\"kyc_street_address\":\"Gatuadressakt_bet316\",\"kyc_tax_identification_number\":null,\"kyc_the_head_of_state_or_government\":null,\"kyc_title\":null,\"kyc_transactions\":false,\"kyc_vice_and_deputy_minister\":null,\"kyc_wealth_administration\":false,\"kyc_work_phone_no\":null,\"kyc_zip_code\":\"74140\",\"last_name\":\"Hansson\",\"last_name_codebtor\":\"Medsok\",\"living_space\":\"45\",\"loan_amount\":2300000.0,\"market_value\":1000001.0,\"monthly_fee\":1005.0,\"monthly_gross_salary\":1122.0,\"monthly_gross_salary_codebtor\":1020.0,\"number_of_adults\":1,\"number_of_applicants\":2,\"number_of_children\":1,\"number_of_children_receiving_alimony\":0,\"number_of_other_properties\":1,\"number_of_rooms\":2,\"other_information\":\"Erbjudandet gäller t.o.m. 2012-12-12.\",\"other_propertieses\":[{\"assessment_value_other\":10000011,\"loan_amount_other\":10000012,\"market_value_other\":1.000001E+7,\"monthly_fee_other\":null,\"property_type_other\":\"VILLA\",\"yearly_fee_other\":10000013}],\"pay_alimony\":false,\"pay_alimony_codebtor\":false,\"phone_no\":\"0701122333\",\"phone_no_codebtor\":\"0701123112\",\"price_indication\":1.40,\"property_type\":\"BRF\",\"recieve_alimony\":false,\"street_address\":\"Gatuadressakt_bet316\",\"street_address_codebtor\":\"Ja\",\"street_property\":\"Gatuadressakt_bet316\",\"student_loan_amount\":100002.0,\"student_loan_amount_codebtor\":0.0,\"surety_for_someones_loans_amount\":0.0,\"tenant_corporate_number\":null,\"tenant_name\":\"Brf Test\",\"work_phone_no\":null,\"work_phone_no_codebtor\":null,\"zip_code\":\"74140\",\"zip_code_and_city_property\":\"74140 Knivsta\",\"zip_code_codebtor\":\"11612\"}",
                    LoanPostRequest.class);

            // Check all fields through reflection to be able to get useful output of failure
            Field[] fields = expectedRequest.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(loanPostRequest);
                Object expectedValue = field.get(expectedRequest);

                try {
                    assertThat(value).isEqualTo(expectedValue);
                } catch (ComparisonFailure e) {
                    System.out.print("Field name: " + field.getName());
                    throw e;
                }
            }
        }

        private GenericApplicationFieldGroup createMortgageSecurity() {
            GenericApplicationFieldGroup mortgageSecurity = createFieldGroup(
                    GenericApplicationFieldGroupNames.MORTGAGE_SECURITY);

            mortgageSecurity.putField(ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE, "1005");
            mortgageSecurity.putField(ApplicationFieldName.POSTAL_CODE, "74140");
            mortgageSecurity.putField(ApplicationFieldName.TOWN, "Knivsta");
            mortgageSecurity.putField(ApplicationFieldName.STREET_ADDRESS, "Gatuadressakt_bet316");
            mortgageSecurity.putField(ApplicationFieldName.ESTIMATED_MARKET_VALUE, "1000001");
            mortgageSecurity.putField(ApplicationFieldName.LIVING_AREA, "45");
            mortgageSecurity.putField(ApplicationFieldName.HOUSING_COMMUNITY_NAME, "Brf Test");
            mortgageSecurity.putField(ApplicationFieldName.PROPERTY_TYPE, "apartment");
            mortgageSecurity.putField(ApplicationFieldName.NUMBER_OF_ROOMS, "2");
            return mortgageSecurity;
        }

        private GenericApplicationFieldGroup createHouseHold() {
            GenericApplicationFieldGroup household = createFieldGroup(
                    GenericApplicationFieldGroupNames.HOUSEHOLD);
            household.putField(ApplicationFieldName.BAILMENT_AMOUNT, "0");
            household.putField(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_ALIMONY, "0");
            household.putField(ApplicationFieldName.NUMBER_OF_ADULTS, "1");
            household.putField(ApplicationFieldName.OTHER_LOANS_AMOUNT, "0");
            household.putField(ApplicationFieldName.DEFERRED_CAPITAL_GAINS_TAX_AMOUNT, "0");
            household.putField(ApplicationFieldName.NUMBER_OF_CHILDREN, "1");
            return household;
        }

        private GenericApplicationFieldGroup createProduct() {
            GenericApplicationFieldGroup currentMortgage = createFieldGroup(GenericApplicationFieldGroupNames.PRODUCT);
            currentMortgage.putField(ApplicationFieldName.FILTER_VERSION, "Unit test version 123");
            currentMortgage.putField(ApplicationFieldName.INTEREST_RATE, "0.0140");
            currentMortgage.putField(ApplicationFieldName.EXPIRATION_DATE, "2012-12-12");
            return currentMortgage;
        }
        
        private GenericApplicationFieldGroup createCurrentMortgage() {
            GenericApplicationFieldGroup currentMortgage = createFieldGroup(GenericApplicationFieldGroupNames.CURRENT_MORTGAGE);
            currentMortgage.putField(ApplicationFieldName.INTEREST_RATE, "0.021");
            currentMortgage.putField(ApplicationFieldName.AMOUNT, "2300000.0");
            currentMortgage.putField(ApplicationFieldName.LENDER, "Danske Bank");
            return currentMortgage;
        }

        private GenericApplicationFieldGroup createApplicants() {
            GenericApplicationFieldGroup applicants = createFieldGroup(GenericApplicationFieldGroupNames.APPLICANTS);
            applicants.addSubGroup(createApplicant());
            applicants.addSubGroup(createCoApplicant());
            return applicants;
        }

        private GenericApplicationFieldGroup createApplicant() {
            GenericApplicationFieldGroup applicant = createFieldGroup(GenericApplicationFieldGroupNames.APPLICANT);
            applicant.putField(ApplicationFieldName.PHONE_NUMBER, "0701122333");
            applicant.putField(ApplicationFieldName.COUNTRY, "SE");
            applicant.putField(ApplicationFieldName.NUMBER_OF_CHILDREN_RECEIVING_ALIMONY, "0");
            applicant.putField(ApplicationFieldName.TOWN, "Knivsta");
            applicant.putField(ApplicationFieldName.PAYING_ALIMONY_AMOUNT, "0");
            applicant.putField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, "Tink Ab");
            applicant.putField(ApplicationFieldName.PERSONAL_NUMBER, "198607015537");
            applicant.putField(ApplicationFieldName.TAXABLE_IN_USA, ApplicationFieldOptionValues.NO);
            applicant.putField(ApplicationFieldName.STUDENT_LOAN_AMOUNT, "100002");
            applicant.putField(ApplicationFieldName.IS_PEP, ApplicationFieldOptionValues.NO);
            applicant.putField(ApplicationFieldName.TAXABLE_IN_SWEDEN, ApplicationFieldOptionValues.YES);
            applicant.putField(ApplicationFieldName.MONTHLY_INCOME, "1122");
            applicant.putField(ApplicationFieldName.EMPLOYEE_SINCE, "2015-12");
            applicant.putField(ApplicationFieldName.BAILMENT_AMOUNT, "0");
            applicant.putField(ApplicationFieldName.POSTAL_CODE, "74140");
            applicant.putField(ApplicationFieldName.STREET_ADDRESS, "Gatuadressakt_bet316");
            applicant.putField(ApplicationFieldName.EMPLOYMENT_TYPE, ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT);
            applicant.putField(ApplicationFieldName.DEFERRED_CAPITAL_GAINS_TAX_AMOUNT, "1000004");
            applicant.putField(ApplicationFieldName.NAME, "Malin Hansson");
            applicant.putField(ApplicationFieldName.EMAIL, "daniel.lervik@tink.se");

            // FIXME: The createLoan() is already fetched from the user, so generation is probably wrong in main
            applicant.setSubGroups(Lists.newArrayList(
                    createLoan(),
                    createProperty(),
                    createSwedishTaxResidence(),
                    createFinnishTaxResidence()));

            return applicant;
        }

        private GenericApplicationFieldGroup createLoan() {
            GenericApplicationFieldGroup loan = createFieldGroup(GenericApplicationFieldGroupNames.LOAN);
            loan.putField(ApplicationFieldName.AMOUNT, "-2300000.0");
            loan.putField(ApplicationFieldName.LENDER, "Danske Bank");
            return loan;
        }

        private GenericApplicationFieldGroup createProperty() {
            GenericApplicationFieldGroup property = createFieldGroup(GenericApplicationFieldGroupNames.PROPERTY);
            property.putField(ApplicationFieldName.LOAN_AMOUNT, "10000012");
            property.putField(ApplicationFieldName.YEARLY_GROUND_RENT, "10000013");
            property.putField(ApplicationFieldName.ASSESSED_VALUE, "10000011");
            property.putField(ApplicationFieldName.MARKET_VALUE, "10000010");
            property.putField(ApplicationFieldName.TYPE, "house");
            return property;
        }

        private GenericApplicationFieldGroup createSwedishTaxResidence() {
            return createTaxResidence("SE");
        }

        private GenericApplicationFieldGroup createFinnishTaxResidence() {
            GenericApplicationFieldGroup fi = createTaxResidence("FI");
            fi.putField(ApplicationFieldName.TAXPAYER_IDENTIFICATION_NUMBER, "12313011");
            return fi;
        }

        private GenericApplicationFieldGroup createTaxResidence(String countryCode) {
            GenericApplicationFieldGroup swedishTaxResidence = createFieldGroup(
                    GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);

            swedishTaxResidence.putField(ApplicationFieldName.COUNTRY, countryCode);

            return swedishTaxResidence;
        }

        private GenericApplicationFieldGroup createCoApplicant() {
            GenericApplicationFieldGroup coApplicant = createFieldGroup(null); // Null in JSON when this was mapped
            coApplicant.putField(ApplicationFieldName.PHONE_NUMBER, "0701123112");
            coApplicant.putField(ApplicationFieldName.COUNTRY, "SE");
            coApplicant.putField(ApplicationFieldName.TOWN, "Stockholm");
            coApplicant.putField(ApplicationFieldName.PAYING_ALIMONY_AMOUNT, "0");
            coApplicant.putField(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, "Self employee company");
            coApplicant.putField(ApplicationFieldName.PERSONAL_NUMBER, "201212121212");
            coApplicant.putField(ApplicationFieldName.STUDENT_LOAN_AMOUNT, "0");
            coApplicant.putField(ApplicationFieldName.MONTHLY_INCOME, "1020");
            coApplicant.putField(ApplicationFieldName.EMPLOYEE_SINCE, "2010-10");
            coApplicant.putField(ApplicationFieldName.POSTAL_CODE, "11612");
            coApplicant.putField(ApplicationFieldName.RELATIONSHIP_STATUS, "cohabitant");
            coApplicant.putField(ApplicationFieldName.STREET_ADDRESS, "Ja"); // FIXME: Huh? This seems wrong
            coApplicant.putField(ApplicationFieldName.EMPLOYMENT_TYPE, ApplicationFieldOptionValues.SELF_EMPLOYED);
            coApplicant.putField(ApplicationFieldName.NAME, "Medsok Medsok");
            coApplicant.putField(ApplicationFieldName.EMAIL, "daniel.lervik_1@tink.se");
            return coApplicant;
        }

        private GenericApplicationFieldGroup createFieldGroup(String fieldGroupName) {
            GenericApplicationFieldGroup fieldGroup = new GenericApplicationFieldGroup();
            fieldGroup.setName(fieldGroupName);
            return fieldGroup;
        }
    }

    public static class Name {
        @Test(expected = IllegalArgumentException.class)
        public void nullNameThrows() {
            new ApplicationToLoanPostRequestMapperImpl.NameToNames(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void emptyNameThrows() {
            new ApplicationToLoanPostRequestMapperImpl.NameToNames("");
        }

        @Test(expected = IllegalArgumentException.class)
        public void onlyFirstOrLastThrows() {
            new ApplicationToLoanPostRequestMapperImpl.NameToNames("Daniel");
        }

        @Test
        public void singleFirstAndLastName() {
            ApplicationToLoanPostRequestMapperImpl.NameToNames nameToNames = new ApplicationToLoanPostRequestMapperImpl.NameToNames(
                    "Daniel-Daniel Lervik");

            assertThat(nameToNames.getFirstName()).isEqualTo("Daniel-Daniel");
            assertThat(nameToNames.getAllFirstNames()).isEqualTo("Daniel-Daniel");
            assertThat(nameToNames.getMiddleNames().orElse(null)).isEqualTo(null);
            assertThat(nameToNames.getLastName()).isEqualTo("Lervik");
        }

        @Test
        public void multipleFirstAndOneLastName() {
            ApplicationToLoanPostRequestMapperImpl.NameToNames nameToNames = new ApplicationToLoanPostRequestMapperImpl.NameToNames(
                    "Daniel Middle-Name Another Lervik");

            assertThat(nameToNames.getFirstName()).isEqualTo("Daniel");
            assertThat(nameToNames.getAllFirstNames()).isEqualTo("Daniel Middle-Name Another");
            assertThat(nameToNames.getMiddleNames().orElse(null)).isEqualTo("Middle-Name Another");
            assertThat(nameToNames.getLastName()).isEqualTo("Lervik");
        }
    }
}
