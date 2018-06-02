package se.tink.backend.abnamro.connector;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.libraries.net.BasicJerseyClientFactory;
import se.tink.backend.connector.api.AbnAmroConnectorService;
import se.tink.backend.connector.rpc.abnamro.TransactionAccountContainer;
import se.tink.backend.connector.rpc.abnamro.TransactionAccountEntity;
import se.tink.backend.connector.rpc.abnamro.TransactionEntity;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;

/**
 * TODO this is a unit test
 */
@Ignore
public class AbnAmroConnectorTest {
    private static final LogUtils log = new LogUtils(AbnAmroConnectorTest.class);
    private final static Random RANDOM = new Random();
    private static final Date TODAY = new Date();

    private AbnAmroConnectorService connector;

    private static final String DEFAULT_USER_AGENT = "Tink (+https://www.tink.se/; noc@tink.se)";
    private Client client;

    // Page configuration (for history tests)
    private static final int PAGES = 5;
    private static final int PAGE_SIZE = 50;
    private static final boolean RANDOMIZE_PAGE_COUNT = false;

    // Account configuration
    private static final int ACCOUNTS = 10;
    private static final long ACCOUNT_NUMBER_START = 500000001;
    private static final long ACCOUNT_NUMBER_END = ACCOUNT_NUMBER_START + ACCOUNTS - 1;
    private static final int LOAD_TEST_SINGLE_TRANSACTIONS_COUNT = 10;

    // Thread pool configuration
    private static final int LOAD_TEST_THREAD_POOL_SIZE = 10;
    private static final long LOAD_TEST_TERMINATION_TIMEOUT = 5;
    private static final TimeUnit LOAD_TEST_TERMINATION_TIMEOUT_TIMEUNIT = TimeUnit.MINUTES;

    private static final AtomicInteger LOAD_TEST_REMAINING_SINGLE_TRANSACTIONS_COUNT = new AtomicInteger();

    // Production
    // private static final String LOAD_TEST_BASE_PATH = "https://ingest.abnamro.tinkapp.nl/connector/abnamro";
    // private static final String LOAD_TEST_AUTH_TOKEN = "secret-token";

    // Staging
    // private static final String LOAD_TEST_BASE_PATH = "https://ingest.staging.abnamro.tinkapp.nl/connector/abnamro";
    // private static final String LOAD_TEST_AUTH_TOKEN = "secret-token";

    // Localhost
    //    private static final String LOAD_TEST_BASE_PATH = "http://localhost:9090/connector/abnamro";
    private static final String LOAD_TEST_BASE_PATH = "http://localhost:9096/connector/abnamro";
    private static final String LOAD_TEST_AUTH_TOKEN = "00000000000000000000000000000000";

    public int getPages() {
        return RANDOMIZE_PAGE_COUNT ? (int) (Math.random() * PAGES) : PAGES;
    }

    @Test
    public void testHistoricalData() throws Exception {
        log.debug("testHistoricalData");

        for (long accountNumber = ACCOUNT_NUMBER_START; accountNumber <= ACCOUNT_NUMBER_END; accountNumber++) {
            int pages = getPages();

            for (int page = 0; page < pages; page++) {
                log.debug(String.format("Account %s, page %s", accountNumber, page));
                boolean isLast = (page == (pages - 1));
                TransactionAccountContainer container = createContainer(accountNumber, page, isLast);
                connector.transactions(container);
            }
        }
    }

    @Test
    public void testHistoricalDataLoad() throws Exception {
        log.debug("testHistoricalDataLoad");
        
        ExecutorService executor = Executors.newFixedThreadPool(LOAD_TEST_THREAD_POOL_SIZE);

        for (long accountNumber = ACCOUNT_NUMBER_START; accountNumber <= ACCOUNT_NUMBER_END; accountNumber++) {
            executor.execute(getPageSender(accountNumber));
        }
        
        executor.shutdown();

        if (!executor.awaitTermination(LOAD_TEST_TERMINATION_TIMEOUT, LOAD_TEST_TERMINATION_TIMEOUT_TIMEUNIT)) {
            executor.shutdownNow();
        }
    }
    
    @Test
    public void testRealTimeTransactionsLoad() throws Exception {
        log.debug("testRealTimeTransactionsLoad");
        
        final long sessionId = RANDOM.nextInt(1000000);
        log.debug(String.format("sessionId=%s", sessionId));
        
        ExecutorService executor = Executors.newFixedThreadPool(LOAD_TEST_THREAD_POOL_SIZE);
        
        LOAD_TEST_REMAINING_SINGLE_TRANSACTIONS_COUNT.set(LOAD_TEST_SINGLE_TRANSACTIONS_COUNT);
        
        for (int remaining = LOAD_TEST_SINGLE_TRANSACTIONS_COUNT; remaining > 0; remaining--) {
            long accountNumber = ACCOUNT_NUMBER_START + RANDOM.nextInt(ACCOUNTS);
            executor.execute(getSingleTransactionSender(sessionId, accountNumber));
        }
        
        executor.shutdown();

        if (!executor.awaitTermination(LOAD_TEST_TERMINATION_TIMEOUT, LOAD_TEST_TERMINATION_TIMEOUT_TIMEUNIT)) {
            executor.shutdownNow();
        }
    }
    
