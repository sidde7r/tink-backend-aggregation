package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserter;
import se.tink.backend.aggregation.agents.framework.utils.wiremock.WiremockS3LogRequestResponseParser;
import se.tink.backend.aggregation.agents.framework.wiremock.AgentIntegrationMockServerTest;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.account.enums.AccountExclusion;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class AmericanExpressV62UKMockServerAgentTest extends AgentIntegrationMockServerTest {

    private String USERNAME = "testUser";
    private String PASSWORD = "testPassword";

    @Test
    public void testLogin() throws Exception {

        /** Prepare WireMock server with request/reponse pair for blackbox test */
        prepareMockServer(
                new WiremockS3LogRequestResponseParser(
                        String.format(
                                "%s/%s",
                                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
                                "resources/mock.txt"),
                        "https://global.americanexpress.com"));

        /** Prepare expected Account and Transaction objects */
        Account account = new Account();
        account.setAccountNumber("XXX-11111");
        account.setAccountExclusion(AccountExclusion.NONE);
        account.setExactAvailableCredit(new ExactCurrencyAmount(new BigDecimal(0), "GBP"));
        account.setExactBalance(new ExactCurrencyAmount(new BigDecimal(0), "GBP"));
        account.setCurrencyCode("GBP");
        account.setBankId("XXX11111");
        account.setCertainDate(null);
        account.setCredentialsId(null);
        account.setExcluded(false);
        account.setFavored(false);
        account.setId("de08fcd2507a46d2bf0da21507ea2a87");
        account.setName("American Express Basic Card - 11111");
        account.setOwnership(1.0);
        account.setPayload(null);
        account.setType(AccountTypes.CREDIT_CARD);
        account.setUserId(null);
        account.setUserModifiedExcluded(false);
        account.setUserModifiedName(false);
        account.setUserModifiedType(false);
        account.setIdentifiers(new ArrayList<>());
        account.setTransferDestinations(null);
        account.setDetails(null);
        account.setClosed(false);
        account.setHolderName("MR M LASTNAME");
        account.setFlags(new ArrayList<>());
        account.setFinancialInstitutionId(null);
        account.setAvailableCredit(0.0);
        account.setBalance(0.0);

        List<Account> expectedAccounts = Arrays.asList(new Account[] {account});

        Transaction transaction1 = new Transaction();
        transaction1.setAccountId("fabc330b9f804c7ca6c2a21c3fd255ae");
        transaction1.setAmount(-403.25);
        transaction1.setCredentialsId(null);
        transaction1.setDate(new Date(1552561200000l));
        transaction1.setDescription("interesting transaction");
        transaction1.setId("84110da33d7a4af7b58ff083d1ad6ece");
        transaction1.setOriginalAmount(0.0);
        transaction1.setPending(false);
        transaction1.setTimestamp(0);
        transaction1.setType(TransactionTypes.DEFAULT);
        transaction1.setUserId(null);
        transaction1.setUpcoming(false);

        Transaction transaction2 = new Transaction();
        // Account id is always different and this id is not comäng from the Account object???
        transaction2.setAccountId("fabc330b9f804c7ca6c2a21c3fd255ae");
        transaction2.setAmount(-64.84);
        transaction2.setCredentialsId(null);
        transaction2.setDate(new Date(1553770800000l));
        transaction2.setDescription("good transaction");
        transaction2.setId("e1a52960ae03496ea532f1993376ceb0");
        transaction2.setOriginalAmount(0.0);
        transaction2.setPending(false);
        transaction2.setTimestamp(0);
        transaction2.setType(TransactionTypes.DEFAULT);
        transaction2.setUserId(null);
        transaction2.setUpcoming(false);

        Transaction transaction3 = new Transaction();
        transaction3.setAccountId("fabc330b9f804c7ca6c2a21c3fd255ae");
        transaction3.setAmount(64.84);
        transaction3.setCredentialsId(null);
        transaction3.setDate(new Date(1554285600000l));
        transaction3.setDescription("PAYMENT RECEIVED - THANK YOU");
        transaction3.setId("150e5364820748a9acb42b67ccbc1e80");
        transaction3.setOriginalAmount(0.0);
        transaction3.setPending(false);
        transaction3.setTimestamp(0);
        transaction3.setType(TransactionTypes.DEFAULT);
        transaction3.setUserId(null);
        transaction3.setUpcoming(false);

        Transaction transaction4 = new Transaction();
        transaction4.setAccountId("fabc330b9f804c7ca6c2a21c3fd255ae");
        transaction4.setAmount(551.4);
        transaction4.setCredentialsId(null);
        transaction4.setDate(new Date(1553511600000l));
        transaction4.setDescription("PAYMENT RECEIVED - THANK YOU");
        transaction4.setId("150e5364820748a9acb42b67ccbc1e80");
        transaction4.setOriginalAmount(0.0);
        transaction4.setPending(false);
        transaction4.setTimestamp(0);
        transaction4.setType(TransactionTypes.DEFAULT);
        transaction4.setUserId(null);
        transaction4.setUpcoming(false);

        List<Transaction> expectedTransactions =
                Arrays.asList(
                        new Transaction[] {transaction1, transaction2, transaction3, transaction4});

        /** Execute the agent against WireMock server */
        NewAgentTestContext context =
                new AgentIntegrationTest.Builder("uk", "uk-americanexpress-password")
                        .addCredentialField(Field.Key.USERNAME, USERNAME)
                        .addCredentialField(Field.Key.PASSWORD, PASSWORD)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .useWireMockServerHost("localhost:" + getWireMockPort())
                        .build()
                        .testRefresh();

        List<Transaction> givenTransactions = context.getTransactions();
        List<Account> givenAccounts = context.getUpdatedAccounts();

        /** Make assertions on output objects of the agent */
        AgentContractEntitiesAsserter asserter = new AgentContractEntitiesAsserter();

        asserter.compareAccounts(expectedAccounts, givenAccounts);
        asserter.compareTransactions(expectedTransactions, givenTransactions);
    }
}
