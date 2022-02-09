package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.tsb;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

public class TsbAgentTest {

    private final String SOURCE_IDENTIFIER = "";
    private final String DESTINATION_IDENTIFIER = "";

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-tsb-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("a4667f95a9c4464eb505dd5e3543d41e")
                .build()
                .testRefresh();
    }

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-tsb-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId("a4667f95a9c4464eb505dd5e3543d41e");

        builder.build().testGenericPaymentUKOB(createMockedDomesticPayment());
    }

    private Payment createMockedDomesticPayment() {
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("1.00", "GBP");
        LocalDate executionDate = LocalDate.now();
        String currency = "GBP";

        return new Payment.Builder()
                .withCreditor(
                        new Creditor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.SORT_CODE, DESTINATION_IDENTIFIER),
                                "Unknown Person"))
                .withDebtor(
                        new Debtor(
                                AccountIdentifier.create(
                                        AccountIdentifierType.SORT_CODE, SOURCE_IDENTIFIER)))
                .withExactCurrencyAmount(amount)
                .withExecutionDate(executionDate)
                .withCurrency(currency)
                .withRemittanceInformation(
                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                                "Message"))
                .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                .build();
    }
}
