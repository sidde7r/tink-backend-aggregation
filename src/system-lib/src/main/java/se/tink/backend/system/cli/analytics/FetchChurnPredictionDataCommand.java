package se.tink.backend.system.cli.analytics;

import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Sets;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.cassandra.TransferEventRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FollowItemRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.Event;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Market;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDemographics;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.core.transfer.TransferEvent;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.uuid.UUIDUtils;

public class FetchChurnPredictionDataCommand extends ServiceContextCommand<ServiceConfiguration>{

    final String USER_ID_HASH_SALT = "91f62fb9dteb4gbbbdd41b323452a5d0";

    private static final LogUtils log = new LogUtils(FetchChurnPredictionDataCommand.class);
    private final AtomicInteger processedUsers = new AtomicInteger();
    private final AtomicInteger usersWithBadData = new AtomicInteger();
    private final AtomicInteger skippedUsers = new AtomicInteger();
    private String daysBackText;
    private Integer churnDays;
    private Date latestPredictionDate;
    private int featureDaysBack;
    private Writer writer;
    private FileOutputStream outputStream;

    private UserRepository userRepository;
    private CredentialsRepository credentialsRepository;
    private CredentialsEventRepository credentialsEventRepository;
    private TransferEventRepository transferEventRepository;
    private UserDemographicsRepository userDemographicsRepository;
    private AccountRepository accountRepository;
    private EventRepository eventRepository;
    private FollowItemRepository followItemRepository;
    private TransactionDao transactionDao;
    private LoanDataRepository loanDataRepository;

