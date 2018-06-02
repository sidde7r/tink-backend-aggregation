package se.tink.backend.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.util.Base64;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.jersey.JerseyClientFactory;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.net.TinkApacheHttpClient4;
import se.tink.backend.common.retry.RetryHelper;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.connector.rpc.PartnerTransactionPayload;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.main.MainServiceContainer;
import se.tink.backend.rpc.AccountListResponse;
import se.tink.backend.rpc.TransactionQueryResponse;
import se.tink.backend.system.SystemServiceContainer;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This class aims to test the Connector in a way as similar to production as possible. The 'real' containers are
 * started and accessed with String/JSON HTTP requests. There should only be a few tests like this which test the main
 * logic. More specific logic should be tested in either integration tests or unit tests.
 */
@Ignore("This class is for now just used as a template for new partners. We might remove this in the future.")
public class TestPartnerConnectorSystemTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static final ConfigurationFactory<ServiceConfiguration> CONFIGURATION_FACTORY = new ConfigurationFactory<>(
            ServiceConfiguration.class, VALIDATOR, MAPPER, "");
    private static final ServiceConfiguration serviceConfiguration;

    private static final String CONNECTOR_CONFIG_PATH = "etc/testpartner/development-connector-server.yml";
    private static final String SYSTEM_CONFIG_PATH = "etc/testpartner/development-system-server.yml";
    private static final String MAIN_CONFIG_PATH = "etc/testpartner/development-main-server.yml";

    private static final String CONNECTOR_URL;
    private static final String MAIN_URL;

    private static final LogUtils log = new LogUtils(TestPartnerConnectorSystemTest.class);

    private static TinkApacheHttpClient4 client;
    private static RetryHelper retryHelper = new RetryHelper(20, 500);

    static {
        File connectorConfigFile = new File(CONNECTOR_CONFIG_PATH);
        try {
            serviceConfiguration = CONFIGURATION_FACTORY.build(connectorConfigFile);
            CONNECTOR_URL = serviceConfiguration.getEndpoints().getConnector().getUrl();
            MAIN_URL = serviceConfiguration.getEndpoints().getMain().getUrl();
        } catch (IOException | ConfigurationException e) {
            log.error("Could not initialize system test");

            // Print the stack trace, since it won't show in the output otherwise.
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        ConnectorServiceContainer.main(new String[] { "server", CONNECTOR_CONFIG_PATH });
        SystemServiceContainer.main(new String[] { "server", SYSTEM_CONFIG_PATH });
        MainServiceContainer.main(new String[] { "server", MAIN_CONFIG_PATH });

        JerseyClientFactory clientFactory = new JerseyClientFactory();
        client = clientFactory.createCustomClient(ByteStreams.nullOutputStream());
    }

    @Test
    public void pingWorks() {
        assertEquals("pong", client.resource(CONNECTOR_URL + "connector/monitoring/ping").get(String.class));
    }

    @Test
    public void createUser_ingestAccounts_ingestTransactions_verifyAllSuccessfullyCreated() throws Exception {
        String externalUserId = RandomStringUtils.randomAlphanumeric(10);
        String token = RandomStringUtils.randomAlphanumeric(10);
        String externalAccountId1 = RandomStringUtils.randomAlphanumeric(10);
        String externalAccountId2 = RandomStringUtils.randomAlphanumeric(10);
        String externalTransactionId1 = RandomStringUtils.randomAlphanumeric(10);
        String externalTransactionId2 = RandomStringUtils.randomAlphanumeric(10);
        String externalTransactionId3 = RandomStringUtils.randomAlphanumeric(10);
        long yesterdayTimestamp = OffsetDateTime.now().minusDays(1).toEpochSecond();
        long nowTimestamp = OffsetDateTime.now().toEpochSecond();

        // Create the user.
        ClientResponse userResponse = getConnectorResource("/users")
                .post(ClientResponse.class, ("{"
                        + "'externalId': '" + externalUserId + "',"
                        + "'token': '" + token + "'"
                        + "}").replace("'", "\""));

        assertTrue(HttpStatusCodes.isSuccess(userResponse.getStatus()));

        // Ingest accounts.
        ClientResponse accountResponse = getConnectorResource("/users/" + externalUserId + "/accounts")
                .post(ClientResponse.class, ("{"
                        + "'accounts': ["
                        + "  {"
                        + "    'balance': 1500,"
                        + "    'name': 'Test account 1',"
                        + "    'type': 'SAVINGS',"
                        + "    'externalId': '" + externalAccountId1 + "',"
                        + "    'number': 'accountNr1'"
                        + "  },"
                        + "  {"
                        + "    'balance': 500,"
                        + "    'name': 'Test account 2',"
                        + "    'type': 'CHECKING',"
                        + "    'externalId': '" + externalAccountId2 + "',"
                        + "    'number': 'accountNr2',"
                        + "    'disposableAmount': 100"
                        + "  }"
                        + "]}").replace("'", "\""));

        // Fetch accounts from main.
        assertTrue(HttpStatusCodes.isSuccess(accountResponse.getStatus()));
        retryHelper.retryUntil(() -> fetchAccounts(externalUserId, token).getAccounts().size() == 2);

        // Assert that all accounts fetched from main are correct.
        List<Account> accounts = fetchAccounts(externalUserId, token).getAccounts();
        assertEquals(2, accounts.size());

        Account account1 = findAccount(accounts, externalAccountId1);
        verifyAccount(account1, 1500, "Test account 1", AccountTypes.SAVINGS, "accountNr1");

        Account account2 = findAccount(accounts, externalAccountId2);
        verifyAccount(account2, 500, "Test account 2", AccountTypes.CHECKING, "accountNr2");

        // Ingest transactions.
        ClientResponse transactionResponse = getConnectorResource("/users/" + externalUserId + "/transactions")
                .post(ClientResponse.class, ("{"
                        + "'type': 'BATCH',"
                        + "'transactionAccounts': ["
                        + "  {"
                        + "    'payload': {"
                        + "      'IGNORE_BALANCE': true"
                        + "    },"
                        + "    'externalId': '" + externalAccountId1 + "',"
                        + "    'balance': null,"
                        + "    'disposableAmount': null,"
                        + "    'transactions': ["
                        + "      {"
                        + "        'amount': -110.00,"
                        + "        'date': " + yesterdayTimestamp + ","
                        + "        'description': 'Coop',"
                        + "        'externalId': '" + externalTransactionId1 + "',"
                        + "        'type': 'CREDIT_CARD',"
                        + "        'pending': false"
                        + "      },"
                        + "      {"
                        + "        'amount': -510.00,"
                        + "        'date': " + nowTimestamp + ","
                        + "        'description': 'Ica',"
                        + "        'externalId': '" + externalTransactionId2 + "',"
                        + "        'type': 'CREDIT_CARD',"
                        + "        'pending': true"
                        + "      }"
                        + "    ]"
                        + "  },"
                        + "  {"
                        + "    'externalId': '" + externalAccountId2 + "',"
                        + "    'balance': 1500,"
                        + "    'disposableAmount': 1200,"
                        + "    'transactions': ["
                        + "      {"
                        + "        'amount': -90.35,"
                        + "        'date': " + nowTimestamp + ","
                        + "        'description': 'Ikea',"
                        + "        'externalId': '" + externalTransactionId3 + "',"
                        + "        'type': 'WITHDRAWAL',"
                        + "        'pending': false"
                        + "      }"
                        + "    ]"
                        + "  }"
                        + "]}").replace("'", "\""));

        // Fetch transactions from main.
        assertTrue(HttpStatusCodes.isSuccess(transactionResponse.getStatus()));
        retryHelper.retryUntil(() -> fetchTransactions(externalUserId, token).getCount() == 3);

        // Assert that all transactions fetched from main are correct.
        List<Transaction> transactions = fetchTransactions(externalUserId, token).getTransactions();
        assertEquals(3, transactions.size());

        Transaction transaction1 = findTransaction(transactions, externalTransactionId1);
        verifyTransaction(transaction1, account1.getId(), -110, new Date(yesterdayTimestamp), "Coop",
                TransactionTypes.CREDIT_CARD, false);

        Transaction transaction2 = findTransaction(transactions, externalTransactionId2);
        verifyTransaction(transaction2, account1.getId(), -510, new Date(nowTimestamp), "Ica",
                TransactionTypes.CREDIT_CARD, true);

        Transaction transaction3 = findTransaction(transactions, externalTransactionId3);
        verifyTransaction(transaction3, account2.getId(), -90.35, new Date(nowTimestamp), "Ikea",
                TransactionTypes.WITHDRAWAL, false);
    }

    @Test
    public void createUser_ingestAccounts_ingestPending_modifyCategory_ingestNonPending_confirmCorrectTransaction() {
        String externalUserId = RandomStringUtils.randomAlphanumeric(10);
        String token = RandomStringUtils.randomAlphanumeric(10);
        String externalAccountId1 = RandomStringUtils.randomAlphanumeric(10);
        String externalTransactionId1 = RandomStringUtils.randomAlphanumeric(10);
        String externalTransactionId2 = RandomStringUtils.randomAlphanumeric(10);
        long yesterdayTimestamp = OffsetDateTime.now().minusDays(1).toEpochSecond();
        long nowTimestamp = OffsetDateTime.now().toEpochSecond();

        // Create the user.
        ClientResponse userResponse = getConnectorResource("/users")
                .post(ClientResponse.class, ("{"
                        + "'externalId': '" + externalUserId + "',"
                        + "'token': '" + token + "'"
                        + "}").replace("'", "\""));

        assertTrue(HttpStatusCodes.isSuccess(userResponse.getStatus()));

        // Ingest accounts.
        ClientResponse accountResponse = getConnectorResource("/users/" + externalUserId + "/accounts")
                .post(ClientResponse.class, ("{"
                        + "'accounts': ["
                        + "  {"
                        + "    'balance': 3000,"
                        + "    'name': 'Test account 1',"
                        + "    'type': 'CHECKING',"
                        + "    'externalId': '" + externalAccountId1 + "',"
                        + "    'number': 'accountNr1'"
                        + "  }"
                        + "]}").replace("'", "\""));

        // Fetch accounts from main.
        assertTrue(HttpStatusCodes.isSuccess(accountResponse.getStatus()));
        retryHelper.retryUntil(() -> fetchAccounts(externalUserId, token).getAccounts().size() == 1);

        // Assert that all accounts fetched from main are correct.
        List<Account> accounts = fetchAccounts(externalUserId, token).getAccounts();
        assertEquals(1, accounts.size());

        Account account1 = findAccount(accounts, externalAccountId1);
        verifyAccount(account1, 3000, "Test account 1", AccountTypes.CHECKING, "accountNr1");

        // Send a pending transaction.
        ClientResponse transactionResponse = getConnectorResource("/users/" + externalUserId + "/transactions")
                .post(ClientResponse.class, ("{"
                        + "'type': 'BATCH',"
                        + "'transactionAccounts': ["
                        + "  {"
                        + "    'payload': {"
                        + "      'IGNORE_BALANCE': false"
                        + "    },"
                        + "    'externalId': '" + externalAccountId1 + "',"
                        + "    'balance': 3000,"
                        + "    'disposableAmount': null,"
                        + "    'transactions': ["
                        + "      {"
                        + "        'amount': -301.50,"
                        + "        'date': " + yesterdayTimestamp + ","
                        + "        'description': 'H&m',"
                        + "        'externalId': '" + externalTransactionId1 + "',"
                        + "        'type': 'CREDIT_CARD',"
                        + "        'pending': true"
                        + "      }"
                        + "    ]"
                        + "  }"
                        + "]}").replace("'", "\""));

        // Fetch transactions from main.
        assertTrue(HttpStatusCodes.isSuccess(transactionResponse.getStatus()));
        retryHelper.retryUntil(() -> fetchTransactions(externalUserId, token).getCount() == 1);

        // Assert that all transactions fetched from main are correct.
        List<Transaction> transactions = fetchTransactions(externalUserId, token).getTransactions();
        assertEquals(1, transactions.size());

        Transaction transaction1 = findTransaction(transactions, externalTransactionId1);
        verifyTransaction(transaction1, account1.getId(), -301.50, new Date(yesterdayTimestamp), "H&m",
                TransactionTypes.CREDIT_CARD, true);

        // Set a user modified category on the reserved transaction and make sure it's set to a new category. The
        // categories below are arbitrary, and what's important is that the category is changed to a leaf node category.
        String hobbyId = getCategoryId(SECategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY, externalUserId, token);
        String cultureId = getCategoryId(SECategories.Codes.EXPENSES_ENTERTAINMENT_CULTURE, externalUserId, token);
        String userModifiedCategoryId = (Objects.equals(transaction1.getCategoryId(), hobbyId) ? cultureId : hobbyId);

        ClientResponse categoryResponse = getMainResource("/transactions/categorize-multiple", externalUserId, token)
                .put(ClientResponse.class, ("{"
                        + "'categorizationList': ["
                        + "  {"
                        + "    'categoryId': '" + userModifiedCategoryId + "',"
                        + "    'transactionIds': ['" + transaction1.getId() + "']"
                        + "  }"
                        + "]}").replace("'", "\""));

        assertTrue(HttpStatusCodes.isSuccess(categoryResponse.getStatus()));
        retryHelper.retryUntil(() -> {
            List<Transaction> fetchedTransactions = fetchTransactions(externalUserId, token).getTransactions();
            Transaction transaction = findTransaction(fetchedTransactions, externalTransactionId1);

            return Objects.equals(userModifiedCategoryId, transaction.getCategoryId());
        });

        // Send a non-pending transaction corresponding to the earlier pending.
        transactionResponse = getConnectorResource("/users/" + externalUserId + "/transactions")
                .post(ClientResponse.class, ("{"
                        + "'type': 'BATCH',"
                        + "'transactionAccounts': ["
                        + "  {"
                        + "    'externalId': '" + externalAccountId1 + "',"
                        + "    'balance': 4000,"
                        + "    'disposableAmount': null,"
                        + "    'transactions': ["
                        + "      {"
                        + "        'payload': {"
                        + "          'PENDING_IDS': ['" + externalTransactionId1 + "']"
                        + "        },"
                        + "        'amount': -307.49,"
                        + "        'date': " + nowTimestamp + ","
                        + "        'description': 'H&m new description',"
                        + "        'externalId': '" + externalTransactionId2 + "',"
                        + "        'type': 'CREDIT_CARD',"
                        + "        'pending': false"
                        + "      }"
                        + "    ]"
                        + "  }"
                        + "]}").replace("'", "\""));

        // Fetch transactions from main.
        assertTrue(HttpStatusCodes.isSuccess(transactionResponse.getStatus()));
        retryHelper.retryUntil(() -> {
            List<Transaction> fetchedTransactions = fetchTransactions(externalUserId, token).getTransactions();
            return fetchedTransactions != null && fetchedTransactions.size() == 1
                    && findTransaction(fetchedTransactions, externalTransactionId2) != null;
        });

        // Assert that all transactions fetched from main are correct.
        transactions = fetchTransactions(externalUserId, token).getTransactions();
        assertEquals(1, transactions.size());

        Transaction transaction2 = findTransaction(transactions, externalTransactionId2);
        verifyTransaction(transaction2, account1.getId(), -307.49, new Date(nowTimestamp), "H&m new description",
                TransactionTypes.CREDIT_CARD, false);
        assertEquals(userModifiedCategoryId, transaction2.getCategoryId());
    }

    @SuppressWarnings("unchecked")
    private String getCategoryId(String categoryCode, String externalUserId, String token) {
        List<LinkedHashMap<String, String>> allCategories = (List<LinkedHashMap<String, String>>) getMainResource(
                "/categories", externalUserId, token).get(List.class);

        for (LinkedHashMap<String, String> category : allCategories) {
            if (Objects.equals(category.get("code"), categoryCode)) {
                return category.get("id");
            }
        }
        throw new IllegalStateException("Could not find wanted category");
    }

    private Account findAccount(List<Account> accounts, String externalAccountId) {
        return accounts.stream().filter(a -> Objects.equals(a.getBankId(), externalAccountId))
                .findFirst().orElse(null);
    }

    private Transaction findTransaction(List<Transaction> transactions, String externalTransactionId) {
        return transactions.stream().filter(t -> Objects.equals(t.getPayload().get(
                TransactionPayloadTypes.EXTERNAL_ID), externalTransactionId))
                .findFirst().orElse(null);
    }

    private void verifyAccount(Account account, double balance, String name, AccountTypes type, String accountNumber) {
        assertNotNull(account);
        assertEquals(balance, account.getBalance(), 0);
        assertEquals(name, account.getName());
        assertEquals(type, account.getType());
        assertEquals(accountNumber, account.getAccountNumber());
    }

    private void verifyTransaction(Transaction transaction, String externalAccountId, double amount, Date date,
            String description, TransactionTypes type, boolean pending) {

        assertNotNull(transaction);
        assertEquals(externalAccountId, transaction.getAccountId());
        assertEquals(amount, transaction.getAmount(), 0);
        assertEquals(date, transaction.getDate());
        assertEquals(description, transaction.getDescription());
        assertEquals(type, transaction.getType());
        assertEquals(pending, transaction.isPending());
    }

    private PartnerTransactionPayload getPayload(Transaction transaction) {
        return SerializationUtils.deserializeFromString(
                transaction.getInternalPayload().get(Transaction.InternalPayloadKeys.PARTNER_PAYLOAD),
                PartnerTransactionPayload.class);
    }

    private AccountListResponse fetchAccounts(String userExternalId, String token) {
        return getMainResource("/accounts/list", userExternalId, token)
                .get(AccountListResponse.class);
    }

    private TransactionQueryResponse fetchTransactions(String userExternalId, String token) {
        return getMainResource("/transactions/query", userExternalId, token)
                .post(TransactionQueryResponse.class, "{\"limit\": 10}");
    }

    private WebResource.Builder getConnectorResource(String path) {
        return client.resource(CONNECTOR_URL + "connector" + path)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "token 0");
    }

    private WebResource.Builder getMainResource(String path, String externalId, String token) {
        String basic = externalId + ":" + token;
        return client.resource(MAIN_URL + "api/v1" + path)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Basic " + Base64.encodeBase64String(basic.getBytes(Charsets.ISO_8859_1)));
    }
}
