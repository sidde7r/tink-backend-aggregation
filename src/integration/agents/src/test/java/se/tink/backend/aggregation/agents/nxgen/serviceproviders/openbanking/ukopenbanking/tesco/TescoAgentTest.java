package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.tesco;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class TescoAgentTest {

    private final String SOURCE_IDENTIFIER = "";
    private final String DESTINATION_IDENTIFIER = "";

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-tesco-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("tesco")
                .build()
                .testRefresh();
    }

    @Test
    public void testPayments() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-tesco-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setClusterId("oxford-preprod")
                        // Go to data/secret/preprod/ss/, check the file name of a bank json file,
                        // put the file name into setAppId() below
                        .setAppId("check the comment above")
                        .setFinancialInstitutionId("tesco");

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
                                        "Unknown Person"))
                        .withDebtor(
                                new Debtor(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SORT_CODE,
                                                SOURCE_IDENTIFIER)))
                        .withExactCurrencyAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(currency)
                        .withRemittanceInformation(
                                RemittanceInformationUtils
                                        .generateUnstructuredRemittanceInformation("Message"))
                        .withUniqueId(RandomUtils.generateRandomHexEncoded(15))
                        .build());

        return payments;
    }
}
