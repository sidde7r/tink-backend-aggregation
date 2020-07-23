package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.hsbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class HSBCAgentTest {

    private final String SOURCE_IDENTIFIER = "";
    private final String DESTINATION_IDENTIFIER = "";
    private static final String HSBC_FINANCIAL_INSTITUTION_ID = "6cd6d369ba8e4d72b1a7d26dabe509a3";

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-hsbc-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId(HSBC_FINANCIAL_INSTITUTION_ID)
                .setAppId("tink")
                .build()
                .testRefresh();
    }

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-hsbc-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId(HSBC_FINANCIAL_INSTITUTION_ID)
                        .setAppId("tink");
        builder.build().testGenericPaymentUKOB(createMockedDomesticPayment());
    }

    private List<Payment> createMockedDomesticPayment() {
        ArrayList<Payment> payments = new ArrayList<>();
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
                                        "anyname"))
                        .withExactCurrencyAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withReference(new Reference("TRANSFER", "Test Tink"))
                        .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                        .build());
        return payments;
    }
}
