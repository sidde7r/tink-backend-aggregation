package se.tink.backend.core.enums;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.DeletedUserStatus;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.FraudTypes;
import se.tink.backend.core.Instrument;
import se.tink.backend.core.MarketStatus;
import se.tink.backend.core.MerchantSources;
import se.tink.backend.core.NotificationStatus;
import se.tink.backend.core.Portfolio;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.core.ProviderTypes;
import se.tink.backend.core.SessionTypes;
import se.tink.backend.core.SubscriptionType;
import se.tink.backend.core.UserConnectedServiceStates;
import se.tink.backend.core.UserDeviceStatuses;
import se.tink.backend.core.UserEventTypes;
import se.tink.backend.core.auth.AuthenticationSource;
import se.tink.backend.core.auth.AuthenticationStatus;
import se.tink.backend.core.auth.UserPublicKeyType;
import se.tink.backend.core.auth.bankid.BankIdAuthenticationStatus;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.backend.core.property.PropertyStatus;
import se.tink.backend.core.property.PropertyType;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.rpc.DataExportRequestStatus;
import se.tink.backend.rpc.Feedback;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.auth.ChallengeStatus;
import se.tink.libraries.date.ResolutionTypes;

public class BackwardsCompatibleChangesOfEnumsInDatabaseTest {

