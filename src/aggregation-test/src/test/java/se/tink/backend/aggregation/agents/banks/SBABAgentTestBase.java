package se.tink.backend.aggregation.agents.banks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Amount;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.date.DateUtils;

public class SBABAgentTestBase {

    // Test social security numbers given by SBAB for their test environment.
    public static final String SSN_SUNE_SKOG = "196705191390";
    public static final String SSN_MAUD_LIDEN = "196609159808";
    public static final String SSN_JADWIGA_JENSEN = "196610086487";
    public static final String SSN_CARL_JONSSON = "196604264918";
    public static final String SSN_LENNART_BLAD = "194701230221";

    public static GenericApplication createApplication(ApplicationType type) {
        GenericApplication application = new GenericApplication();
        application.setType(type);
        return application;
    }

    public static GenericApplicationFieldGroup createTaxResidenceGroup() {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.RESIDENCE_FOR_TAX_PURPOSES);

        GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
        subGroup.setFields(Maps.newHashMap(ImmutableMap.of("country-code", "SE")));
        group.setSubGroups(Lists.newArrayList(subGroup));

        return group;
    }

    public static GenericApplicationFieldGroup createCitizenshipsGroup() {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.CITIZENSHIPS);

        GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();
        subGroup.setFields(Maps.newHashMap(ImmutableMap.of("country-code", "SE")));
        group.setSubGroups(Lists.newArrayList(subGroup));

        return group;
    }

    public static GenericApplicationFieldGroup createOpenSavingsAccountApplicantsGroup() {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.APPLICANTS);
        GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();

        subGroup.setFields(ImmutableMap.<String, String>builder()
                .put(ApplicationFieldName.PERSONAL_NUMBER, "")
                .put(ApplicationFieldName.EMAIL, "")
                .put(ApplicationFieldName.PHONE_NUMBER, "")
                .build()
        );

        group.setSubGroups(Lists.newArrayList(subGroup));

        return group;
    }

    public static GenericApplicationFieldGroup createMortgageApartmentSecurityGroup() {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.MORTGAGE_SECURITY);

        group.setFields(ImmutableMap.<String, String>builder()
                .put(ApplicationFieldName.ESTIMATED_MARKET_VALUE, "2000000")
                .put(ApplicationFieldName.HOUSING_COMMUNITY_NAME, "Hsb:s Brf Arkitekten")
                .put(ApplicationFieldName.MUNICIPALITY, "0180") // Stockholm
                .put(ApplicationFieldName.LIVING_AREA, "100")
                .put(ApplicationFieldName.MONTHLY_HOUSING_COMMUNITY_FEE, "2544")
                .put(ApplicationFieldName.NUMBER_OF_ROOMS, "3")
                .put(ApplicationFieldName.POSTAL_CODE, "65115")
                .put(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT)
                .put(ApplicationFieldName.STREET_ADDRESS, "Box 1012")
                .put(ApplicationFieldName.TOWN, "Karlstad")
                .build());

        return group;
    }

    public static GenericApplicationFieldGroup createMortgageHouseSecurityGroup() {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.MORTGAGE_SECURITY);

        group.setFields(ImmutableMap.<String, String>builder()
                .put(ApplicationFieldName.ASSESSED_VALUE, "2200000")
                .put(ApplicationFieldName.ESTIMATED_MARKET_VALUE, "2200000")
                .put(ApplicationFieldName.LIVING_AREA, "75")
                .put(ApplicationFieldName.PROPERTY_TYPE, ApplicationFieldOptionValues.HOUSE)
                .build());

        return group;
    }

    public static GenericApplicationFieldGroup createHouseholdGroup() {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.HOUSEHOLD);
        return group;
    }
    
    public static GenericApplicationFieldGroup createCurrentMortgageGroup() {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.CURRENT_MORTGAGE);
        
        group.putField(ApplicationFieldName.LENDER, "SEB");
        group.putField(ApplicationFieldName.AMOUNT, "1000000");
        group.putField(ApplicationFieldName.INTEREST_RATE, "0.0160");
        return group;
    }
    
    public static GenericApplicationFieldGroup createProductGroup() {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.PRODUCT);
        
        group.putField(ApplicationFieldName.INTEREST_RATE, "0.0140");
                
        return group;
    }

    public static GenericApplicationFieldGroup createSwitchMortageProviderApplicantsGroup(String ssn) {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.APPLICANTS);
        GenericApplicationFieldGroup applicant;

        switch(ssn) {
        case SSN_CARL_JONSSON:
            applicant = createApplicant("Carl", "Jonsson", "10000", SSN_CARL_JONSSON);
            break;
        case SSN_JADWIGA_JENSEN:
            applicant = createApplicant("Jadwiga", "Jensen", "900000", SSN_JADWIGA_JENSEN);
            break;
        case SSN_MAUD_LIDEN:
            applicant = createApplicant("Maud", "Lidén", "850000", SSN_MAUD_LIDEN);
            break;
        case SSN_SUNE_SKOG:
            applicant = createApplicant("Sune", "Skog", "35000", SSN_SUNE_SKOG);
            break;
        case SSN_LENNART_BLAD:
            applicant = createApplicant("Lennart", "Blad", "27000", SSN_LENNART_BLAD);
            break;
        default:
            throw new RuntimeException("Unknown test SSN");
        }

        group.setSubGroups(Lists.newArrayList(applicant));

        return group;
    }

    private static GenericApplicationFieldGroup createApplicant(String firstName, String lastName, String monthlyIncome, String ssn) {
        GenericApplicationFieldGroup subGroup = new GenericApplicationFieldGroup();

        subGroup.setFields(ImmutableMap.<String, String>builder()
                // General
                .put(ApplicationFieldName.FIRST_NAME, firstName)
                .put(ApplicationFieldName.LAST_NAME, lastName)
                .put(ApplicationFieldName.PERSONAL_NUMBER, ssn)
                .put(ApplicationFieldName.RELATIONSHIP_STATUS, ApplicationFieldOptionValues.SINGLE)
                
                // Occupation
                .put(ApplicationFieldName.EMPLOYER_OR_COMPANY_NAME, "Tink")
                .put(ApplicationFieldName.EMPLOYMENT_TYPE, ApplicationFieldOptionValues.PERMANENT_EMPLOYMENT)
                .put(ApplicationFieldName.PROFESSION, "Snickare")
                .put(ApplicationFieldName.EMPLOYEE_SINCE, "2012-12")
                
                // Residence
                .put(ApplicationFieldName.RESIDENCE_PROPERTY_TYPE, ApplicationFieldOptionValues.APARTMENT)
                .put(ApplicationFieldName.STREET_ADDRESS, "Drottinggatan 94")
                .put(ApplicationFieldName.POSTAL_CODE, "11136")
                .put(ApplicationFieldName.TOWN, "Stockholm")
                .put(ApplicationFieldName.MUNICIPALITY, "0180")
                .put(ApplicationFieldName.COUNTRY, "SE")
                
                // Other contact information
                .put(ApplicationFieldName.EMAIL, "test@tink.se")
                .put(ApplicationFieldName.PHONE_NUMBER, "070-0000000")
                
                // Financial situation
                .put(ApplicationFieldName.MONTHLY_INCOME, monthlyIncome)
                .put(ApplicationFieldName.STUDENT_LOAN_MONTHLY_COST, "700")
                
                .build()
        );

        return subGroup;
    }

    public static GenericApplicationFieldGroup createKYCGroup() {
        return new SbabKycGroupBuilder()
                .setPep(false)
                .setMonthlyIncome(28000)
                .setSavingsPurpose(ApplicationFieldOptionValues.PURPOSE_FINANCIAL_BUFFER)
                .setSavingsSourcesReason(ApplicationFieldOptionValues.GIFT_OR_INHERITANCE)
                .setInitiaDeposit(230000)
                .setSavingsFrequencyPerMonth(0)
                .setSavingsAmountPerMonth(2000)
                .setPersonsSaving(ApplicationFieldOptionValues.PERSONS_SAVING_ME)
                .setSavingsSources(ApplicationFieldOptionValues.MY_ACCOUNT_IN_SWEDISH_BANK)
                .setMoneyWithdrawalsPerMonth(0)
                .setSavingsSourceAccountInSwedishBank(ApplicationFieldOptionValues.SWEDBANK)
                .build();
    }

    public static Transfer create1SEKTransfer() {
        Transfer transfer = new Transfer();

        transfer.setAmount(Amount.inSEK(1.0));
        transfer.setDestinationMessage("Tink dest");
        transfer.setSourceMessage("Tink source");
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(DateUtils.getToday());

        return transfer;
    }

    protected static class SbabKycGroupBuilder {
        private final Map<String, String> fields = Maps.newHashMap();

        public SbabKycGroupBuilder setPep(boolean isPep) {
            String value = isPep ? ApplicationFieldOptionValues.YES : ApplicationFieldOptionValues.NO;
            fields.put(ApplicationFieldName.IS_PEP, value);
            return this;
        }

        public SbabKycGroupBuilder setMonthlyIncome(int monthlyIncome) {
            String value = ApplicationFieldOptionValues.LESS_THAN_20000;

            if (monthlyIncome > 70000) {
                value = ApplicationFieldOptionValues.MORE_THAN_70000;
            } else if (monthlyIncome > 50000) {
                value = ApplicationFieldOptionValues.BETWEEN_50000_AND_70000;
            } else if (monthlyIncome > 35000) {
                value = ApplicationFieldOptionValues.BETWEEN_35000_AND_50000;
            } else if (monthlyIncome > 20000) {
                value = ApplicationFieldOptionValues.BETWEEN_20000_AND_35000;
            }

            fields.put(ApplicationFieldName.SBAB_SAVINGS_MONTHLY_INCOME, value);
            return this;
        }

        public SbabKycGroupBuilder setSavingsPurpose(String... savingsPurposes) {
            fields.put(ApplicationFieldName.SBAB_SAVINGS_PURPOSE, multiSelectAnswer(savingsPurposes));
            return this;
        }

        public SbabKycGroupBuilder setSavingsSourcesReason(String reasons) {
            fields.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_REASON, multiSelectAnswer(reasons));
            return this;
        }

        public SbabKycGroupBuilder setInitiaDeposit(int deposit) {
            String value = ApplicationFieldOptionValues.LESS_THAN_50000;

            if (deposit > 350000) {
                value = ApplicationFieldOptionValues.MORE_THAN_350000;
            } else if (deposit > 250000) {
                value = ApplicationFieldOptionValues.BETWEEN_250000_AND_350000;
            } else if (deposit > 150000) {
                value = ApplicationFieldOptionValues.BETWEEN_150000_AND_250000;
            } else if (deposit > 50000) {
                value = ApplicationFieldOptionValues.BETWEEN_50000_AND_150000;
            }

            fields.put(ApplicationFieldName.SBAB_INITIAL_DEPOSIT, value);
            return this;
        }

        public SbabKycGroupBuilder setSavingsFrequencyPerMonth(int frequencyPerMonth) {
            fields.put(ApplicationFieldName.SBAB_SAVINGS_FREQUENCY, frequencyPerMonth(frequencyPerMonth));
            return this;
        }

        public SbabKycGroupBuilder setSavingsAmountPerMonth(int savingsAmountPerMonth) {
            String value = ApplicationFieldOptionValues.LESS_THAN_10000;

            if (savingsAmountPerMonth > 40000) {
                value = ApplicationFieldOptionValues.MORE_THAN_40000;
            } else if (savingsAmountPerMonth > 30000) {
                value = ApplicationFieldOptionValues.BETWEEN_30000_AND_40000;
            } else if (savingsAmountPerMonth > 20000) {
                value = ApplicationFieldOptionValues.BETWEEN_20000_AND_30000;
            } else if (savingsAmountPerMonth > 10000) {
                value = ApplicationFieldOptionValues.BETWEEN_10000_AND_20000;
            }

            fields.put(ApplicationFieldName.SBAB_SAVINGS_AMOUNT_PER_MONTH, value);
            return this;
        }

        public SbabKycGroupBuilder setPersonsSaving(String... personsSaving) {
            fields.put(ApplicationFieldName.SBAB_PERSONS_SAVING, multiSelectAnswer(personsSaving));
            return this;
        }

        public SbabKycGroupBuilder setSavingsSources(String... savingsSources) {
            fields.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES, multiSelectAnswer(savingsSources));
            return this;
        }

        public SbabKycGroupBuilder setMoneyWithdrawalsPerMonth(int withdrawalsPerMonth) {
            fields.put(ApplicationFieldName.SBAB_MONEY_WITHDRAWAL, frequencyPerMonth(withdrawalsPerMonth));
            return this;
        }

        public SbabKycGroupBuilder setSavingsSourceAccountInSwedishBank(String... banks) {
            fields.put(ApplicationFieldName.SBAB_SAVINGS_SOURCES_MY_ACCOUNT_IN_SWEDISH_BANK, multiSelectAnswer(banks));
            return this;
        }

        public GenericApplicationFieldGroup build() {
            if (fields.size() < 11) {
                throw new IllegalStateException("All KYC application fields have to be populated");
            }

            GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();

            group.setName(GenericApplicationFieldGroupNames.KNOW_YOUR_CUSTOMER);
            group.setFields(fields);

            group.setSubGroups(Lists.newArrayList(createCitizenshipsGroup(), createTaxResidenceGroup()));

            return group;
        }

        private static String frequencyPerMonth(int frequency) {
            if (frequency > 10) {
                return ApplicationFieldOptionValues.MORE_THAN_TEN_TIMES_A_MONTH;
            } else if (frequency > 5) {
                return ApplicationFieldOptionValues.SIX_TO_TEN_TIMES_A_MONTH;
            } else if (frequency > 0) {
                return ApplicationFieldOptionValues.ONE_TO_FIVE_TIMES_A_MONTH;
            }

            return ApplicationFieldOptionValues.LESS_THAN_ONCE_A_MONTH;
        }

        private static String multiSelectAnswer(String... answers) {
            return SerializationUtils.serializeToString(Lists.newArrayList(answers));
        }
    }
}
