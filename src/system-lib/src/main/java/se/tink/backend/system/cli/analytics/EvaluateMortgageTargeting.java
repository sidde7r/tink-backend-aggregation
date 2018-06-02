package se.tink.backend.system.cli.analytics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Injector;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.json.JSONArray;
import org.json.JSONObject;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.application.PropertyUtils;
import se.tink.backend.common.config.SEBIntegrationConfiguration;
import se.tink.backend.common.config.SEBMortgageIntegrationConfiguration;
import se.tink.backend.common.config.SbabIntegrationConfiguration;
import se.tink.backend.common.config.SbabMortgageIntegrationConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.net.BasicJerseyClientFactory;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.County;
import se.tink.backend.core.Credentials;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.backend.core.Field;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudCreditScoringContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIncomeContent;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.Loan;
import se.tink.backend.core.Market;
import se.tink.backend.core.Municipality;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.Doubles;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;
import se.tink.libraries.date.DateUtils;

public class EvaluateMortgageTargeting extends ServiceContextCommand<ServiceConfiguration> {

    private static final double INTEREST_RATE_TOLERANCE = 0.0002; // 0.02%

    private static final LogUtils log = new LogUtils(EvaluateMortgageTargeting.class);
    
    private int year;
    
    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private FraudDetailsRepository fraudDetailsRepository;
    private LoanDataRepository loanDataRepository;
    private UserRepository userRepository;
    
    private ServiceConfiguration configuration;
    
    private boolean fetchProductInformation;
    private RateLimiter productInformationRateLimiter;
    
    private long timeout = 24;
    private TimeUnit timeoutUnit = TimeUnit.HOURS;
    
    private Date threeMonthsAhead = DateUtils.addMonths(new Date(), 3);

    public static Function<FraudDetails, FraudDetailsContentType> FRAUD_DETAILS_TO_TYPE = FraudDetails::getType;
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
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
    
