package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.santander;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class SantanderAgentTest {

    private final String SOURCE_IDENTIFIER = "";
    private final String DESTINATION_IDENTIFIER = "";

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-santander-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("santander")
                .setAppId("tink")
                .build()
                .testRefresh();
    }

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-santander-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("santander")
                        .setAppId("tink");

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
                                        "Unknown person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                Type.SORT_CODE, SOURCE_IDENTIFIER)))
                        .withExactCurrencyAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withReference(new Reference("TRANSFER", "test Tink"))
                        .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                        .build());

        return payments;
    }
}
