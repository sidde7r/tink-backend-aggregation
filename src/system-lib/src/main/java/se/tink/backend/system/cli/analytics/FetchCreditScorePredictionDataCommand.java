package se.tink.backend.system.cli.analytics;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.CredentialsEventRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.UserDemographicsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsEvent;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.FraudCreditScoringContent;
import se.tink.backend.core.FraudCreditorContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIncomeContent;
import se.tink.backend.core.FraudNonPaymentContent;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Market;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDemographics;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class FetchCreditScorePredictionDataCommand extends ServiceContextCommand<ServiceConfiguration> {

    private final String userIdHashSalt = System.getProperty("usersSalt");

    private static final LogUtils log = new LogUtils(FetchCreditScorePredictionDataCommand.class);
    private final AtomicInteger processedUsers = new AtomicInteger();
    private final AtomicInteger skippedUsers = new AtomicInteger();
    private final AtomicInteger usersWithBadData = new AtomicInteger();
    private Writer writer;
    private FileOutputStream outputStream;
    private Date currentMonth;

    private CredentialsRepository credentialsRepository;
    private CredentialsEventRepository credentialsEventRepository;
    private UserDemographicsRepository userDemographicsRepository;
    private AccountRepository accountRepository;
    private TransactionDao transactionDao;
    private LoanDataRepository loanDataRepository;
    private CategoryRepository categoryRepository;
    private FraudDetailsRepository fraudDetailsRepository;

    public FetchCreditScorePredictionDataCommand() {
        super("fetch-credit-score-prediction-data", "Command to fetch data for credit score prediction");
    }

    @Override protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        credentialsEventRepository = serviceContext.getRepository(CredentialsEventRepository.class);
        userDemographicsRepository = serviceContext.getRepository(UserDemographicsRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        fraudDetailsRepository = serviceContext.getRepository(FraudDetailsRepository.class);

        log.info("Fetching data for credit score prediction");

        if (!fetchSystemProperties()) {
            return;
        }

        currentMonth = getFirstDayOfMonthAndInclusiveStartTimeFromDate(new Date());

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(20))
                .filter(user -> {
                    processedUsers.getAndIncrement();
                    boolean userCreatedBeforeFirstDayOfThisMonth = user.getCreated().before(currentMonth);
                    boolean isSwedishMarketUser = Objects
                            .equal(user.getProfile().getMarket(), Market.Code.SE.name());
                    if (isSwedishMarketUser && userCreatedBeforeFirstDayOfThisMonth) {
                        return true;
                    } else {
                        skippedUsers.getAndIncrement();
                        return false;
                    }
                }).forEach(user -> {
            List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
            if (!userHasCreditSafe(credentials)) {
                skippedUsers.getAndIncrement();
                return;
            }

            UserData userData = new UserData(user);

            // Fetch and set all the data and write to file.
            try {
                fetchAndSetGeneralUserData(userData);
                fetchAndSetUserDemographicsData(userData);
                fetchAndSetMonthVaryingData(userData, credentials);
                userData.writeToFile();
            } catch (Exception e) {
                log.error("Caught exception", e);
                usersWithBadData.getAndIncrement();
            }
        });

        writer.close();
        outputStream.close();
        log.info(String.format("DONE. Processed %s users. %s of these were skipped because of not fulfilling the "
                        + "conditions and %s were skipped because of bad/unknown data.",
                processedUsers.get(), skippedUsers.get(), usersWithBadData.get()));
    }

    private boolean userHasCreditSafe(List<Credentials> credentials) {
        Optional<Credentials> creditSafe = credentials.stream()
                .filter(credential -> Objects.equal(credential.getProviderName(), "creditsafe"))
                .findFirst();

        return creditSafe.isPresent();
    }

    private void fetchAndSetMonthVaryingData(UserData userData, List<Credentials> credentials)
            throws AvoidThisUserException {
        Map<Date, MonthlyData> monthlyDataToMonthDate = createMonthlyDataToMonthMap(userData);

        fetchAndSetCredentialsAndAccountData(userData, monthlyDataToMonthDate, credentials);
        fetchAndSetTransactionData(userData, monthlyDataToMonthDate);
        fetchAndSetFraudDetailsData(userData, monthlyDataToMonthDate);

        String featuresByMonthString = getFeaturesByMonthString(monthlyDataToMonthDate);
        userData.add(monthlyDataToMonthDate.values().iterator().next().getFeatureNameString(), featuresByMonthString);
    }

    private void fetchAndSetFraudDetailsData(UserData userData, Map<Date, MonthlyData> monthlyDataToMonthDate) {
        List<FraudDetails> fraudDetails = fraudDetailsRepository.findAllByUserId(userData.user.getId());
        ImmutableListMultimap<FraudDetailsContentType, FraudDetails> detailsByType = Multimaps
                .index(fraudDetails, FraudDetails::getType);

        setIncomeData(monthlyDataToMonthDate, detailsByType);
        setCreditScoreData(monthlyDataToMonthDate, detailsByType);
        setDebtData(monthlyDataToMonthDate, detailsByType);
        setNonPaymentData(monthlyDataToMonthDate, detailsByType);
    }

    private void setNonPaymentData(Map<Date, MonthlyData> monthlyDataToMonthDate,
            ImmutableListMultimap<FraudDetailsContentType, FraudDetails> detailsByType) {
        List<FraudDetails> nonPayments = Lists.newArrayList(detailsByType.get(FraudDetailsContentType.NON_PAYMENT));

        Collections.sort(nonPayments, (o1, o2) -> o1.getCreated().compareTo(o2.getCreated()));

        for (int i = 0; i < nonPayments.size(); i++) {
            FraudDetails details = nonPayments.get(i);
            Date month = getFirstDayOfMonthAndInclusiveStartTimeFromDate(details.getCreated());
            Date nextCreditUpdateMonth = i == nonPayments.size() - 1 ?
                    currentMonth :
                    getFirstDayOfMonthAndInclusiveStartTimeFromDate(nonPayments.get(i + 1).getCreated());

            // If we have multiple updates on one month, make sure the oldest one is ignored.
            if (Objects.equal(nextCreditUpdateMonth, month)) {
                continue;
            }

            while (!Objects.equal(month, nextCreditUpdateMonth)) {
                MonthlyData monthlyData = monthlyDataToMonthDate.get(month);
                FraudNonPaymentContent content = (FraudNonPaymentContent) details.getContent();
                if (!Objects.equal(details.getStatus(), FraudStatus.EMPTY)) {
                    String info = "{name:" + content.getName() + ", type:" + content.getType() + ", amount:" + content
                            .getAmount() + ", date:" + (content.getDate() == null ?
                            "N/A" : ThreadSafeDateFormat.FORMATTER_DAILY.format(content.getDate())) + "}";
                    monthlyData.nonPayments.add(info);
                }
                month = DateUtils.addMonths(month, 1);
            }
        }
    }

    private void setDebtData(Map<Date, MonthlyData> monthlyDataToMonthDate,
            ImmutableListMultimap<FraudDetailsContentType, FraudDetails> detailsByType) {
        List<FraudDetails> credits = Lists.newArrayList(detailsByType.get(FraudDetailsContentType.CREDITS));

        Collections.sort(credits, (o1, o2) -> o1.getCreated().compareTo(o2.getCreated()));

        for (int i = 0; i < credits.size(); i++) {
            FraudDetails details = credits.get(i);
            Date month = getFirstDayOfMonthAndInclusiveStartTimeFromDate(details.getCreated());
            Date nextCreditUpdateMonth = i == credits.size() - 1 ?
                    currentMonth :
                    getFirstDayOfMonthAndInclusiveStartTimeFromDate(credits.get(i + 1).getCreated());

            // If we have multiple updates on one month, make sure the oldest one is ignored.
            if (Objects.equal(nextCreditUpdateMonth, month)) {
                continue;
            }

            while (!Objects.equal(month, nextCreditUpdateMonth)) {
                MonthlyData monthlyData = monthlyDataToMonthDate.get(month);
                FraudCreditorContent content = (FraudCreditorContent) details.getContent();

                monthlyData.credit = content.getAmount();
                month = DateUtils.addMonths(month, 1);
            }
        }
    }

    private void setCreditScoreData(Map<Date, MonthlyData> monthlyDataToMonthDate,
            ImmutableListMultimap<FraudDetailsContentType, FraudDetails> detailsByType) {
        List<FraudDetails> scores = Lists.newArrayList(detailsByType.get(FraudDetailsContentType.SCORING));

        Collections.sort(scores, (o1, o2) -> o1.getCreated().compareTo(o2.getCreated()));

        for (int i = 0; i < scores.size(); i++) {
            FraudDetails details = scores.get(i);
            Date month = getFirstDayOfMonthAndInclusiveStartTimeFromDate(details.getCreated());
            Date nextScoringUpdateMonth = i == scores.size() - 1 ?
                    currentMonth :
                    getFirstDayOfMonthAndInclusiveStartTimeFromDate(scores.get(i + 1).getCreated());

            // If we have multiple updates on one month, make sure the oldest one is ignored.
            if (Objects.equal(nextScoringUpdateMonth, month)) {
                continue;
            }

            while (!Objects.equal(month, nextScoringUpdateMonth)) {
                MonthlyData monthlyData = monthlyDataToMonthDate.get(month);
                FraudCreditScoringContent content = (FraudCreditScoringContent) details.getContent();

                monthlyData.creditScore = content.getScore() + "/" + content.getMaxScore();
                month = DateUtils.addMonths(month, 1);
            }
        }
    }

    private void setIncomeData(Map<Date, MonthlyData> monthlyDataToMonthDate,
            ImmutableListMultimap<FraudDetailsContentType, FraudDetails> detailsByType) {
        List<FraudDetails> incomes = Lists.newArrayList(detailsByType.get(FraudDetailsContentType.INCOME));

        // Sort in ascending order on created date.
        Collections.sort(incomes, (o1, o2) -> o1.getCreated().compareTo(o2.getCreated()));

        for (int i = 0; i < incomes.size(); i++) {
            FraudDetails details = incomes.get(i);
            Date month = getFirstDayOfMonthAndInclusiveStartTimeFromDate(details.getCreated());
            Date nextIncomeUpdateMonth = i == incomes.size() - 1 ?
                    currentMonth :
                    getFirstDayOfMonthAndInclusiveStartTimeFromDate(incomes.get(i + 1).getCreated());

            // If we have multiple updates on one month, make sure the oldest one is ignored.
            if (Objects.equal(nextIncomeUpdateMonth, month)) {
                continue;
            }

            while (!Objects.equal(month, nextIncomeUpdateMonth)) {
                MonthlyData monthlyData = monthlyDataToMonthDate.get(month);
                FraudIncomeContent content = (FraudIncomeContent) details.getContent();

                monthlyData.yearlyIncomeByCapital = content.getIncomeByCapital();
                monthlyData.yearlyIncomeByService = content.getIncomeByService();
                month = DateUtils.addMonths(month, 1);
            }
        }
    }

    private String getFeaturesByMonthString(Map<Date, MonthlyData> monthlyDataToMonthDate) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (Date month : monthlyDataToMonthDate.keySet()) {
            builder.append(ThreadSafeDateFormat.FORMATTER_MONTHLY.format(month));
            builder.append(":{");
            builder.append(monthlyDataToMonthDate.get(month).getFeatureString());
            builder.append("}, ");
        }

        return builder.substring(0, builder.length() - 2) + "]";
    }

    private void fetchAndSetGeneralUserData(UserData userData) {
        String hashedUserId = StringUtils.hashAsStringSHA1(userData.user.getId(), userIdHashSalt);
        userData.add("hashedUserId", hashedUserId);
        userData.add("registeredDate", ThreadSafeDateFormat.FORMATTER_DAILY.format(userData.user.getCreated()));
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
            userData.add("gender", userDemographics.getGender());
        } catch (Exception e) {
            throw new AvoidThisUserException("Couldn't get all data from user demographics");
        }
    }

    private void fetchAndSetCredentialsAndAccountData(final UserData userData,
            Map<Date, MonthlyData> monthlyDataToMonthDate, List<Credentials> credentials)
            throws AvoidThisUserException {
        userData.add("[{providerName, status, updated}]", credentialsToString(credentials));
        List<Account> accounts = accountRepository.findByUserId(userData.user.getId());

        final Map<String, Credentials> credentialsById = Maps
                .uniqueIndex(credentials, Credentials::getId);

        // For every account, check which month it was created and add the data for that account for all following
        // months until last month or the month it was deleted.
        for (Account account : accounts) {
            Credentials credential = credentialsById.get(account.getCredentialsId());
            List<CredentialsEvent> events = credentialsEventRepository
                    .findByUserIdAndCredentialsId(userData.user.getId(), credential.getId());
            CredentialsEvent createdEvent = null;
            CredentialsEvent deletedEvent = null;

            for (CredentialsEvent event : events) {
                if (Objects.equal(event.getStatus(), CredentialsStatus.CREATED)) {
                    createdEvent = event;
                } else if (Objects.equal(event.getStatus(), CredentialsStatus.DELETED)) {
                    deletedEvent = event;
                }
            }

            Date lastMonthForAccount = deletedEvent == null ?
                    currentMonth :
                    getFirstDayOfMonthAndInclusiveStartTimeFromDate(deletedEvent.getTimestamp());

            if (createdEvent == null) {
                throw new AvoidThisUserException("Could not find a CREATED event for an account");
            }

            Date month = getFirstDayOfMonthAndInclusiveStartTimeFromDate(createdEvent.getTimestamp());

            while (!Objects.equal(month, lastMonthForAccount)) {
                MonthlyData monthlyData = monthlyDataToMonthDate.get(month);
                monthlyData.addAccountData(account);
                month = DateUtils.addMonths(month, 1);
            }
        }
    }

    private Map<Date, MonthlyData> createMonthlyDataToMonthMap(UserData userData) {
        Map<Date, MonthlyData> monthlyDataToMonthDate = Maps.newHashMap();
        Date month = getFirstDayOfMonthAndInclusiveStartTimeFromDate(userData.user.getCreated());

        while (!Objects.equal(month, currentMonth)) {
            monthlyDataToMonthDate.put(month, new MonthlyData());
            month = DateUtils.addMonths(month, 1);
        }
        return monthlyDataToMonthDate;
    }

    private void fetchAndSetTransactionData(final UserData userData, Map<Date, MonthlyData> monthlyDataToMonthDate) {
        List<Transaction> allTransactions = transactionDao.findAllByUserId(userData.user.getId());

        for (Transaction transaction : allTransactions) {
            Date month = getFirstDayOfMonthAndInclusiveStartTimeFromDate(new Date(transaction.getInserted()));

            while (!Objects.equal(month, currentMonth)) {
                MonthlyData monthlyData = monthlyDataToMonthDate.get(month);
                monthlyData.addTransactionCategoryData(transaction);
                month = DateUtils.addMonths(month, 1);
            }
        }
    }

    private String credentialsToString(List<Credentials> credentials) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < credentials.size(); i++) {
            Credentials credential = credentials.get(i);
            builder.append("{");
            builder.append(credential.getProviderName());
            builder.append(", ");
            builder.append(credential.getStatus());
            builder.append(", ");
            builder.append(credential.getUpdated());
            builder.append(i == credentials.size() - 1 ? "}" : "}, ");
        }
        builder.append("]");
        return builder.toString();
    }

    private boolean fetchSystemProperties() {
        String filename = "data-" + ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date()) + ".gz";
        File output = new File("/var/lib/tink/commands/creditscoring", filename);

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

            // Make sure file is only readable and writable by creator and nobody else.
            if (!output.setReadable(false, false) || !output.setReadable(true, true) || !output
                    .setWritable(false, false) || !output.setWritable(true, true)) {
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

    private static Date getFirstDayOfMonthAndInclusiveStartTimeFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        DateUtils.setInclusiveStartTime(calendar);
        return calendar.getTime();
    }

    private class UserData {
        private List<String> userOutput = Lists.newArrayList();
        private List<String> featureNames = Lists.newArrayList();
        User user;
        int nrOfDaysSinceCreated;

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

    /**
     * Holds the data specific for an account type.
     */
    private class AccountTypeData {
        int accountCount;
        double totalBalance;
    }

    /**
     * Holds the data specific for a transaction category.
     */
    private class TransactionCategoryData {
        int transactionCount;
        double totalAmount;
    }

    /**
     * Holds the data specific for a month in the lifetime of a user.
     */
    private class MonthlyData {

        Map<String, TransactionCategoryData> categoryDataByCategoryCode = Maps.newHashMap();
        Map<String, AccountTypeData> accountTypeDataByType = Maps.newHashMap();
        Double yearlyIncomeByCapital;
        Double yearlyIncomeByService;
        String creditScore = "N/A";
        Double credit;
        List<String> nonPayments = Lists.newArrayList();

        void addTransactionCategoryData(Transaction transaction) {
            String categoryId = transaction.getCategoryId();
            Category category = categoryRepository.findById(categoryId);

            if (!categoryDataByCategoryCode.containsKey(category.getCode())) {
                categoryDataByCategoryCode.put(category.getCode(), new TransactionCategoryData());
            }

            TransactionCategoryData categoryData = categoryDataByCategoryCode.get(category.getCode());
            categoryData.totalAmount += transaction.getAmount();
            categoryData.transactionCount++;
        }

        public void addAccountData(Account account) {
            String type = getAccountTypeFromAccount(account);

            if (!accountTypeDataByType.containsKey(type)) {
                accountTypeDataByType.put(type, new AccountTypeData());
            }

            AccountTypeData accountTypeData = accountTypeDataByType.get(type);
            accountTypeData.accountCount++;
            accountTypeData.totalBalance += account.getBalance();
        }

        private String getAccountTypeFromAccount(Account account) {
            String type;

            if (Objects.equal(account.getType(), AccountTypes.MORTGAGE) || Objects
                    .equal(account.getType(), AccountTypes.LOAN)) {
                Loan loan = loanDataRepository.findMostRecentOneByAccountId(account.getId());
                if (loan == null || Objects.equal(loan.getType(), Loan.Type.OTHER)) {
                    type = "OTHER_LOAN";
                } else {
                    type = loan.getType().toString();
                }
            } else {
                type = account.getType().toString();
            }

            return type;
        }

        Map<String, String> getFeatureValuesToFeatureNamesMap() {
            Map<String, String> featureValuesToFeatureNames = Maps.newLinkedHashMap();
            featureValuesToFeatureNames
                    .put("yearlyIncomeByService", yearlyIncomeByService == null ? "N/A" : Double.toString(
                            yearlyIncomeByService));
            featureValuesToFeatureNames
                    .put("yearlyIncomeByCapital", yearlyIncomeByCapital == null ? "N/A" : Double.toString(
                            yearlyIncomeByCapital));
            featureValuesToFeatureNames.put("creditScore", creditScore);
            featureValuesToFeatureNames.put("credit", credit == null ? "N/A" : Double.toString(credit));
            featureValuesToFeatureNames
                    .put("nonPayments", nonPayments.isEmpty() ? "N/A" : nonPayments.toString());
            featureValuesToFeatureNames.put("[accountType:{accountCount, totalBalance}]", getAccountDataString());
            featureValuesToFeatureNames.put("[category:{transactionCount, totalAmount}]", getCategoryDataString());
            return featureValuesToFeatureNames;
        }

        public String getFeatureString() {
            Map<String, String> featureValuesToFeatureNames = getFeatureValuesToFeatureNamesMap();
            List<String> features = Lists.newArrayList();
            for (String feature : featureValuesToFeatureNames.values()) {
                features.add(feature);
            }
            return Joiner.on(", ").join(features);
        }

        public String getFeatureNameString() {
            Map<String, String> featureValuesToFeatureNames = getFeatureValuesToFeatureNamesMap();
            List<String> featureNames = Lists.newArrayList();
            for (String featureName : featureValuesToFeatureNames.keySet()) {
                featureNames.add(featureName);
            }
            return "[month:{" + Joiner.on(", ").join(featureNames) + "}]";
        }

        private String getCategoryDataString() {
            List<String> dataList;
            List<String> allDataList = Lists.newArrayList();

            for (String categoryCode : categoryDataByCategoryCode.keySet()) {
                dataList = Lists.newArrayList();
                TransactionCategoryData data = categoryDataByCategoryCode.get(categoryCode);
                dataList.add(Integer.toString(data.transactionCount));
                dataList.add(Double.toString(data.totalAmount));
                allDataList.add(categoryCode + ":{" + Joiner.on(", ").join(dataList) + "}");
            }

            return "[" + Joiner.on(", ").join(allDataList) + "]";
        }

        public String getAccountDataString() {
            List<String> dataList;
            List<String> allDataList = Lists.newArrayList();

            for (String accountType : accountTypeDataByType.keySet()) {
                dataList = Lists.newArrayList();
                AccountTypeData data = accountTypeDataByType.get(accountType);
                dataList.add(Integer.toString(data.accountCount));
                dataList.add(Double.toString(data.totalBalance));
                allDataList.add(accountType + ":{" + Joiner.on(", ").join(dataList) + "}");
            }

            return "[" + Joiner.on(", ").join(allDataList) + "]";
        }
    }

    private class AvoidThisUserException extends Exception {
        public AvoidThisUserException(String message) {
            super(message);
        }
    }
}