    private static Map<Class<?>, Set<String>> enumsInBackend;
    private static final ImmutableMap<Class<?>, Set<String>> ENUMS_IN_DATABASE =
            ImmutableMap.<Class<?>, Set<String>>builder()
                    .put(AccountTypes.class, ImmutableSet.of("DUMMY", "CREDIT_CARD", "PENSION", "CHECKING", "MORTGAGE",
                            "INVESTMENT", "SAVINGS", "EXTERNAL", "LOAN", "OTHER"))
                    .put(AuthenticationStatus.class,
                            ImmutableSet.of("AUTHENTICATED", "AUTHENTICATION_ERROR", "NO_USER", "USER_BLOCKED",
                                    "AUTHENTICATED_UNAUTHORIZED_DEVICE", "AUTHENTICATION_ERROR_UNAUTHORIZED_DEVICE"))
                    .put(AuthenticationSource.class, ImmutableSet.of("TOUCHID", "FINGERPRINT", "FACEID"))
                    .put(AuthenticationMethod.class,
                            ImmutableSet.of("BANKID", "EMAIL_AND_PASSWORD", "NON_VALID", "SMS_OTP_AND_PIN6",
                                    "PHONE_NUMBER_AND_PIN6", "ABN_AMRO_PIN5", "CHALLENGE_RESPONSE"))
                    .put(CategoryTypes.class, ImmutableSet.of("TRANSFERS", "INCOME", "EXPENSES"))
                    .put(ChallengeStatus.class, ImmutableSet.of("EXPIRED", "CONSUMED", "INVALID", "VALID"))
                    .put(CredentialsStatus.class, ImmutableSet.of("AUTHENTICATING", "UPDATING", "TEMPORARY_ERROR",
                            "AUTHENTICATION_ERROR", "AWAITING_MOBILE_BANKID_AUTHENTICATION", "CREATED", "DISABLED",
                            "UNCHANGED", "NOT_IMPLEMENTED_ERROR", "AWAITING_SUPPLEMENTAL_INFORMATION",
                            "PERMANENT_ERROR", "AWAITING_THIRD_PARTY_APP_AUTHENTICATION",
                            "DELETED", "UPDATED", "METRIC", "AWAITING_OTHER_CREDENTIALS_TYPE", "HINTED"))
                    .put(CredentialsTypes.class, ImmutableSet.of("KEYFOB", "MOBILE_BANKID", "FRAUD", "PASSWORD"))
                    .put(Feedback.ObjectTypes.class, ImmutableSet.of("ACTIVITY"))
                    .put(FollowTypes.class, ImmutableSet.of("SAVINGS", "SEARCH", "EXPENSES"))
                    .put(FraudDetailsContentType.class, ImmutableSet.of("NON_PAYMENT", "FREQUENT_ACCOUNT_ACTIVITY",
                            "DOUBLE_CHARGE", "REAL_ESTATE_ENGAGEMENT", "ADDRESS", "SCORING", "LARGE_EXPENSE",
                            "INCOME", "LARGE_WITHDRAWAL", "IDENTITY", "COMPANY_ENGAGEMENT", "CREDITS"))
                    .put(FraudStatus.class, ImmutableSet.of("CRITICAL", "SEEN", "EMPTY", "WARNING", "OK", "FRAUDULENT"))
                    .put(FraudTypes.class, ImmutableSet.of("TRANSACTION", "INQUIRY", "CREDIT", "IDENTITY"))
                    .put(MarketStatus.class, ImmutableSet.of("BETA", "ENABLED", "DISABLED"))
                    .put(MerchantSources.class, ImmutableSet.of("GOOGLE", "FILE", "MANUALLY"))
                    .put(NotificationStatus.class,
                            ImmutableSet.of("SENT", "CREATED", "READ", "RECEIVED", "SENT_ENCRYPTED"))
                    .put(ProviderStatuses.class,
                            ImmutableSet.of("OBSOLETE", "TEMPORARY_DISABLED", "ENABLED", "DISABLED"))
                    .put(ProviderTypes.class,
                            ImmutableSet.of("CREDIT_CARD", "TEST", "FRAUD", "BANK", "BROKER", "OTHER"))
                    .put(ResolutionTypes.class, ImmutableSet.of("MONTHLY_ADJUSTED", "MONTHLY", "DAILY", "WEEKLY", "ALL",
                            "YEARLY"))
                    .put(SessionTypes.class, ImmutableSet.of("OAUTH", "MOBILE", "LINK", "WEB"))
                    .put(SubscriptionType.class, ImmutableSet.of("FAILING_CREDENTIALS_EMAIL", "MONTHLY_SUMMARY_EMAIL",
                            "ROOT", "PRODUCT_UPDATES"))
                    .put(UserConnectedServiceStates.class, ImmutableSet.of("ACTIVE", "INACTIVE"))
                    .put(UserDeviceStatuses.class, ImmutableSet.of("UNAUTHORIZED", "AUTHORIZED",
                            "AWAITING_BANKID_AUTHENTICATION", "AWAITING_SUPPLEMENTAL_INFORAMTION"))
                    .put(UserEventTypes.class, ImmutableSet.of("CREATED", "PASSWORD_RESET", "BLOCKED",
                            "AUTHENTICATION_ERROR", "PIN6_CHANGED", "PASSWORD_CHANGED", "DELETED",
                            "AUTHENTICATION_SUCCESSFUL", "PIN6_RESET"))
                    .put(UserPublicKeyType.class, ImmutableSet.of("RSA", "ECDSA"))
                    .put(BankIdAuthenticationStatus.class, ImmutableSet.of("AUTHENTICATED", "AUTHENTICATION_ERROR",
                            "AWAITING_BANKID_AUTHENTICATION", "NO_USER"))
                    .put(SignableOperation.StatusDetailsKey.class, ImmutableSet.of("BANKID_FAILED", "INVALID_INPUT",
                            "TECHNICAL_ERROR", "USER_VALIDATION_ERROR"))
                    .put(PropertyStatus.class, ImmutableSet.of("ACTIVE", "EXPIRED"))
                    .put(PropertyType.class, ImmutableSet.of("HOUSE", "APARTMENT", "VACATION_HOUSE"))
                    .put(RateThisAppStatus.class, ImmutableSet
                            .of("SENT", "NOT_SENT", "USER_CLICKED_IGNORE",
                                    "USER_CLICKED_RATE_IN_STORE"))
                    .put(Instrument.Type.class, ImmutableSet.of("FUND", "STOCK", "OTHER"))
                    .put(Portfolio.Type.class, ImmutableSet.of("ISK", "KF", "DEPOT", "PENSION", "OTHER"))
                    .put(DataExportRequestStatus.class, ImmutableSet.of("CREATED", "IN_PROGRESS", "FAILED", "COMPLETED"))
                    .put(DeletedUserStatus.class, ImmutableSet.of("IN_PROGRESS", "COMPLETED", "FAILED"))
                    .build();

    private static boolean hasTableAnnotation(Class<?> clazz) {
        boolean isPresent = false;
        if (clazz.isAnnotationPresent(org.springframework.data.cassandra.mapping.Table.class)) {
            isPresent = true;
        }
        else if (clazz.isAnnotationPresent(javax.persistence.Table.class)) {
            isPresent = true;
        }

        return isPresent;
    }

