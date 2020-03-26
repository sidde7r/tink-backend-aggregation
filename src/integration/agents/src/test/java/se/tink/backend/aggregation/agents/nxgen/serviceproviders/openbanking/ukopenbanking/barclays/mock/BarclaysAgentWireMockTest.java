package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.mock;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.NewAgentTestContext;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesAsserts;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class BarclaysAgentWireMockTest {

    private final String SOURCE_IDENTIFIER = "2314701111111";
    private final String DESTINATION_IDENTIFIER = "04000469430924";

    @Test
    public void test() throws Exception {

        // Given
        WireMockTestServer server = new WireMockTestServer();

        server.prepareMockServer(
                new AapFileParser(
                        new ResourceFileReader()
                                .read(
                                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/barclays/mock/resources/barclays_mock_log.aap")));

        final WireMockConfiguration configuration =
                WireMockConfiguration.builder("localhost:" + server.getHttpsPort())
                        .setConfigurationPath(
                                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/barclays/mock/resources/configuration.yml")
                        .setCallbackData(
                                ImmutableMap.<String, String>builder()
                                        .put("code", "DUMMY_AUTH_CODE")
                                        .build())
                        .build();

        AgentContractEntitiesJsonFileParser contractParser =
                new AgentContractEntitiesJsonFileParser();
        AgentContractEntity expected =
                contractParser.parseContractOnBasisOfFile(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/barclays/mock/resources/agent-contract.json");

        List<Account> expectedAccounts = expected.getAccounts();
        List<Transaction> expectedTransactions = expected.getTransactions();

        // When
        NewAgentTestContext context =
                new AgentIntegrationTest.Builder("uk", "uk-barclays-oauth2")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId("barclays")
                        .setWireMockConfiguration(configuration)
                        .build()
                        .testRefresh();

        List<Transaction> givenTransactions = context.getTransactions();
        List<Account> givenAccounts = context.getUpdatedAccounts();

        // Then
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedAccounts, givenAccounts));
        Assert.assertTrue(
                AgentContractEntitiesAsserts.areListsMatchingVerbose(
                        expectedTransactions, givenTransactions));
    }

    @Test
    public void testPayment() throws Exception {

        // Given
        WireMockTestServer server = new WireMockTestServer();
        server.prepareMockServer(
                new AapFileParser(
                        new ResourceFileReader()
                                .read(
                                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/barclays/mock/resources/barclays_payment_mock_log.aap")));

        final WireMockConfiguration configuration =
                WireMockConfiguration.builder("localhost:" + server.getHttpsPort())
                        .setConfigurationPath(
                                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/barclays/mock/resources/configuration.yml")
                        .setCallbackData(
                                ImmutableMap.<String, String>builder()
                                        .put("code", "DUMMY_AUTH_CODE")
                                        .build())
                        .build();

        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-barclays-oauth2")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId("barclays")
                        .setWireMockConfiguration(configuration);

        builder.build().testGenericPaymentUKOB(createMockedDomesticPayment());
    }

    private List<Payment> createMockedDomesticPayment() {

        List<Payment> payments = new ArrayList<>();

        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "GBP");
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";

        payments.add(
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SORT_CODE,
                                                DESTINATION_IDENTIFIER),
                                        "Ritesh Tink"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                Type.SORT_CODE, SOURCE_IDENTIFIER)))
                        .withExactCurrencyAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withReference(new Reference("TRANSFER", "UK Demo"))
                        .withUniqueId("b900555d03124056b54930e1c53c9cac")
                        .build());

        return payments;
    }
}