    public EvaluateMortgageTargeting() {
        super("evaluate-mortgage-targeting", "Evaluate mortgage targeting.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        this.configuration = configuration;
        this.fetchProductInformation = Boolean.getBoolean("fetchProductInformation");
        
        if (fetchProductInformation) {
            this.productInformationRateLimiter = RateLimiter.create(Integer.getInteger("frequency", 20));
        }
        
        year = Calendar.getInstance().get(Calendar.YEAR);
        
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        fraudDetailsRepository = serviceContext.getRepository(FraudDetailsRepository.class);
        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        
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

        userRepository.streamAll().forEach(user -> executor.execute(() -> process(user, filter)));
        
        executor.shutdown();
        executor.awaitTermination(timeout, timeoutUnit);
    }
    
    private void process(User user, Filter filter) {
        
        if (!Objects.equal(user.getProfile().getMarketAsCode(), Market.Code.SE)) {
            return;
        }
        
        final String ssn = user.getProfile().getFraudPersonNumber();
        
        // Ignore users without ID-Koll.
        if (Strings.isNullOrEmpty(ssn)) {
            return;
        }
        
        Profile profile = new Profile(user.getId()); 

        try {
            profile.age = year - Integer.valueOf(ssn.substring(0, 4));    
        } catch (Exception e) {
            log.warn(profile.userId, "Unable to parse identity number.", e);
        }
        
        populateIdentitySpecificDetails(profile);
        populateAccountSpecificDetails(profile, ssn);
        
        profile.qualifies = filter.qualifies(profile);
        
        if (profile.mortgages != null && !profile.mortgages.isEmpty()) {
            Mortgage candidate = null;
            
            for (Mortgage mortgage : profile.mortgages) {
                if (filter.qualifies(profile, mortgage)) {
                    // Choose the qualified mortgage with the highest cost.
                    if (candidate == null
                            || (candidate.interest * candidate.amount) < (mortgage.interest * mortgage.amount)) {
                        candidate = mortgage;
                    }
                }
            }
            
            if (candidate != null) {
                profile.candidate = candidate;
                
                if (fetchProductInformation) {
                    populateProductInformation(profile);
                }
            }
        }

        log.info(profile.toString());
    }
    
    private void populateIdentitySpecificDetails(Profile profile) {
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
                profile.livesInHousingCooperative = PropertyUtils.isApartment(content, detailsByType);
                profile.postalCode = content.getPostalcode();
                profile.municipality = getMunicipality(content);
            }
            
            
            FraudDetails incomeDetails = getMostRecentEntry(detailsByType.get(FraudDetailsContentType.INCOME));
            
            if (incomeDetails != null) {
                FraudIncomeContent content = (FraudIncomeContent) incomeDetails.getContent();
                profile.incomeByService = content.getIncomeByService();
            }

            
            FraudDetails realEstateDetails = getMostRecentEntry(detailsByType
                    .get(FraudDetailsContentType.REAL_ESTATE_ENGAGEMENT));
            if (realEstateDetails != null) {
                profile.hasRealEstate = !Objects.equal(realEstateDetails.getStatus(), FraudStatus.EMPTY);
            }

            FraudDetails nonPaymentDetails = getMostRecentEntry(detailsByType.get(FraudDetailsContentType.NON_PAYMENT));
            if (nonPaymentDetails != null) {
                profile.hasRecordOfNonPayment = !Objects.equal(nonPaymentDetails.getStatus(), FraudStatus.EMPTY);
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
        
        return "Okänd";
    }
    
    private void populateAccountSpecificDetails(Profile profile, String ssn) {
        profile.isSEBCustomer = false;
        
        List<Credentials> allCredentials = credentialsRepository.findAllByUserId(profile.userId);

        if (allCredentials == null || allCredentials.isEmpty()) {
            return;
        }
        
        List<Account> accounts = accountRepository.findByUserId(profile.userId);
        
        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        ImmutableListMultimap<String, Account> accountsByCredentialsId = FluentIterable.from(accounts)
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(Predicates.or(AccountPredicate.IS_LOAN, AccountPredicate.IS_MORTGAGE))
                .index(Account::getCredentialsId);

        for (Credentials c : allCredentials) {
            
            // Ignore credentials that have a different username than the user's SSN.
            if (!Objects.equal(ssn, c.getField(Field.Key.USERNAME))) {
                continue;
            }

            if (DemoCredentials.isDemoUser(c.getField(Field.Key.USERNAME))) {
                continue;
            }
            
            if (Objects.equal(c.getProviderName(), "seb") || Objects.equal(c.getProviderName(), "seb-bankid")) {
                profile.isSEBCustomer = true;
            }
            
            Mortgage mortgage = null;
            double amountInterestProductSum = 0;
            
            for (Account a : accountsByCredentialsId.get(c.getId())) {

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
                        mortgage.provider = c.getProviderName();
                    }

                    mortgage.amount += Math.abs(loan.getBalance());
                    mortgage.parts++;
                    mortgage.terms = getTerms(mortgage, loan, a);

                    amountInterestProductSum += Math.abs(loan.getBalance()) * loan.getInterest();

                    // If terms are unknown, make a qualified guess.
                    if (Objects.equal(mortgage.terms, "unknown")) {
                        if (mortgage.parts > 1) {
                            double portfolioInterest = amountInterestProductSum / mortgage.amount;
                            
                            if (Doubles.fuzzyEquals(portfolioInterest, loan.getInterest(), INTEREST_RATE_TOLERANCE)) {
                                // This part has the same interest rate as the rest of the portfolio (almost, at least).
                                // We make an optimistic interpretation of this, assuming they're all floating. This
                                // assumption is based on the assumption that you normally don't bind all parts with the
                                // same (fixed) terms, since it wouldn't create any diversification. Different fixed
                                // terms would probably yield different rates (which would take you to the 'else' case).
                                mortgage.terms = "floating";
                            } else {
                                // This part has a different interest rate than the portfolio as a whole. Most likely a
                                // portfolio mixed of different terms. Doesn't necessarily mean that any of them are
                                // floating, but we assume it is.
                                mortgage.terms = "mixed";
                            }
                        }
                    }
                }
            }
            
            if (mortgage != null) {
                mortgage.interest = amountInterestProductSum / mortgage.amount; 
                profile.mortgages.add(mortgage);
            }
        }
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