    private static Set<Field> getAllEnumsFromClass(Class<?> clazz) {
        Set<Field> allEnumFields = Sets.newHashSet();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType().isEnum()) {
                allEnumFields.add(field);
            }
        }
        return allEnumFields;
    }

    private static Set<Class<? extends Object>> getAllClassesInTinkBackendCoreAndRpc(List<ClassLoader> classLoadersList)
    {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(
                        new FilterBuilder().include(FilterBuilder.prefix("se.tink.backend.core")).include(
                                FilterBuilder.prefix("se.tink.backend.rpc"))));

        return reflections.getSubTypesOf(Object.class);
    }

    @BeforeClass
    public static void setUp() throws NoSuchFieldException {

        // Create a hash of all available Enums in backend

        List<Class<?>> classesWithTablePresent = Lists.newArrayList();
        Set<Field> allPresentEnumFields = Sets.newHashSet();
        enumsInBackend = Maps.newHashMap();


        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Set<Class<? extends Object>> allClasses = getAllClassesInTinkBackendCoreAndRpc(classLoadersList);

        System.out.println("allClasses.size() = " + allClasses.size());

        for (Class<? extends Object> clazz : allClasses) {
            if (hasTableAnnotation(clazz)) {
                classesWithTablePresent.add(clazz);
            }
        }

        System.out.println("classesWithTablePresent.size() = " + classesWithTablePresent.size());

        for (Class<? extends Object> clazz : classesWithTablePresent) {
            Set<Field> allTheEnums = getAllEnumsFromClass(clazz);
            allPresentEnumFields.addAll(allTheEnums);
        }


        // Since some Enums are present as fields in multiple classes the size of allPresentEnumFields
        // is larger than the enumsInBackend.

        System.out.println("allPresentEnumFields.size() = " + allPresentEnumFields.size());

        for (Field field : allPresentEnumFields) {
            Class<?> fieldClass = field.getType();
            Set<String> theEnumConstants = Sets.newHashSet();

            for (Object enumConstant : fieldClass.getEnumConstants()) {
                String enumConstantValue = ((Enum) enumConstant).name();
                theEnumConstants.add(enumConstantValue);
            }

            enumsInBackend.put(fieldClass, theEnumConstants);
        }


        System.out.println("enumsInBackend.size() = " + enumsInBackend.size());
    }

    private Map<Class<?>, Set<String>> getValuesFromDifferenceOfEnumMaps(Map<Class<?>,
            MapDifference.ValueDifference<Set<String>>> classValueDifferenceMap) {

        Map<Class<?>, Set<String>> differenceOfEnumMaps = Maps.newHashMap();

        for (Class<?> clazz : classValueDifferenceMap.keySet()) {
            MapDifference.ValueDifference<Set<String>> setValueDifference = classValueDifferenceMap.get(clazz);
            Set<String> theDifference = getValuesPresentOnlyInOneSet(setValueDifference);
            differenceOfEnumMaps.put(clazz, theDifference);
        }

        return differenceOfEnumMaps;
    }

    private Set<String> getValuesPresentOnlyInOneSet(MapDifference.ValueDifference<Set<String>> setValueDifference) {
        Set<String> a = setValueDifference.leftValue();
        Set<String> b = setValueDifference.rightValue();

        return Sets.difference(Sets.union(a,b), Sets.intersection(a,b));
    }

    @Test
    public void testEnumValuesAgainstValuesInDatabase() throws ClassNotFoundException {

        Assert.assertNotNull("Warning, no enums in backend. This could cause database issues.", enumsInBackend);

        MapDifference<Class<?>, Set<String>> differenceBetweenEnumMaps =
                Maps.difference(enumsInBackend, ENUMS_IN_DATABASE);

        Assert.assertTrue("There are more classes in the test than in the database. Please add the following classes "
                        + "to ENUMS_IN_DATABASE: " + differenceBetweenEnumMaps.entriesOnlyOnLeft().keySet().toString(),
                differenceBetweenEnumMaps.entriesOnlyOnLeft().isEmpty());
        Assert.assertTrue("There are fewer classes in the test than in the database. Please make sure the database no "
                        + "longer contains the following enums (or migrate away from it) and then remove them from "
                        + "ENUMS_IN_DATABASE. Enums: " + differenceBetweenEnumMaps.entriesOnlyOnRight().keySet()
                        .toString(),
                differenceBetweenEnumMaps.entriesOnlyOnRight().isEmpty());
        Assert.assertTrue("Backwards compatibility might be broken. Please check if the following values have been "
                + "added, are missing or have been changed: "
                + getValuesFromDifferenceOfEnumMaps(differenceBetweenEnumMaps.entriesDiffering()).toString(),
                differenceBetweenEnumMaps.entriesDiffering().isEmpty());

    }
}
