package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.ukob.redirect;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class UkRedirectAuthenticationDemoAgentTest {
    private final String SOURCE_SORT_CODE = "123456";
    private final String SOURCE_ACCOUNT_NUMBER = "12345678";
    private final String SOURCE_IDENTIFIER = SOURCE_SORT_CODE + SOURCE_ACCOUNT_NUMBER;

    private final String DESTINATION_SORT_CODE = "234567";
    private final String DESTINATION_ACCOUNT_NUMBER = "23456789";
    private final String DESTINATION_IDENTIFIER =
            DESTINATION_SORT_CODE + DESTINATION_ACCOUNT_NUMBER;

    @Test
    public void testPayment() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-test-open-banking-redirect")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setAppId("dummy")
                        .setFinancialInstitutionId("dummy");

        builder.build().testGenericPaymentUKOB(createMockedDomesticPayment());
    }

    private List<Payment> createMockedDomesticPayment() {

        List<Payment> payments = new ArrayList<>();

        BigDecimal d = new BigDecimal(1);

        ExactCurrencyAmount amount = new ExactCurrencyAmount(d, "GBP");
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";

        payments.add(
                new Payment.Builder()
                        .withCreditor(
                                new Creditor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SORT_CODE,
                                                DESTINATION_IDENTIFIER),
                                        "Unknown Person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SORT_CODE,
                                                SOURCE_IDENTIFIER)))
                        .withExactCurrencyAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withReference(new Reference("TRANSFER", "test Tink"))
                        .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                        .build());

        return payments;
    }
}
