package se.tink.backend.product.execution.integration.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.UUID;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.product.execution.model.User;
import se.tink.libraries.application.ApplicationFieldOptionValues;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.libraries.uuid.UUIDUtils;

public class ApplicationFactory {
    // Test social security numbers given by SBAB for their test environment.
    public static final String SSN_SUNE_SKOG = "196705191390";
    public static final String SSN_MAUD_LIDEN = "196609159808";
    public static final String SSN_JADWIGA_JENSEN = "196610086487";
    public static final String SSN_CARL_JONSSON = "196604264918";
    public static final String SSN_LENNART_BLAD = "194701230221";

    // Data needed by application
    public static final String USER_LOCALE = "sv-se";
    public static final String USER_DEVICE_ID = UUIDUtils.generateUUID();
    public static final String CREDENTIAL_ID = "76e7e841040242448aad31a4d5e0c2c5";
    public static final String USER_ID = "c616a6be3db24471b9d5757188b59beb";

    public static GenericApplication createApplication(ApplicationType type, String ssn) {
        GenericApplication application = new GenericApplication();
        application.setApplicationId(UUID.randomUUID());
        application.setType(type);
        application.setCredentialsId(UUIDUtils.fromTinkUUID(CREDENTIAL_ID));
        application.setUserId(UUIDUtils.fromTinkUUID(USER_ID));
        application.setPersonalNumber(ssn);
        return application;
    }

    public static GenericApplicationFieldGroup createSwitchMortageProviderApplicantsGroup(String ssn) {
        GenericApplicationFieldGroup group = new GenericApplicationFieldGroup();
        group.setName(GenericApplicationFieldGroupNames.APPLICANTS);
        GenericApplicationFieldGroup applicant;

        switch (ssn) {
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

    public static GenericApplicationFieldGroup createApplicant(String firstName, String lastName, String monthlyIncome,
            String ssn) {
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

    public static Credentials createCredentials(String ssn) {
        Credentials credentials = new Credentials();
        credentials.setId(CREDENTIAL_ID);
        credentials.setUserId(USER_ID);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);
        credentials.setUsername(ssn);
        credentials.setPassword(null);

        return credentials;
    }

    public static User createUser() {
        return new User(UUIDUtils.fromTinkUUID(USER_ID), USER_LOCALE, USER_DEVICE_ID);
    }

    public static ArrayList<GenericApplicationFieldGroup> createFieldGroupsForApartment(String ssn) {
        return Lists.newArrayList(
                createSwitchMortageProviderApplicantsGroup(ssn),
                createMortgageApartmentSecurityGroup(),
                createHouseholdGroup(),
                createCurrentMortgageGroup(),
                createProductGroup());
    }

    public static ArrayList<GenericApplicationFieldGroup> createFieldGroupsForHouse(String ssn) {
        return Lists.newArrayList(
                createSwitchMortageProviderApplicantsGroup(ssn),
                createMortgageHouseSecurityGroup(),
                createHouseholdGroup(),
                createCurrentMortgageGroup(),
                createProductGroup());
    }
}
