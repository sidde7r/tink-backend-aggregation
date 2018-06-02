package se.tink.backend.system.cli.extraction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.common.repository.cassandra.ApplicationArchiveRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.ApplicationRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.ApplicationRow;
import se.tink.backend.core.County;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.backend.core.Field;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudCreditScoringContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIncomeContent;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Market;
import se.tink.backend.core.Municipality;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ApplicationArchiveRow;
import se.tink.backend.core.application.ConfirmationFormListData;
import se.tink.backend.core.application.FieldData;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.enums.Gender;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.Doubles;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class ExtractMortgageDataCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final double INTEREST_RATE_TOLERANCE = 0.0002; // 0.02%
    private static final LogUtils log = new LogUtils(ExtractMortgageDataCommand.class);

    private int year;
    private File outFile;

    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private FraudDetailsRepository fraudDetailsRepository;
    private LoanDataRepository loanDataRepository;
    private UserRepository userRepository;
    private ApplicationRepository applicationRepository;
    private ApplicationArchiveRepository applicationArchiveRepository;

    private ServiceConfiguration configuration;

    private long timeout = 24;
    private TimeUnit timeoutUnit = TimeUnit.HOURS;
    private AtomicInteger userCount = new AtomicInteger();
    private AtomicInteger userFilteredCount = new AtomicInteger();

    private static final TypeReference<List<ConfirmationFormListData>> FORM_LIST_TYPE_REFERENCE = new TypeReference<List<ConfirmationFormListData>>() {
    };
    private static Splitter SPLITTER = Splitter.on(CharMatcher.WHITESPACE);

    public static Function<FraudDetails, FraudDetailsContentType> FRAUD_DETAILS_TO_TYPE = FraudDetails::getType;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#.00");

    private static final Supplier<Set<String>> MUNICIPALITIES = Suppliers.memoizeWithExpiration(
            () -> {

                Set<String> municipalities = Sets.newConcurrentHashSet();

                try {
                    List<County> counties = OBJECT_MAPPER.readValue(new File(
                            "data/seeding/counties-and-municipalities.json"), new TypeReference<List<County>>() {
                    });

                    for (County county : counties) {
                        for (Municipality municipality : county.getMunicipalities()) {
                            municipalities.add(municipality.getName().toLowerCase());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return municipalities;
            }, 30, TimeUnit.MINUTES);

    public ExtractMortgageDataCommand() {
        super("extract-mortgage-data", "Extract mortgage data.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        this.configuration = configuration;

        year = Calendar.getInstance().get(Calendar.YEAR);

        accountRepository = serviceContext.getRepository(AccountRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        fraudDetailsRepository = serviceContext.getRepository(FraudDetailsRepository.class);
        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        applicationRepository = serviceContext.getRepository(ApplicationRepository.class);
        applicationArchiveRepository = serviceContext.getRepository(ApplicationArchiveRepository.class);

        final Filter filter = new Filter();

        filter.minAge = Integer.getInteger("minAge", 18);
        filter.maxAge = Integer.getInteger("maxAge", 65);
        filter.minAmount = (double) Integer.getInteger("minAmount", 0);
        filter.maxAmount = (double) Integer.getInteger("maxAmount", 10000000);
        filter.minCreditScore = Integer.getInteger("minCreditScore", 0);
        filter.minInterestRate = Integer.getInteger("minInterestRate", 0) / 10000d; // From basis points to decimal
        filter.maxInterestRate = Integer.getInteger("maxInterestRate", 400) / 10000d; // From basis points to decimal
        filter.minPostalCode = Integer.getInteger("minPostalCode", 0);
        filter.maxPostalCode = Integer.getInteger("maxPostalCode", 999999);
        filter.minSalary = (double) Integer.getInteger("minSalary", 0) * 12; // From monthly to yearly
        filter.maxSalary = (double) Integer.getInteger("maxSalary", 1000000) * 12; // From monthly to yearly

        final ExecutorService executor = Executors.newFixedThreadPool(20);
        String date = ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date());
        outFile = new File(System.getProperty("outFile"), "mortgage-data_" + date + ".txt");

        Files.write("gender" + "\t" + "age" + "\t" + "creditScore" + "\t" + "incomeByService" + "\t"
                        + "postalCode" + "\t" + "municipality" + "\t" + "credentialsCount" + "\t"
                        + "mortgageOtherBankThanChecking" + "\t" + "mortgageSize" + "\t" + "oldInterest" + "\t" + "newInterest"
                        + "\n",
                outFile, Charsets.UTF_8);

        userRepository.streamAll().forEach(user -> executor.execute(() -> process(user, filter)));

        executor.shutdown();
        executor.awaitTermination(timeout, timeoutUnit);
    }

    private void process(User user, Filter filter) {
        try {
            if (!Objects.equal(user.getProfile().getMarketAsCode(), Market.Code.SE)) {
                return;
            }

            final String ssn = user.getProfile().getFraudPersonNumber();

            // Ignore users without ID-Koll.
            if (Strings.isNullOrEmpty(ssn)) {
                return;
            }

            Profile profile = new Profile(user.getId());

            SocialSecurityNumber.Sweden swedish = new SocialSecurityNumber.Sweden(ssn);

            Gender gender = swedish.getGender();
            int birthYear = swedish.getBirthYear();

            if (birthYear != -1) {
                profile.age = (year - birthYear);
            }
            if (gender != null) {
                profile.gender = gender.toLowerCase();
            }

            populateIdentitySpecificDetails(profile);
            populateAccountSpecificDetails(profile, swedish.asString());
            populateApplicationSpecificDetails(profile);

            if (filter.qualifies(profile)) {
                userCount.incrementAndGet();
                Files.append(profile.toString(), outFile, Charsets.UTF_8);
            } else {
                userFilteredCount.incrementAndGet();
            }

            if (userCount.get() % 1000 == 0 || userFilteredCount.get() % 1000 == 0) {
                log.info("Have included " + userCount.get() + " and excluded " + userFilteredCount.get() + " users");
            }

        } catch (Exception e) {
            log.error("Something went wrong processing user " + user.getId(), e);
        }
    }

    private void populateIdentitySpecificDetails(Profile profile) throws Exception {
        List<FraudDetails> details = fraudDetailsRepository.findAllByUserId(profile.userId);

        if (details != null && !details.isEmpty()) {
            ImmutableListMultimap<FraudDetailsContentType, FraudDetails> detailsByType = Multimaps.index(details,
                    FRAUD_DETAILS_TO_TYPE);

            FraudDetails creditScoreDetails = getMostRecentEntry(detailsByType.get(FraudDetailsContentType.SCORING));

            if (creditScoreDetails != null) {
                FraudCreditScoringContent content = (FraudCreditScoringContent) creditScoreDetails.getContent();
                profile.creditScore = content.getScore();
            }

            FraudDetails addressDetails = getMostRecentEntry(detailsByType.get(FraudDetailsContentType.ADDRESS));

            if (addressDetails != null) {
                FraudAddressContent content = (FraudAddressContent) addressDetails.getContent();
                profile.postalCode = content.getPostalcode();
                profile.municipality = getMunicipality(content);
            }

            FraudDetails incomeDetails = getMostRecentEntry(detailsByType.get(FraudDetailsContentType.INCOME));

            if (incomeDetails != null) {
                FraudIncomeContent content = (FraudIncomeContent) incomeDetails.getContent();
                profile.incomeByService = content.getIncomeByService();
            }
        }
    }

    private String getMunicipality(final FraudAddressContent address) {

        if (!Strings.isNullOrEmpty(address.getCommunity())) {
            if (MUNICIPALITIES.get().contains(address.getCommunity().toLowerCase())) {
                return address.getCommunity();
            }
        }

        if (!Strings.isNullOrEmpty(address.getCity())) {
            if (MUNICIPALITIES.get().contains(address.getCity().toLowerCase())) {
                return address.getCity();
            }
        }

        return "unknown";
    }

    private void populateAccountSpecificDetails(Profile profile, final String ssn) {
        List<Credentials> allCredentials = credentialsRepository.findAllByUserId(profile.userId);

        if (allCredentials == null || allCredentials.isEmpty()) {
            return;
        }

        Predicate<Credentials> predicate = credentials -> !Objects.equal("csn", credentials.getProviderName()) &&
                !Objects.equal("creditsafe", credentials.getProviderName()) &&
                credentials.getStatus() != CredentialsStatus.DISABLED &&
                Objects.equal(ssn, credentials.getField(Field.Key.USERNAME)) &&
                !DemoCredentials.isDemoUser(credentials.getField(Field.Key.USERNAME));

        final ImmutableMap<String, Credentials> validCredentialsById = FluentIterable
                .from(allCredentials)
                .filter(predicate)
                .uniqueIndex(Credentials::getId);

        profile.credentialsCount = validCredentialsById.keySet().size();

        List<Account> accounts = accountRepository.findByUserId(profile.userId);

        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        ImmutableListMultimap<String, Account> checkingAccountsByCredentialsId = FluentIterable.from(accounts)
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(AccountPredicate.IS_CHECKING_ACCOUNT)
                .filter(AccountPredicate.IS_FAVORED)
                .filter(account -> validCredentialsById.containsKey(account.getCredentialsId()))
                .index(Account::getCredentialsId);

        ImmutableListMultimap<String, Account> loanAccountsByCredentialsId = FluentIterable.from(accounts)
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(AccountPredicate.IS_NOT_CLOSED)
                .filter(Predicates.or(AccountPredicate.IS_LOAN, AccountPredicate.IS_MORTGAGE))
                .filter(account -> validCredentialsById.containsKey(account.getCredentialsId()))
                .index(Account::getCredentialsId);

        // Check if there is a checking account in another bank than loan account.

        boolean sameAsLoan = true;

        checkingCredentials:
        for (String credentialsId1 : checkingAccountsByCredentialsId.keySet()) {
            String checkingAccountProvider = validCredentialsById.get(credentialsId1).getProviderName();

            for (String credentialsId2 : checkingAccountsByCredentialsId.keySet()) {
                String loanAccountProvider = validCredentialsById.get(credentialsId2).getProviderName();

                if (!Objects.equal(checkingAccountProvider, loanAccountProvider)) {
                    sameAsLoan = false;
                    break checkingCredentials;
                }
            }
        }

        profile.mortgageOtherBankThanChecking = !sameAsLoan;

        for (Credentials c : validCredentialsById.values()) {

            Mortgage mortgage = null;
            double amountInterestProductSum = 0;

            for (Account a : loanAccountsByCredentialsId.get(c.getId())) {

                Loan loan = loanDataRepository.findMostRecentOneByAccountId(a.getId());

                if (loan != null && Objects.equal(loan.getType(), Loan.Type.MORTGAGE) && !a.isClosed()) {

                    // If balance or interest rate would be missing, we can't do anything with the data either way.
                    if (loan.getBalance() == null || loan.getInterest() == null) {
                        continue;
                    }

                    // Demo loan. Ignore.
                    if (loan.getNumMonthsBound() != null && loan.getNumMonthsBound() == 1
                            && Doubles.fuzzyEquals(loan.getBalance(), -2300000d, 0.1)
                            && Doubles.fuzzyEquals(loan.getInterest(), 0.019, 0.0001)) {
                        continue;
                    }

                    if (mortgage == null) {
                        mortgage = new Mortgage();
                    }

                    mortgage.amount += Math.abs(loan.getBalance());
                    amountInterestProductSum += Math.abs(loan.getBalance()) * loan.getInterest();
                }
            }

            if (mortgage != null && mortgage.amount > profile.mortgage.amount) {
                double interest = amountInterestProductSum / mortgage.amount * 100;
                mortgage.oldInterest = DECIMAL_FORMATTER.format(interest);
                profile.mortgage = mortgage;
            }
        }
    }

    private void populateApplicationSpecificDetails(Profile profile) {
        List<ApplicationRow> applicationRows = applicationRepository.findAllByUserId(profile.userId);

        for (ApplicationRow applicationRow : applicationRows) {
            if (Objects.equal(applicationRow.getType(),
                    ApplicationType.SWITCH_MORTGAGE_PROVIDER.toString())
                    && Objects
                    .equal(applicationRow.getStatus(), ApplicationStatusKey.SIGNED.name())) {

                Optional<ApplicationArchiveRow> applicationArchiveRowOptional = applicationArchiveRepository
                        .findByUserIdAndApplicationId(UUIDUtils.fromTinkUUID(profile.userId),
                                UUIDUtils.fromTinkUUID(applicationRow.getId()));

                if (applicationArchiveRowOptional.isPresent()) {
                    ApplicationArchiveRow applicationArchiveRow = applicationArchiveRowOptional.get();
                    List<ConfirmationFormListData> formList = SerializationUtils
                            .deserializeFromString(applicationArchiveRow.getContent(), FORM_LIST_TYPE_REFERENCE);

                    if (formList != null) {
                        for (ConfirmationFormListData data : formList) {
                            if (Objects.equal("Bolån", data.getTitle())) {
                                for (FieldData field : data.getFields()) {
                                    if (Objects.equal("Befintliga lån", field.getTitle())) {
                                        List<List<String>> currentLoanDataListList = field.getValues();
                                        profile.mortgage.oldInterest = getLoanInterestRate(currentLoanDataListList);
                                        profile.mortgage.amount = getLoanAmount(currentLoanDataListList);
                                    }
                                    if (Objects.equal("Nytt lån", field.getTitle())) {
                                        List<List<String>> currentLoanDataListList = field.getValues();
                                        profile.mortgage.newInterest = getLoanInterestRate(currentLoanDataListList);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    static double getLoanAmount(List<List<String>> currentLoanDataListList) {
        if (currentLoanDataListList != null) {
            for (List<String> currentLoanDataList : currentLoanDataListList) {
                for (String loanData : currentLoanDataList) {
                    if (loanData.contains("kr")) {
                        String loanAmount = loanData.substring(0, loanData.indexOf("kr"));
                        if (!Strings.isNullOrEmpty(loanAmount)) {
                            return StringUtils.parseAmount(loanAmount.replaceAll(" ", ""));
                        }
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Data could both look like:
     * "SBAB bolån 1,48 % ränta" and "SBAB 1,5% ränta"
     */
    static String getLoanInterestRate(List<List<String>> currentLoanDataListList) {
        if (currentLoanDataListList != null) {
            for (List<String> currentLoanDataList : currentLoanDataListList) {
                for (String loanData : currentLoanDataList) {

                    Iterable<String> dataList = SPLITTER.split(loanData);

                    for (int i = 0; i < Iterables.size(dataList); i++) {
                        String data = Iterables.get(dataList, i);
                        if (data.contains("%") && data.length() > 1) {
                            return data.replace("%", "");
                        }
                        if (data.contains("%") && data.length() == 1 && i > 0) {
                            return Iterables.get(dataList, i - 1);
                        }
                    }
                }
            }
        }
        return "";
    }

    private boolean containsDifferentInterestRates(List<Loan> entries) {
        if (entries == null || entries.size() < 2) {
            return false;
        }

        for (int i = 1; i < entries.size(); i++) {
            if (!Doubles.fuzzyEquals(entries.get(i - 1).getInterest(), entries.get(i).getInterest(),
                    INTEREST_RATE_TOLERANCE)) {
                return true;
            }
        }

        return false;
    }

    private FraudDetails getMostRecentEntry(List<FraudDetails> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }

        return details.stream().max(Orderings.FRAUD_DETAILS_DATE).get();
    }

    private class Profile {

        public String userId;
        public Integer age;
        public Integer creditScore;
        public Integer credentialsCount;
        public Double incomeByService;
        public String postalCode;
        public String municipality;
        public String gender;
        public boolean mortgageOtherBankThanChecking;
        public Mortgage mortgage;

        public Profile(String userId) {
            this.userId = userId;
            this.mortgage = new Mortgage();
        }

        @Override
        public String toString() {
            return gender + "\t" +
                    age + "\t" +
                    creditScore + "\t" +
                    (incomeByService != null ? incomeByService.intValue() : null) + "\t" +
                    postalCode + "\t" +
                    municipality + "\t" +
                    credentialsCount + "\t" +
                    mortgageOtherBankThanChecking + "\t" +
                    (mortgage.amount != 0 ? (int) mortgage.amount : null) + "\t" +
                    mortgage.oldInterest + "\t" +
                    mortgage.newInterest + "\n";

        }
    }

    private class Mortgage {

        public double amount;
        public String oldInterest;
        public String newInterest;
    }

    private class Filter {

        // User
        public int minAge;
        public int maxAge;
        public int minCreditScore;
        public int minPostalCode;
        public int maxPostalCode;
        public double minSalary; // Yearly
        public double maxSalary; // Yearly

        // Mortgage
        public double minAmount;
        public double maxAmount;
        public double minInterestRate;
        public double maxInterestRate;

        public boolean qualifies(Profile profile) {

            if (profile.age == null || profile.age < minAge || profile.age > maxAge) {
                return false;
            }

            if (profile.creditScore == null || profile.creditScore < minCreditScore) {
                return false;
            }

            if (Strings.isNullOrEmpty(profile.postalCode)) {
                return false;
            }

            int postalCode = Integer.valueOf(profile.postalCode);

            if (postalCode < minPostalCode || postalCode > maxPostalCode) {
                return false;
            }

            if (profile.incomeByService == null || profile.incomeByService < minSalary
                    || profile.incomeByService > maxSalary) {
                return false;
            }

            return true;
        }

        public boolean qualifies(Profile profile, Mortgage mortgage) {

            if (Double.valueOf(mortgage.oldInterest) < minInterestRate
                    || Double.valueOf(mortgage.oldInterest) > maxInterestRate) {
                return false;
            }

            if (Double.valueOf(mortgage.amount) < minAmount || Double.valueOf(mortgage.amount) > maxAmount) {
                return false;
            }

            return true;
        }
    }
}
