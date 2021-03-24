package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays;

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

public class BarclaysAgentTest {

    private final String SOURCE_IDENTIFIER = "";
    private final String DESTINATION_IDENTIFIER = "";
    private static final String BARCLAYS_FINANCIAL_INSTITUTION_ID =
            "0ee1cda5943e4662bb9775c34de291d6";

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-barclays-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId(BARCLAYS_FINANCIAL_INSTITUTION_ID)
                .build()
                .testRefresh();
    }

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-barclays-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setAppId("tink")
                        .setFinancialInstitutionId(BARCLAYS_FINANCIAL_INSTITUTION_ID);

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