    private String getTerms(Mortgage mortgage, Loan loan, Account account) {
        if (loan.getNumMonthsBound() == null) {
            if (!Objects.equal(mortgage.terms, "mixed")) {
                if (isLoanPartFloating(loan, account)) {
                    if (Strings.isNullOrEmpty(mortgage.terms)) {
                        return "floating";
                    } else if (Objects.equal(mortgage.terms, "fixed")) {
                        return "mixed";
                    }
                } else {
                    return "unknown";
                }
            }
        } else if (isLoanPartFloating(loan, account)) {
            if (Strings.isNullOrEmpty(mortgage.terms)) {
                return "floating";
            } else if (Objects.equal(mortgage.terms, "fixed")) {
                return "mixed";
            }
        } else {
            if (Strings.isNullOrEmpty(mortgage.terms)) {
                return "fixed";
            } else if (Objects.equal(mortgage.terms, "floating")) {
                return "mixed";
            }
        }
        
        // Unchanged
        return mortgage.terms;
    }
    
    private boolean isLoanPartFloating(Loan loan, Account account) {
        if (loan.getNextDayOfTermsChange() != null && threeMonthsAhead.after(loan.getNextDayOfTermsChange())) {
            return true;
        }

        if (loan.getNumMonthsBound() == null) {
            if (containsDifferentInterestRates(loanDataRepository.findAllByAccountId(account.getId()))) {
                return true;
            }
        } else if (loan.getNumMonthsBound() <= 3) {
            return true;
        }

        return false;
    }

    private FraudDetails getMostRecentEntry(List<FraudDetails> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }

        return details.stream().max(Orderings.FRAUD_DETAILS_DATE).get();
    }
    
    private void populateProductInformation(Profile profile) {
        populateProductInformationFromSbab(profile);
        populateProductInformationFromSeb(profile);
    }
    
    private void populateProductInformationFromSbab(Profile profile) {
        
        if (productInformationRateLimiter != null) {
            productInformationRateLimiter.acquire();
        }
        
        SbabMicroClient sbabClient = new SbabMicroClient(configuration.getIntegrations().getSbab());
        
        int loanAmount = (int) profile.candidate.amount;
        int marketValue = (int) (loanAmount / 0.7d); // Assume 70% debt ratio.
        int numberOfApplicants = 1; // We don't know if a co-applicant is present, so let's be conservative.
        
        try {
            profile.sbabInterestRate = sbabClient.getInterestRate(marketValue, loanAmount, numberOfApplicants);
        } catch (Exception e) {
            log.error(profile.userId, "Unable to fetch interest rate from SBAB.", e);
        }
    }
    
    private void populateProductInformationFromSeb(Profile profile) {
        
        if (productInformationRateLimiter != null) {
            productInformationRateLimiter.acquire();
        }
        
        SebMicroClient sebClient = new SebMicroClient(configuration.getIntegrations().getSeb());

        double marketValue = profile.candidate.amount / 0.7d; // Assume 70% debt ratio.
        String propertyType = profile.livesInHousingCooperative ? "01" : "02";

        try {
            profile.sebInterestRate = sebClient.getInterestRate(profile.candidate.amount, marketValue, profile.age,
                    propertyType, profile.municipality);
        } catch (Exception e) {
            log.error(profile.userId, "Unable to fetch interest rate from SEB.", e);
        }
    }
    
    private class SebMicroClient {
        
        private static final String API_VERSION = "v1.0";
        private static final String INTEREST_RATES_PATH = "/rates";
        
        private final Client client;
        private final String mortgageBaseUrl;
        private final HashMap<String, String> headers;
        
        public SebMicroClient(SEBIntegrationConfiguration configuration) {
            
            SEBMortgageIntegrationConfiguration mortgageConfiguration = configuration.getMortgage();
            
            this.client = new BasicJerseyClientFactory().createCookieClientWithoutSSL();
            this.headers = mortgageConfiguration.getHttpHeaders();
            
            String schema = mortgageConfiguration.isHttps() ? "https://" : "http://";
            this.mortgageBaseUrl = String.format("%s%s/tink/loans/%s/mortgages", schema,
                    mortgageConfiguration.getTargetHost(), API_VERSION);
        }
        
        // propertyType: 01 = apartment; 02 = villa; 03 = other
        public double getInterestRate(double loanAmount, double marketValue, int age, String propertyType,
                String municipality) throws Exception {

            MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
            queryParameters.add("age", String.valueOf(age));
            queryParameters.add("loan_amount", String.valueOf(loanAmount));
            queryParameters.add("property_type", propertyType);
            queryParameters.add("market_value", String.valueOf(marketValue));
            queryParameters.add("new_placement_volume", String.valueOf(loanAmount));
            queryParameters.add("municipality", municipality);

            String rawResponse = createJsonRequest(getMortgageUrl(INTEREST_RATES_PATH), queryParameters).get(
                    String.class);
            
            JSONObject response = new JSONObject(rawResponse);

            return response.getDouble("indicative_rate") / 100d;
        }
        
        private String getMortgageUrl(String path) {
            return getUrl(mortgageBaseUrl, path);
        }
        
        private String getUrl(String baseUrl, String path) {
            return baseUrl + path;
        }
        
        private Builder createJsonRequest(String url, MultivaluedMap<String, String> queryParameters) {
            Builder resource = client.resource(url)
                    .queryParams(queryParameters)
                    .header("User-Agent", "Tink (+https://www.tink.se/; noc@tink.se)")
                    .type(MediaType.APPLICATION_JSON + "; charset=utf-8")
                    .accept(MediaType.APPLICATION_JSON);
            
            for (Map.Entry<String, String> header : headers.entrySet()) {
                resource = resource.header(header.getKey(), header.getValue());
            }
            
            return resource;
        }
    }
    
    private class SbabMicroClient {
        
        private static final String REQUEST_PATH = "/LAR_WS/rest/v1";
        private static final String INTEREST_RATES_PATH = REQUEST_PATH + "/rantor";
        private static final String DISCOUNT_PATH = REQUEST_PATH + "/rabatter";
        
        private final Client client;
        private final String mortgageBaseUrl;
        
        public SbabMicroClient(SbabIntegrationConfiguration configuration) {

            SbabMortgageIntegrationConfiguration mortgageConfiguration = configuration.getMortgage();

            this.client = new BasicJerseyClientFactory().createCustomClient();
            client.addFilter(new HTTPBasicAuthFilter(mortgageConfiguration.getUsername(), mortgageConfiguration
                    .getPassword()));

            String schema = mortgageConfiguration.isHttps() ? "https://" : "http://";
            this.mortgageBaseUrl = schema + mortgageConfiguration.getTargetHost();
        }

        public double getInterestRate(int marketValue, int mortgageValue, int numberOfApplicants) throws Exception {
            return getInterestRate(marketValue, mortgageValue)
                    - getMortgageDiscounts(numberOfApplicants, mortgageValue);
        }

        private double getInterestRate(int marketValue, int mortgageValue) throws Exception {

            MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
            queryParameters.add("marknadsvarde", String.valueOf(marketValue));
            queryParameters.add("soktBelopp", String.valueOf(mortgageValue));
            
            String rawResponse = createJsonRequest(getMortgageUrl(INTEREST_RATES_PATH), queryParameters).get(
                    String.class);
            JSONObject response = new JSONObject(rawResponse);
            JSONArray interestRates = response.getJSONArray("rantor");

            for (int i = 0; i < interestRates.length(); i++) {
                JSONObject interestRateEntry = interestRates.getJSONObject(i);

                if (Doubles.fuzzyEquals(interestRateEntry.getDouble("bindningstidManader"), 3d, 0.1)) {
                    return interestRateEntry.getDouble("prisdiffRantesats") / 100d;
                }
            }

            throw new Exception("No interest rate available");
        }

        private double getMortgageDiscounts(int numberOfApplicants, int mortgageValue) throws Exception {
            MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl();
            queryParameters.add("antalSokande", String.valueOf(numberOfApplicants));
            queryParameters.add("soktBelopp", String.valueOf(mortgageValue));
            queryParameters.add("syfte", "BYT_BANK");
            String rawResponse = createJsonRequest(getMortgageUrl(DISCOUNT_PATH), queryParameters).get(String.class);
            JSONObject response = new JSONObject(rawResponse);
            return response.getDouble("rabatt") / 100d;
        }
        
        private String getMortgageUrl(String path) {
            return getUrl(mortgageBaseUrl, path);
        }
        
        private String getUrl(String baseUrl, String path) {
            return baseUrl + path;
        }
        
        private Builder createJsonRequest(String url, MultivaluedMap<String, String> queryParameters) {
            return client.resource(url)
                    .queryParams(queryParameters)
                    .header("User-Agent", "Tink (+https://www.tink.se/; noc@tink.se)")
                    .type(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);
        }
    }
    
    private class Profile {
        
        public String userId;
        public Integer age;
        public Mortgage candidate;
        public Integer creditScore;
        public Boolean hasRealEstate;
        public Boolean hasRecordOfNonPayment;
        public Double incomeByService;
        public Boolean isSEBCustomer;
        public Boolean livesInHousingCooperative;
        public List<Mortgage> mortgages = Lists.newArrayList();
        public String postalCode;
        public String municipality;
        public Boolean qualifies;
        public Double sbabInterestRate;
        public Double sebInterestRate;
        
        public Profile(String userId) {
            this.userId = userId;
        }
        
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this.getClass())
                    .add("userId", userId)
                    .add("age", age)
                    .add("creditScore", creditScore)
                    .add("hasRealEstate", hasRealEstate)
                    .add("hasRecordOfNonPayment", hasRecordOfNonPayment)
                    .add("incomeByService", incomeByService)
                    .add("isSEBCustomer", isSEBCustomer)
                    .add("livesInHousingCooperative", livesInHousingCooperative)
                    .add("postalCode", postalCode)
                    .add("municipality", municipality)
                    .add("qualifies", qualifies)
                    .add("candidate", candidate)
                    .add("sbabInterestRate", sbabInterestRate)
                    .add("sebInterestRate", sebInterestRate)
                    .add("mortgages", mortgages)
                    .toString();
        }
    }
    
    private class Mortgage {
        
        public double amount;
        public double interest;
        public int parts;
        public String provider;
        public String terms;
        
        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this.getClass())
                    .add("amount", amount)
                    .add("interest", interest)
                    .add("parts", parts)
                    .add("provider", provider)
                    .add("terms", terms)
                    .toString();
        }
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
            
            if (Objects.equal(profile.hasRealEstate, true)) {
                return false;
            }
            
            if (Objects.equal(profile.hasRecordOfNonPayment, true)) {
                return false;    
            }
            
            if (Objects.equal(profile.isSEBCustomer, true)) {
                return false;    
            }
            
            if (!Objects.equal(profile.livesInHousingCooperative, true)) {
                return false;    
            }
            
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
    
            if (!Objects.equal(mortgage.terms, "floating")) {
                return false;    
            }
            
            if (mortgage.interest < minInterestRate || mortgage.interest > maxInterestRate) {
                return false;    
            }
            
            if (mortgage.amount < minAmount || mortgage.amount > maxAmount) {
                return false;    
            }
            
            return true;
        }
    }
}