    public FetchChurnPredictionDataCommand() {
        super("fetch-churn-prediction-data", "Command to fetch data for churn prediction");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws Exception {
        if (!fetchSystemProperties()) {
            return;
        }

        log.info("Fetching churn prediction data.");

        userRepository = serviceContext.getRepository(UserRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        credentialsEventRepository = serviceContext.getRepository(CredentialsEventRepository.class);
        transferEventRepository = serviceContext.getRepository(TransferEventRepository.class);
        userDemographicsRepository = serviceContext.getRepository(UserDemographicsRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        eventRepository = serviceContext.getRepository(EventRepository.class);
        followItemRepository = serviceContext.getRepository(FollowItemRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);

        userRepository.streamAll().filter(user -> {
            logNrOfHandledUsers();
            boolean isSwedishMarketUser = Objects.equal(user.getProfile().getMarket(), Market.Code.SE.name());
            Date todayMinusChurnAndFeatureDays = DateUtils.addDays(new Date(), -(churnDays + featureDaysBack));
            boolean hasBeenAMemberLongEnough = user.getCreated().before(todayMinusChurnAndFeatureDays);
            if (isSwedishMarketUser && hasBeenAMemberLongEnough) {
                return true;
            } else {
                skippedUsers.getAndIncrement();
                return false;
            }
        }).forEach(user -> {
            UserData userData = new UserData(user);
            List<Integer> hasLoggedInPerDaySinceCreated = getLoginListAndSetPredictionDate(userData);

            // Some features are calculated X days back from the user prediction date.
            userData.featureFromDate = DateUtils.addDays(userData.predictionDate, -Math.abs(featureDaysBack));
            userData.featureFromDate = DateUtils.setInclusiveStartTime(userData.featureFromDate);

            if (!user.getCreated().before(userData.featureFromDate)) {
                skippedUsers.getAndIncrement();
                return;
            }

            // Fetch and set all the data and write to file.
            try {
                fetchAndSetGeneralUserData(userData);
                fetchAndSetUserDemographicsData(userData);
                fetchAndSetLoginData(userData, hasLoggedInPerDaySinceCreated);
                fetchAndSetTransferEventData(userData);
                fetchAndSetCredentialsAndAccountData(userData);
                fetchAndSetFollowItemData(userData);
                fetchAndSetTransactionData(userData);
                setIsChurn(userData, hasLoggedInPerDaySinceCreated);
                userData.writeToFile();
            } catch (Exception e) {
                log.error("Caught exception", e);
                usersWithBadData.getAndIncrement();
            }
        });

        writer.close();
        outputStream.close();
        log.info(String.format("Processed %s users. %s of these were skipped because of not fulfilling the conditions "
                        + "and %s were skipped because of bad/unknown data.",
                processedUsers.get(), skippedUsers.get(), usersWithBadData.get()));

    }

    private void setIsChurn(UserData userData, List<Integer> hasLoggedInPerDaySinceCreated) {
        int isChurn = 1;
        int startIndex = userData.predictionDateIndexInLoginArray + 1;
        int endIndex = startIndex + churnDays;
        for (int i = startIndex; i < endIndex; i++) {
            if (hasLoggedInPerDaySinceCreated.get(i) == 1) {
                isChurn = 0;
                break;
            }
        }

        userData.add("isChurn", isChurn);
    }

    private void fetchAndSetTransactionData(final UserData userData) {
        List<Transaction> allTransactions = transactionDao.findAllByUserId(userData.user.getId());
        int nrOfTransactions = 0;
        int nrOfTaggedTransactions = 0;
        Set<String> uniqueTags = Sets.newHashSet();

        for (Transaction transaction : allTransactions) {
            if (new Date(transaction.getInserted()).before(userData.predictionDate)) {
                nrOfTransactions++;

                List<String> tags = extractTags(transaction);
                if (tags != null && !tags.isEmpty()) {
                    nrOfTaggedTransactions++;
                    uniqueTags.addAll(tags);
                }
            }
        }

        userData.add("nrOfTransactions", nrOfTransactions);
        userData.add("nrOfTaggedTransactions", nrOfTaggedTransactions);
        userData.add("nrOfUniqueTags", uniqueTags.size());
    }

    private static List<String> extractTags(Transaction t) {
        Splitter SPLITTER = Splitter.on(CharMatcher.WHITESPACE).trimResults().omitEmptyStrings();
        if (Strings.isNullOrEmpty(t.getNotes())) {
            return null;
        } else {
            return Lists.newArrayList(Iterables.filter(SPLITTER.split(t.getNotes()),
                    s -> (s.length() > 1 && s.charAt(0) == '#')));
        }
    }

    private void fetchAndSetCredentialsAndAccountData(final UserData userData) throws AvoidThisUserException {
        final String userId = userData.user.getId();
        List<Account> accounts = accountRepository.findByUserId(userId);
        List<Credentials> allCredentials = credentialsRepository.findAllByUserId(userId);

        // Only pick the credentials which were actually added before the prediction date.
        List<Credentials> credentialsOnPredictionDate = Lists
                .newArrayList(Iterables.filter(allCredentials, credential -> {
                    List<CredentialsEvent> events = credentialsEventRepository
                            .findByUserIdAndCredentialsId(userId, credential.getId());
                    CredentialsEvent createdEvent = Iterables.find(events,
                            event -> Objects.equal(event.getStatus(), CredentialsStatus.CREATED));
                    return createdEvent.getTimestamp().before(userData.predictionDate);
                }));

        final Map<String, Credentials> credentialsById = Maps
                .uniqueIndex(credentialsOnPredictionDate, Credentials::getId);

        ArrayList<Account> accountsOnPredictionDate = Lists
                .newArrayList(Iterables.filter(accounts,
                        account -> credentialsById.get(account.getCredentialsId()) != null));

        userData.add("hasIdentityCheck", getUserHasUpdatedCreditSafe(credentialsOnPredictionDate));
        userData.add("nrOfCredentials", credentialsOnPredictionDate.size());
        userData.add("credentials", credentialsToString(credentialsOnPredictionDate));

        int nrOfSavingsAccounts = 0;
        int nrOfInvestmentAccounts = 0;
        int nrOfCheckingAccounts = 0;
        int nrOfMortgages = 0;
        int nrOfBlancoLoans = 0;
        int nrOfStudentLoans = 0;
        int nrOfMembershipLoans = 0;
        int nrOfVehicleLoans = 0;
        int nrOfLandLoans = 0;
        int nrOfOtherLoans = 0;
        int nrOfCreditCardAccounts = 0;
        int nrOfPensionAccounts = 0;
        int nrOfOtherAccounts = 0;
        int nrOfExternalAccounts = 0;

        for (Account account : accountsOnPredictionDate) {
            switch (account.getType()) {
            case SAVINGS:
                nrOfSavingsAccounts++;
                break;
            case INVESTMENT:
                nrOfInvestmentAccounts++;
                break;
            case CHECKING:
                nrOfCheckingAccounts++;
                break;
            case CREDIT_CARD:
                nrOfCreditCardAccounts++;
                break;
            case PENSION:
                nrOfPensionAccounts++;
                break;
            case OTHER:
                nrOfOtherAccounts++;
                break;
            case EXTERNAL:
                nrOfExternalAccounts++;
                break;
            case LOAN:
            case MORTGAGE:
                Loan loan = loanDataRepository.findMostRecentOneByAccountId(account.getId());

                if (loan == null) {
                    nrOfOtherLoans++;
                    break;
                }

                switch (loan.getType()) {
                case MORTGAGE:
                    nrOfMortgages++;
                    break;
                case BLANCO:
                    nrOfBlancoLoans++;
                    break;
                case MEMBERSHIP:
                    nrOfMembershipLoans++;
                    break;
                case STUDENT:
                    nrOfStudentLoans++;
                    break;
                case VEHICLE:
                    nrOfVehicleLoans++;
                    break;
                case LAND:
                    nrOfLandLoans++;
                    break;
                case OTHER:
                    nrOfOtherLoans++;
                    break;

                }
                break;
            case DUMMY:
                // ignore
                break;
            }
        }

        userData.add("nrOfSavingsAccounts", nrOfSavingsAccounts);
        userData.add("nrOfInvestmentAccounts", nrOfInvestmentAccounts);
        userData.add("nrOfCheckingAccounts", nrOfCheckingAccounts);
        userData.add("nrOfMortgageAccounts", nrOfMortgages);
        userData.add("nrOfBlancoLoans", nrOfBlancoLoans);
        userData.add("nrOfStudentLoans", nrOfStudentLoans);
        userData.add("nrOfMembershipLoans", nrOfMembershipLoans);
        userData.add("nrOfVehicleLoans", nrOfVehicleLoans);
        userData.add("nrOfLandLoans", nrOfLandLoans);
        userData.add("nrOfOtherLoans", nrOfOtherLoans);
        userData.add("nrOfCreditCardAccounts", nrOfCreditCardAccounts);
        userData.add("nrOfPensionAccounts", nrOfPensionAccounts);
        userData.add("nrOfOtherAccounts", nrOfOtherAccounts);
        userData.add("nrOfExternalAccounts", nrOfExternalAccounts);
    }

    private String credentialsToString(List<Credentials> credentials) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < credentials.size(); i++) {
            Credentials credential = credentials.get(i);
            builder.append("{providerName=");
            builder.append(credential.getProviderName());
            builder.append(", status=");
            builder.append(credential.getStatus());
            builder.append(", updated=");
            builder.append(credential.getUpdated());
            builder.append(i == credentials.size() - 1 ? "}" : "}, ");
        }
        builder.append("]");
        return builder.toString();
    }

    private void logNrOfHandledUsers() {
        int handledUsers = processedUsers.getAndIncrement();
        if (handledUsers % 10000 == 0) {
            log.info(String.format("Processed %s users", handledUsers));
        }
    }

    private void fetchAndSetGeneralUserData(UserData userData) {
        String hashedUserId = StringUtils.hashAsStringSHA1(userData.user.getId(), USER_ID_HASH_SALT);
        userData.add("hashedUserId", hashedUserId);
        userData.add("nrOfDaysSinceCreated", userData.nrOfDaysSinceCreated);
    }

    private void fetchAndSetFollowItemData(final UserData userData) {
        List<FollowItem> followItems = Lists.newArrayList(
                Iterables.filter(followItemRepository.findByUserId(userData.user.getId()),
                        item -> item.getCreated().before(userData.predictionDate)));
        userData.add("nrOfFollowItems", followItems.size());
    }

    private void fetchAndSetUserDemographicsData(UserData userData) throws AvoidThisUserException {
        UserDemographics userDemographics = userDemographicsRepository.findOne(userData.user.getId());

        if (userDemographics == null) {
            throw new AvoidThisUserException("Did not find user demographics");
        }

        // Sometimes data is missing here and then we'll skip the user.
        try {
            userData.add("age", userDemographics.getAge());
            userData.add("community", userDemographics.getCommunity());
            Long categorizationLevel = userDemographics.getCurrentCategorization();
            userData.add("categorizationLevel", categorizationLevel == null ? "0" : Long.toString(categorizationLevel));
            Double errorFrequency = userDemographics.getWeeklyErrorFrequency();
            userData.add("percentageOfWeeksWithRefreshErrors",
                    errorFrequency == null ? "0.0" : Double.toString(errorFrequency));
        } catch (Exception e) {
            throw new AvoidThisUserException("Couldn't get all data from user demographics");
        }
    }

    private int getUserHasUpdatedCreditSafe(List<Credentials> credentialsOnPredictionDate) {
        Optional<Credentials> creditSafe = credentialsOnPredictionDate.stream()
                .filter(credential -> Objects.equal(credential.getProviderName(), "creditsafe")).findFirst();

        if (!creditSafe.isPresent()) {
            return 0;
        }
        boolean hasUpdatedCreditSafe = Objects.equal(creditSafe.get().getStatus(), CredentialsStatus.UPDATED);
        return hasUpdatedCreditSafe ? 1 : 0;
    }

    private void fetchAndSetLoginData(UserData userData, List<Integer> hasLoggedInPerDaySinceCreated) {
        userData.add("loginArray", hasLoggedInPerDaySinceCreated.toString());
        userData.add("nrOfUniqueWeeksTheUserHasUsedTheApp", getUniqueUseWeeks(hasLoggedInPerDaySinceCreated));
    }

    private boolean fetchSystemProperties() {

        // The nr of days back from the prediction date that some features will be calculated from.
        featureDaysBack = Integer.getInteger("featureDaysBack", 30);
        daysBackText = "Last" + featureDaysBack + "Days";

        // The nr of days a user has to be inactive in order for us to consider the user as being churned.
        churnDays = Integer.getInteger("churnDays", 90);

        // The latest possible prediction date in order to be able to predict the future.
        latestPredictionDate = DateUtils.addDays(new Date(), -churnDays);
        DateUtils.flattenTime(latestPredictionDate);

        String outputFile = System.getProperty("outputFile");
        Preconditions.checkNotNull(outputFile, "Output file can't be null");

        File output = new File(outputFile);

        try {
            if (!output.getParentFile().exists()) {
                if (!output.getParentFile().mkdirs()) {
                    log.error("Could not create new directory for output file");
                    return false;
                }
            }

            if (!output.exists()) {
                if (!output.createNewFile()) {
                    log.error("Could not create new file");
                    return false;
                }
            }

            // Make sure file is only readable by creator and nobody else.
            if (!output.setReadable(false, false) || !output.setReadable(true, true) || !output
                    .setWritable(false, false)) {
                log.error("Could not change permissions of output file");
                return false;
            }

            outputStream = new FileOutputStream(output);
            writer = new OutputStreamWriter(new GZIPOutputStream(outputStream), "UTF-8");
        } catch (Exception e) {
            log.error("Caught exception", e);
            return false;
        }

        return true;
    }

    private void fetchAndSetTransferEventData(UserData userData) {
        List<TransferEvent> transferEvents = transferEventRepository
                .findAllByUserId(UUIDUtils.fromTinkUUID(userData.user.getId()));

        int nrOfExecutedTransfers = 0;
        int nrOfFailedTransfers = 0;
        boolean hasTriedBankTransfers = false;
        boolean hasTriedEinvoices = false;
        boolean hasTriedPayments = false;

        for (TransferEvent event : transferEvents) {
            if (event.getCreated().after(userData.featureFromDate) && event.getCreated()
                    .before(userData.predictionDate)) {
                if (Objects.equal(event.getStatus(), SignableOperationStatuses.EXECUTED)) {
                    nrOfExecutedTransfers++;
                } else if (Objects.equal(event.getStatus(), SignableOperationStatuses.FAILED)) {
                    nrOfFailedTransfers++;
                }
            }

            if (Objects.equal(event.getTransferType(), TransferType.BANK_TRANSFER)) {
                hasTriedBankTransfers = true;
            } else if (Objects.equal(event.getTransferType(), TransferType.PAYMENT)) {
                hasTriedPayments = true;
            } else if (Objects.equal(event.getTransferType(), TransferType.EINVOICE)) {
                hasTriedEinvoices = true;
            }
        }

        int totalNrOfTransfers = nrOfExecutedTransfers + nrOfFailedTransfers;
        double failPercentage = totalNrOfTransfers == 0 ? 0 : (double) nrOfFailedTransfers / totalNrOfTransfers;
        userData.add("nrOfExecutedTransfersEinvoicesPayments" + daysBackText, nrOfExecutedTransfers);
        userData.add("failPercentageForTransfersEinvoicesPayments" + daysBackText, Double.toString(failPercentage));
        userData.add("hasTriedBankTransfers", hasTriedBankTransfers ? 1 : 0);
        userData.add("hasTriedPayments", hasTriedPayments ? 1 : 0);
        userData.add("hasTriedEinvoices", hasTriedEinvoices ? 1 : 0);
    }

    private int getUniqueUseWeeks(List<Integer> hasLoggedInPerDaySinceCreated) {
        int nrOfUniqueWeeksTheUserHasUsedTheApp = 0;
        List<Integer> temporaryWeekList = Lists.newArrayList();

        // Split login array until (last) prediction date into week lists and check if the user has logged in that week.
        for (int i = 0; i <= hasLoggedInPerDaySinceCreated.size() - churnDays; i++) {
            int loginValueForDate = hasLoggedInPerDaySinceCreated.get(i);
            temporaryWeekList.add(loginValueForDate);
            if(temporaryWeekList.size() == 7) {
                if (temporaryWeekList.contains(1)) {
                    nrOfUniqueWeeksTheUserHasUsedTheApp++;
                }
                temporaryWeekList = Lists.newArrayList();
            }
        }

        return nrOfUniqueWeeksTheUserHasUsedTheApp;
    }

    private List<Integer> getLoginListAndSetPredictionDate(UserData userData) {
        List<Event> userEvents = eventRepository
                .findUserEventsAfter(UUIDUtils.fromTinkUUID(userData.user.getId()), userData.user.getCreated());
        List<Integer> loginList = Lists.newArrayListWithExpectedSize(userData.nrOfDaysSinceCreated);
        Set<String> loggedInDaysSinceCreated = Sets.newHashSet();

        for (Event event : userEvents) {
            if (Objects.equal(event.getType(), "user.context.get")) {
                String date = ThreadSafeDateFormat.FORMATTER_DAILY.format(event.getDate());
                loggedInDaysSinceCreated.add(date);
            }
        }

        // The first day is always true since that is when the user was created.
        loginList.add(1);

        // Increase one day at a time from created date to yesterday and check if the user has been logged in that day.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(userData.user.getCreated());
        userData.predictionDate = userData.user.getCreated();
        for (int i = 1; i < userData.nrOfDaysSinceCreated; i++) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            String date = ThreadSafeDateFormat.FORMATTER_DAILY.format(calendar.getTime());
            if (loggedInDaysSinceCreated.contains(date)) {
                loginList.add(1);

                // The prediction date is set so that the outcome of churn/non-churn can be known. The specific
                // prediction date for each user is set to the last time the user was logged in, calculated backwards
                // from the latest possible prediction date.
                if (i <= userData.nrOfDaysSinceCreated - churnDays) {
                    userData.predictionDate = calendar.getTime();
                    userData.predictionDateIndexInLoginArray = i;
                }
            } else {
                loginList.add(0);
            }
        }

        return loginList;
    }

    private class UserData {
        private List<String> userOutput = Lists.newArrayList();
        private List<String> featureNames = Lists.newArrayList();
        User user;
        int nrOfDaysSinceCreated;
        public Date featureFromDate;
        public Date predictionDate;
        public int predictionDateIndexInLoginArray;

        public UserData(User user) {
            this.user = user;
            nrOfDaysSinceCreated = DateUtils.getNumberOfDaysBetween(user.getCreated(), new Date());
        }

        void add(String featureName, String content) {
            userOutput.add(content);

            if (processedUsers.get() == 1) {
                featureNames.add(featureName);
            }
        }

        void add(String featureName, Integer content) {
            userOutput.add(Integer.toString(content));

            if (processedUsers.get() == 1) {
                featureNames.add(featureName);
            }
        }

        public void writeToFile() throws IOException {
            if (processedUsers.get() == 1) {
                writer.append(Joiner.on(", ").join(featureNames));
            }
            writer.append("\n");
            writer.append(Joiner.on(", ").join(userOutput));
        }
    }

    private class AvoidThisUserException extends Exception {

        public AvoidThisUserException(String message) {
            super(message);
        }
    }
}
