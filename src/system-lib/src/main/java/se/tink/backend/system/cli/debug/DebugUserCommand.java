package se.tink.backend.system.cli.debug;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.data.domain.PageRequest;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.ProductDAO;
import se.tink.backend.common.mail.SubscriptionHelper;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.cassandra.ProductTemplateRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.ApplicationRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.DeviceRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionRepository;
import se.tink.backend.common.repository.mysql.main.SubscriptionTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.repository.mysql.main.UserEventRepository;
import se.tink.backend.common.repository.mysql.main.UserFacebookProfileRepository;
import se.tink.backend.common.repository.mysql.main.UserForgotPasswordTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.ApplicationRow;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.Device;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Subscription;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserEvent;
import se.tink.backend.core.UserFacebookProfile;
import se.tink.backend.core.UserForgotPasswordToken;
import se.tink.backend.core.follow.ExpensesFollowCriteria;
import se.tink.backend.core.follow.SavingsFollowCriteria;
import se.tink.backend.core.product.ProductInstance;
import se.tink.backend.core.product.ProductTemplate;
import se.tink.backend.system.cli.CliPrintUtils;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class DebugUserCommand extends ServiceContextCommand<ServiceConfiguration> {
    private ObjectMapper mapper = new ObjectMapper();

    public DebugUserCommand() {
        super("debug-user", "Dump debug information for a user.");
    }

    private void printAccounts(AccountRepository accountsRepository, String userId) {
        List<Account> accounts = accountsRepository.findByUserId(userId);
        List<Map<String, String>> output = Lists.transform(accounts, input -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("id", input.getId());
            data.put("credentialsid", input.getCredentialsId());
            data.put("accountnumber", input.getAccountNumber());
            data.put("bankid", input.getBankId());
            data.put("name", input.getName());
            data.put("type", input.getType().toString());
            data.put("excluded", Boolean.toString(input.isExcluded()));
            data.put("closed", Boolean.toString(input.isClosed()));
            data.put("favored", Boolean.toString(input.isFavored()));
            data.put("ownership", Double.toString(input.getOwnership()));
            data.put("payload", String.valueOf(input.getPayload()));
            data.put("certaindate", String.valueOf(input.getCertainDate()));
            return data;
        });
        CliPrintUtils.printTable(output);
    }

    private void printLoans(AccountRepository accountRepository, LoanDataRepository loanDataRepository, String userId) {
        FluentIterable<Account> loanAccounts = FluentIterable.from(accountRepository.findByUserId(userId)).filter(
                account -> Objects.equals(account.getType(), AccountTypes.LOAN));

        List<Map<String, String>> output = Lists.newArrayList();

        for (Account account : loanAccounts) {
            List<Loan> threeMostRecentLoans = loanDataRepository.findMostRecentByAccountId(UUIDUtils.fromTinkUUID(account.getId()), 3);

            output.addAll(Lists.transform(threeMostRecentLoans, input -> {
                Map<String, String> data = Maps.newLinkedHashMap();
                data.put("id", String.valueOf(UUIDUtils.toTinkUUID(input.getId())));
                data.put("accountid", String.valueOf(UUIDUtils.toTinkUUID(input.getAccountId())));
                data.put("credentialsid", String.valueOf(UUIDUtils.toTinkUUID(input.getCredentialsId())));
                data.put("name", input.getName());
                data.put("interest", String.valueOf(input.getInterest()));
                data.put("type", input.getType().name());
                data.put("updated", String.valueOf(input.getUpdated()));
                return data;
            }));
        }
        CliPrintUtils.printTable(output);
    }

    private void printLastCredentialsEvents(CredentialsEventRepository credentialsEventRepository,
            CredentialsRepository credentialsRepository, String userId) {
        List<Credentials> credentials = credentialsRepository.findAllByUserId(userId);
        for (Credentials credential : credentials) {

            List<CredentialsEvent> credentialsEvents = credentialsEventRepository
                    .findMostRecentByUserIdAndCredentialsId(credential.getUserId(), credential.getId(), 20);
            List<Map<String, String>> output = Lists.newArrayList();

            output.addAll(Lists.transform(credentialsEvents, input -> {
                Map<String, String> data = Maps.newLinkedHashMap();
                data.put("credentialsid", String.valueOf(UUIDUtils.toTinkUUID(input.getCredentialsId())));
                data.put("providername", input.getProviderName());
                data.put("status", String.valueOf(input.getStatus()));
                data.put("timestamps", String.valueOf(input.getTimestamp()));
                data.put("message", input.getMessage());
                return data;
            }));
            System.out.println();
            System.out.println("Credentials events for credentials " + credential.getId());
            CliPrintUtils.printTable(output);
        }
    }

    private void printCredentials(CredentialsRepository credentialsRepository,
            String userId) {
        List<Credentials> devices = credentialsRepository.findAllByUserId(userId);
        List<Map<String, String>> output = Lists.transform(devices, input -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("id", input.getId());
            data.put("providername", input.getProviderName());
            data.put("status", String.valueOf(input.getStatus()));
            data.put("type", String.valueOf(input.getType()));
            data.put("fields", input.getFieldsSerialized());
            data.put("updated", String.valueOf(input.getUpdated()));
            data.put("statusUpdated", String.valueOf(input.getStatusUpdated()));
            data.put("statusPayload", input.getStatusPayload());
            data.put("debugFlag", String.valueOf(input.isDebug()));
            return data;
        });
        CliPrintUtils.printTable(output);
    }

    private void printLastUserEvents(EventRepository eventRepository, String id, int limit) {
        Iterable<Map<String, String>> output = Iterables.transform(
                eventRepository.findLastByUserId(UUIDUtils.fromTinkUUID(id), limit),
                input -> {
                    Map<String, String> fields = Maps.newLinkedHashMap();
                    fields.put("id", String.valueOf(input.getId()));
                    fields.put("date", String.valueOf(input.getDate()));
                    fields.put("type", input.getType());
                    fields.put("content", input.getContent());
                    return fields;
                });

        CliPrintUtils.printTable(Lists.newArrayList(output));
    }

    private void printSubscriptions(SubscriptionRepository subscriptionRepository, String userId) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(userId);
        List<Map<String, String>> output = Lists
                .transform(subscriptions, input -> {
                    Map<String, String> data = Maps.newLinkedHashMap();
                    data.put("type", input.getType().name());
                    data.put("subscribed", Boolean.toString(input.isSubscribed()));
                    return data;
                });
        CliPrintUtils.printTable(output);
    }

    private void printFacebookProfile(UserFacebookProfileRepository facebookRepository, String userId) {
        UserFacebookProfile facebookProfile = facebookRepository.findByUserId(userId);
        List<Map<String, String>> output = Lists.newArrayList();

        if (facebookProfile != null) {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("first name", facebookProfile.getFirstName());
            data.put("last name", facebookProfile.getLastName());
            data.put("email", facebookProfile.getEmail());
            data.put("updated", ThreadSafeDateFormat.FORMATTER_DAILY.format(facebookProfile.getUpdated()));

            output.add(data);
        }
        CliPrintUtils.printTable(output);
    }

    private void printUserPushTokens(DeviceRepository deviceRepository, String userId) {
        List<Device> tokens = deviceRepository.findByUserId(userId);
        List<Map<String, String>> output = Lists.transform(tokens, input -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("notificationToken", input.getNotificationToken());
            data.put("type", input.getType());
            return data;
        });
        CliPrintUtils.printTable(output);
    }

    private static final Ordering<UserDevice> USER_DEVICE_ORDERING = new Ordering<UserDevice>() {
        @Override
        public int compare(UserDevice left, UserDevice right) {
            return ComparisonChain.start()
                    .compare(left.getUpdated(), right.getUpdated(), Ordering.natural().nullsFirst())
                    .compare(left.getInserted(), right.getInserted(), Ordering.natural().nullsFirst())
                    .result();

        }
    };

    private void printUserDevices(UserDeviceRepository userDeviceRepository, String userId) {
        // Fetch max 20 of the most recently used devices.
        List<UserDevice> devices = USER_DEVICE_ORDERING.reverse().leastOf(userDeviceRepository.findByUserId(userId), 20);

        List<Map<String, String>> output = Lists.transform(devices, input -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("inserted", String.valueOf(input.getInserted()));
            data.put("status", input.getStatus().name());
            data.put("updated", String.valueOf(input.getUpdated()));
            data.put("agent", input.getUserAgent());
            return data;
        });
        CliPrintUtils.printTable(output);
    }

    private void printUserForgotPasswordToken(UserForgotPasswordTokenRepository userForgotPasswordTokenRepository,
            String userId) {
        List<UserForgotPasswordToken> tokens = userForgotPasswordTokenRepository.findByUserId(userId);
        List<Map<String, String>> output = Lists.transform(tokens,
                input -> {
                    Map<String, String> data = Maps.newLinkedHashMap();
                    data.put("inserted", String.valueOf(input.getInserted()));
                    data.put("token", input.getId());
                    return data;
                });
        CliPrintUtils.printTable(output);
    }

    private void printUserInfo(User user) {
        List<Map<String, String>> output = Lists.newArrayList();
        output.add(CliPrintUtils.keyValueEntry("id", String.format("%s (%s)", user.getId(),
                UUIDUtils.fromTinkUUID(user.getId()).toString())));
        output.add(CliPrintUtils.keyValueEntry("username", user.getUsername()));
        output.add(CliPrintUtils.keyValueEntry("profile_market", user.getProfile().getMarket()));
        output.add(CliPrintUtils.keyValueEntry("profile_locale", user.getProfile().getLocale()));
        output.add(CliPrintUtils.keyValueEntry("created", String.valueOf(user.getCreated())));
        output.add(CliPrintUtils.keyValueEntry("profile_periodadjustedday",
                Integer.toString(user.getProfile().getPeriodAdjustedDay())));
        output.add(CliPrintUtils.keyValueEntry("blocked", Boolean.toString(user.isBlocked())));
        output.add(CliPrintUtils.keyValueEntry("flags", String.valueOf(user.getFlags())));
        output.add(CliPrintUtils.keyValueEntry("debugFlag", String.valueOf(user.isDebug())));
        output.add(CliPrintUtils.keyValueEntry("nationalId", user.getNationalId()));
        output.add(CliPrintUtils.keyValueEntry("hasPassword", String.valueOf(!Strings.isNullOrEmpty(user.getHash()))));

        CliPrintUtils.printTable(output);
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Input validation
        final String username = System.getProperty("username");
        final String userId = System.getProperty("userId");
        final String nationalId = System.getProperty("nationalId");
        System.out.println("Username to search for is: " + username);
        System.out.println("UserId to search for is: " + userId);
        System.out.println("NationalId to search for is: " + nationalId);

        Preconditions.checkArgument(Strings.nullToEmpty(username).trim().length() > 0
                || Strings.nullToEmpty(userId).trim().length() > 0
                || Strings.nullToEmpty(nationalId).trim().length() > 0);

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        AccountRepository accountsRepository = serviceContext.getRepository(AccountRepository.class);
        CredentialsEventRepository credentialsEventRepository = serviceContext.getRepository(CredentialsEventRepository.class);
        UserForgotPasswordTokenRepository userForgotPasswordTokenRepository = serviceContext
                .getRepository(UserForgotPasswordTokenRepository.class);
        DeviceRepository deviceRepository = serviceContext.getRepository(DeviceRepository.class);
        UserDeviceRepository userDeviceRepository = serviceContext.getRepository(UserDeviceRepository.class);
        EventRepository eventRepository = serviceContext.getRepository(EventRepository.class);
        UserEventRepository userEventRepository = serviceContext.getRepository(UserEventRepository.class);
        LoanDataRepository loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);

        SubscriptionRepository subscriptionRepository = serviceContext.getRepository(SubscriptionRepository.class);
        SubscriptionTokenRepository subscriptionTokenRepository = serviceContext.getRepository(SubscriptionTokenRepository.class);
        SubscriptionHelper subscriptionHelper = new SubscriptionHelper(subscriptionRepository, subscriptionTokenRepository);
        UserFacebookProfileRepository userFacebookProfileRepository = serviceContext.getRepository(UserFacebookProfileRepository.class);
        ApplicationRepository applicationRepository = serviceContext.getRepository(ApplicationRepository.class);
        ProductTemplateRepository productTemplateRepository = serviceContext.getRepository(ProductTemplateRepository.class);
        ProductDAO productDAO = serviceContext.getDao(ProductDAO.class);
        FollowItemRepository followItemRepository = serviceContext.getRepository(FollowItemRepository.class);

        User user;

        if (!Strings.isNullOrEmpty(userId)) {
            user = userRepository.findOne(userId);
        } else if (!Strings.isNullOrEmpty(nationalId)) {
            user = userRepository.findOneByNationalId(nationalId);
        } else {
            user = userRepository.findOneByUsername(username);
        }

        // Output presented to the end user.

        System.out.println("<!-- START -->");

        try {

            if (user == null) {

                System.out.println("Could not find user.");

            } else {

                System.out.println("User");
                printUserInfo(user);

                System.out.println();
                System.out.println("Credentials");
                printCredentials(credentialsRepository, user.getId());

                System.out.println();
                System.out.println("Accounts");
                printAccounts(accountsRepository, user.getId());

                System.out.println();
                System.out.println("Loans");
                printLoans(accountsRepository, loanDataRepository, user.getId());

                System.out.println();
                System.out.println("Password tokens");
                printUserForgotPasswordToken(userForgotPasswordTokenRepository, user.getId());

                System.out.println();
                System.out.println("User devices");
                printUserDevices(userDeviceRepository, user.getId());

                System.out.println();
                System.out.println("User tokens");
                printUserPushTokens(deviceRepository, user.getId());

                System.out.println();
                System.out.println("Subscriptions");
                printSubscriptions(subscriptionRepository, user.getId());

                System.out.println();
                System.out.println("Facebook profile");
                printFacebookProfile(userFacebookProfileRepository, user.getId());

                System.out.println();
                System.out.println("Events");
                printLastUserEvents(eventRepository, user.getId(), 50);

                // No header here - it's being printed in the method.
                printLastCredentialsEvents(credentialsEventRepository, credentialsRepository, user.getId());

                System.out.println();
                System.out.println("User events");
                printLastUserEvents(userEventRepository, user.getId());

                System.out.println();
                System.out.println("User e-mail subscription links:");
                printSubscriptionLinks(subscriptionHelper, user.getId());

                System.out.println();
                System.out.println("User applications:");
                printApplications(applicationRepository, user.getId());

                System.out.println();
                System.out.println("Product articles:");
                printProductArticles(productDAO, productTemplateRepository, user.getId());

                System.out.println();
                System.out.println("Follow items:");
                printFollowItems(followItemRepository, user.getId());
            }

        } finally {

            // Without this finally clause, the Salt runner will not be able to match output correctly.

            System.out.println("<!-- END   -->");

        }

        // Data parsed by Salt to be able to make log searches.

        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
        for (Credentials credential : credentials) {
            System.out.println("SALT(credentialsid) = " + credential.getId());
        }

    }

    private void printFollowItems(FollowItemRepository followItemRepository, String userId) {
        List<Map<String, String>> output = followItemRepository.findByUserId(userId).stream().map(input -> {
            Map<String, String> data = Maps.newLinkedHashMap();
            data.put("id", String.valueOf(input.getId()));
            data.put("created", String.valueOf(input.getCreated()));
            data.put("type", String.valueOf(input.getType()));
            data.put("lastModified", String.valueOf(input.getLastModified()));

            if (!Strings.isNullOrEmpty(input.getCriteria()) && input.getType() != null) {
                switch (input.getType()) {
                case EXPENSES:
                    ExpensesFollowCriteria expensesCriteria = SerializationUtils
                            .deserializeFromString(input.getCriteria(), ExpensesFollowCriteria.class);

                    data.put("categoryIds", String.valueOf(expensesCriteria.getCategoryIds()));
                    break;
                case SAVINGS:
                    SavingsFollowCriteria savingsCriteria = SerializationUtils
                            .deserializeFromString(input.getCriteria(), SavingsFollowCriteria.class);

                    data.put("accountIds", String.valueOf(savingsCriteria.getAccountIds()));
                    data.put("targetPeriod", String.valueOf(savingsCriteria.getTargetPeriod()));
                    break;
                case SEARCH:
                    // Only sensitive data
                    break;
                }
            }

            return data;
        }).collect(Collectors.toList());

        CliPrintUtils.printTable(output);
    }

    private void printProductArticles(final ProductDAO productDAO,
            final ProductTemplateRepository productTemplateRepository,
            String userId) {

        List<Map<String, String>> output =
                Lists.transform(productDAO.findAllArticlesByUserId(UUIDUtils.fromTinkUUID(userId)),
                        input -> {
                            Map<String, String> data = Maps.newLinkedHashMap();
                            try {
                                data.put("templateId", UUIDUtils.toTinkUUID(input.getTemplateId()));
                                data.put("filterId", UUIDUtils.toTinkUUID(input.getFilterId()));
                                data.put("instanceId", UUIDUtils.toTinkUUID(input.getInstanceId()));
                                data.put("providername", input.getProviderName());
                                data.put("name", input.getName());
                                data.put("type", String.valueOf(input.getType()));
                                data.put("status", String.valueOf(input.getStatus()));
                                data.put("validFrom", String.valueOf(input.getValidFrom()));
                                data.put("validTo", String.valueOf(input.getValidTo()));

                                ProductInstance instance = productDAO
                                        .findInstanceByUserIdAndId(input.getUserId(), input.getInstanceId());
                                ProductTemplate template = productTemplateRepository
                                        .findById(input.getTemplateId());

                                Map<String, Object> properties = Maps.newHashMap();
                                properties.putAll(template.getProperties());
                                properties.putAll(instance.getProperties());

                                data.put("properties", mapper.writeValueAsString(properties));

                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                            return data;
                        });
        CliPrintUtils.printTable(output);
    }

    private static final Ordering<ApplicationRow> APPLICATION_ROW_BY_CREATION_DATE = new Ordering<ApplicationRow>() {
        @Override
        public int compare(ApplicationRow a1, ApplicationRow a2) {
            return ComparisonChain.start()
                    .compare(a1.getCreated(), a2.getCreated(), Ordering.natural().nullsFirst())
                    .compare(a1.getUpdated(), a2.getUpdated(), Ordering.natural().nullsFirst())
                    .result();
        }
    };

    private void printApplications(ApplicationRepository applicationRepository, String userId) {

        List<ApplicationRow> applications = APPLICATION_ROW_BY_CREATION_DATE.reverse().sortedCopy(
                applicationRepository.findAllByUserId(userId));

        List<Map<String, String>> output =
                Lists.transform(applications, input -> {
                    Map<String, String> data = Maps.newLinkedHashMap();
                    data.put("id", String.valueOf(UUIDUtils.fromTinkUUID(input.getId())));
                    data.put("created", String.valueOf(input.getCreated()));
                    data.put("type", input.getType());
                    data.put("status", input.getStatus());
                    data.put("updated", String.valueOf(input.getUpdated()));
                    data.put("properties", input.getProperties());
                    return data;
                });
        CliPrintUtils.printTable(output);
    }

    private void printSubscriptionLinks(SubscriptionHelper subscriptionHelper, String userId) {
        for (String lang : ImmutableList.of("sv", "en")) {
            System.out.println(String.format(
                    " * https://app.tink.se/subscriptions/%s?locale=%s",
                    subscriptionHelper.getOrCreateTokenFor(userId), lang));
        }
    }

    private void printLastUserEvents(UserEventRepository userEventRepository, String userId) {
        int queryLimit = 10;
        List<UserEvent> events = userEventRepository.findAllByUserIdOrderByDateDesc(userId, new PageRequest(0, queryLimit));
        List<Map<String, String>> output = Lists.transform(events,
                input -> {
                    Map<String, String> data = Maps.newLinkedHashMap();
                    data.put("date", String.valueOf(input.getDate()));
                    data.put("remoteaddress", String.valueOf(input.getRemoteAddress()));
                    data.put("type", String.valueOf(input.getType()));
                    return data;
                });
        CliPrintUtils.printTable(output);
    }

}
