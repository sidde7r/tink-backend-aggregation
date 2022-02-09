package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class StarlingAgentTest {

    private static final String STARLING_FINANCIAL_INSTITUTION_ID =
            "b615ccc66e4b4ed1876e80ad397acf56";

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-starling-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId(STARLING_FINANCIAL_INSTITUTION_ID)
                .setAppId("tink")
                .build()
                .testRefresh();
    }

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-starling-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(false)
                        .setAppId("tink")
                        .setClusterId("oxford-staging")
                        .setFinancialInstitutionId(STARLING_FINANCIAL_INSTITUTION_ID);

        builder.build().testTinkLinkPayment(createMockedDomesticPayment());
    }

    private Payment createMockedDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);
        remittanceInformation.setValue("Ref1234");
        ExactCurrencyAmount amount = ExactCurrencyAmount.of("0.01", "GBP");
        String currency = "GBP";

        return new Payment.Builder()
                .withCreditor(new Creditor(new SortCodeIdentifier(""), "Unknown Person"))
                .withDebtor(new Debtor(new SortCodeIdentifier("")))
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                .build();
    }
}