    private Runnable getPageSender(final long accountNumber) {
        return () -> {

            int pages = getPages();

            Stopwatch timerAllPages = Stopwatch.createStarted();

            for (int page = 0; page < pages; page++) {
                Stopwatch timerTotal = Stopwatch.createStarted();
                log.debug(String.format("Account %s, page %s", accountNumber, page));

                boolean isLast = (page == (pages - 1));
                TransactionAccountContainer container = createContainer(accountNumber, page, isLast);

                Stopwatch timerSend = Stopwatch.createStarted();
                try {
                    createClientRequest("/transactions").header(HttpHeaders.AUTHORIZATION,
                            "token " + LOAD_TEST_AUTH_TOKEN).post(container);
                } catch (Exception e) {
                    log.error(String.format("Unable to send page %s for account %s", page, accountNumber), e);
                }
                timerSend.stop();
                timerTotal.stop();

                long elapsedTimeSend = timerSend.elapsed(TimeUnit.MILLISECONDS);
                long elapsedTimeTotal = timerTotal.elapsed(TimeUnit.MILLISECONDS);
                log.debug(String.format("Executing [account=%s, page=%s] took %sms (%sms).", accountNumber, page,
                        elapsedTimeTotal, elapsedTimeSend));
            }

            timerAllPages.stop();

            log.debug(String.format("Total time: [account=%s, pages=%d] took %s.", accountNumber, pages,
                    DateUtils.prettyFormatMillis((int) timerAllPages.elapsed(TimeUnit.MILLISECONDS))));

        };
    }
    
    private Runnable getSingleTransactionSender(final long sessionId, final long accountNumber) {
        return () -> {
            Stopwatch timerTotal = Stopwatch.createStarted();
            TransactionAccountContainer container = createContainer(sessionId, accountNumber);

            Stopwatch timerSend = Stopwatch.createStarted();
            try {
                createClientRequest("/transactions").header(HttpHeaders.AUTHORIZATION,
                        "token " + LOAD_TEST_AUTH_TOKEN).post(container);
            } catch (Exception e) {
                log.error(String.format("Unable to send single transaction for account %s", accountNumber), e);
            }
            timerSend.stop();

            timerTotal.stop();

            TransactionEntity transaction = container.getAccount().getTransactions().get(0);

            long elapsedTimeSend = timerSend.elapsed(TimeUnit.MILLISECONDS);
            long elapsedTimeTotal = timerTotal.elapsed(TimeUnit.MILLISECONDS);
            log.debug(String.format("[account: %s, remaining: %s] took %sms (%sms). %s", accountNumber,
                    LOAD_TEST_REMAINING_SINGLE_TRANSACTIONS_COUNT.decrementAndGet(), elapsedTimeTotal,
                    elapsedTimeSend, transaction.toString()));
        };
    }
    
    protected Builder createClientRequest(String path) {
        return client
                .resource(LOAD_TEST_BASE_PATH + path)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("User-Agent", DEFAULT_USER_AGENT);
    }
    
    
    private TransactionEntity createTransaction(double amount, Date date, List<String> description) {
        TransactionEntity transaction = new TransactionEntity();
        
        transaction.setAmount(amount);
        transaction.setCpAccount(null);
        transaction.setCpName(null);
        transaction.setDate(date);
        transaction.setDescription(description);
        transaction.setPayload(null);
        transaction.setType("0");
        
        return transaction;
    }
    
    private TransactionAccountContainer createContainer(long accountNumber, int page, boolean isLast) {
        
        List<TransactionEntity> transactions = Lists.newArrayList();
        
        for (int i = 0; i < PAGE_SIZE; i++) {
            double amount = - (page * PAGE_SIZE + i + 1);
            Date date = DateUtils.addDays(TODAY, - (page * PAGE_SIZE + i));
            List<String> description = Lists.newArrayList(
                    String.format("Page%dTrans%d", page, i),
                    RandomStringUtils.randomAlphanumeric(40),
                    RandomStringUtils.randomAlphanumeric(40),
                    RandomStringUtils.randomAlphanumeric(40));
            transactions.add(createTransaction(amount, date, description));
        }
        
        TransactionAccountContainer container = createContainer(accountNumber, transactions);
        container.getAccount().setStatus(isLast ? 1 : 2);
        
        return container;
    }
    
    private TransactionAccountContainer createContainer(long sessionId, long accountNumber) {

        double amount = - Math.round((RANDOM.nextDouble() * 99 + 1) * 100) / 100;
        Date date = DateUtils.addDays(TODAY, -1);
        
        List<String> description = Lists.newArrayList(
                "Load test transaction",
                String.format("session=%s", sessionId),
                RandomStringUtils.randomAlphanumeric(40),
                RandomStringUtils.randomAlphanumeric(40),
                RandomStringUtils.randomAlphanumeric(40));
        
        return createContainer(accountNumber, Lists.newArrayList(createTransaction(amount, date, description)));
    }
    
    
    private TransactionAccountContainer createContainer(long accountNumber, List<TransactionEntity> transactions) {
        TransactionAccountEntity account = new TransactionAccountEntity();
        account.setAccountNumber(accountNumber);
        account.setBalance(1000);
        account.setStatus(0);
        account.setTransactions(transactions);

        // The bc number and account number are in reality different but we use the same since it is easier to setup
        account.setBcNumbers(ImmutableList.of(accountNumber));
        
        TransactionAccountContainer container = new TransactionAccountContainer();
        container.setAccount(account);
        return container;
    }
    
    @Before
    public void setup() throws Exception {
        client = new BasicJerseyClientFactory().createBasicClient();
        connector = new BasicWebServiceClassBuilder(InterContainerJerseyClientFactory.withoutPinning().build()
                .resource("http://localhost:9090")).build(AbnAmroConnectorService.class);
    }
    
    @After
    public void cleanup() throws Exception {
        log.debug("cleanup");
    }
}
