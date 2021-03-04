package se.tink.backend.aggregation.agents.nxgen.demo.banks;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Beneficiary;
import se.tink.libraries.payment.rpc.CreateBeneficiary;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

@Ignore("Manual test -- should be put in a separate Bazel package")
public class RedirectAuthenticationDemoAgentTest {
    private final String SOURCE_IDENTIFIER = "1234567891234";
    private final String DESTINATION_IDENTIFIER = "1434567891234";
    private final String SOURCE_ACCOUNT_NAME = "ha";

    private final String ITALY_SOURCE_ACCOUNT =
            "IT52X0300203280728575573739"; // from ais flow, checking account
    private final String ITALY_DESTINATION_ACCOUNT =
            "IT53X0300203280882749129712"; // from ais flow, savings account
    private final String FRANCE_SOURCE_ACCOUNT =
            "FR9496449644000017699358020"; // from ais flow, checking account
    private final String FRANCE_DESTINATION_ACCOUNT =
            "FR9179997999000017699358020"; // from ais flow, savings account
    private static final String EXPIRED_TIME_IN_SECOND = "60";

    @Test
    public void testTransfer() throws Exception {

        Transfer transfer = new Transfer();
        transfer.setType(TransferType.BANK_TRANSFER);
        AccountIdentifier sourceAccount =
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, SOURCE_IDENTIFIER);
        sourceAccount.setName(SOURCE_ACCOUNT_NAME);
        transfer.setSource(sourceAccount);
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SORT_CODE, DESTINATION_IDENTIFIER));

        transfer.setAmount(ExactCurrencyAmount.of(10.50, "GBP"));
        transfer.setSourceMessage("TRANSFER, test Tink!");

        new AgentIntegrationTest.Builder("uk", "uk-test-open-banking-redirect")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("dummy")
                .setAppId("dummy")
                .build()
                .testBankTransferUK(transfer);
    }

    @Test
    public void testAISSE() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder(
                                "se", "se-test-open-banking-redirect-configurable-session-expiry")
                        .addCredentialField(Field.Key.SESSION_EXPIRY_TIME, EXPIRED_TIME_IN_SECOND)
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true);

        builder.build().testRefresh();
    }

    @Test
    public void testAISIT() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("it", "it-test-open-banking-redirect")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }

    @Test
    public void testAisUk() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("uk", "uk-test-open-banking-redirect")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }

    @Test
    public void testAISFR() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("fr", "fr-test-open-banking-redirect")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("f58e31ebaf625c15a9601aa4deac83d0")
                        .setAppId("tink")
                        .setClusterId("local-development")
                        .setRedirectUrl("https://127.0.0.1:7357/api/v1/thirdparty/callback");

        builder.build().testRefresh();
    }

    @Test
    public void testPaymentIT() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("it", "it-test-open-banking-redirect")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build()
                .testGenericPaymentForRedirect(
                        createListMockedDomesticPayment(
                                1, ITALY_DESTINATION_ACCOUNT, ITALY_SOURCE_ACCOUNT));
        // todo: Remove once we have the assumptions tested
        // builder.build().testGenericPaymentItalia(DESTINATION_ACCOUNT);
    }

    @Test
    public void testCreateBeneficiaryIT() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("it", "it-test-open-banking-redirect")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
        Beneficiary beneficiary =
                Beneficiary.builder()
                        .accountNumber(ITALY_DESTINATION_ACCOUNT)
                        .accountNumberType(Type.IBAN)
                        .name("Test")
                        .build();
        CreateBeneficiary createBeneficiary =
                CreateBeneficiary.builder()
                        .beneficiary(beneficiary)
                        .ownerAccountNumber(ITALY_SOURCE_ACCOUNT)
                        .build();
        builder.build().testCreateBeneficiary(createBeneficiary);
    }

    @Test
    public void testCreateBeneficiaryFrOtp() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder(
                                "fr", "fr-test-open-banking-redirect-beneficiary-otp")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
        Beneficiary beneficiary =
                Beneficiary.builder()
                        .accountNumber(FRANCE_DESTINATION_ACCOUNT)
                        .accountNumberType(Type.IBAN)
                        .name("Test")
                        .build();
        CreateBeneficiary createBeneficiary =
                CreateBeneficiary.builder()
                        .beneficiary(beneficiary)
                        .ownerAccountNumber(FRANCE_SOURCE_ACCOUNT)
                        .build();
        builder.build().testCreateBeneficiary(createBeneficiary);
    }

    @Test
    public void testPaymentFR() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("fr", "fr-test-open-banking-redirect")
                        .expectLoggedIn(false)
                        .setFinancialInstitutionId("dummy")
                        .setAppId("dummy")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build()
                .testGenericPaymentForRedirect(
                        createListMockedDomesticPayment(
                                1, FRANCE_DESTINATION_ACCOUNT, FRANCE_SOURCE_ACCOUNT));
    }

    private List<Payment> createListMockedDomesticPayment(
            int numberOfMockedPayments, String DESTINATION_ACCOUNT, String SOURCE_ACCOUNT) {
        List<Payment> listOfMockedPayments = new ArrayList<>();
        for (int i = 0; i < numberOfMockedPayments; ++i) {
            Creditor creditor =
                    new Creditor(
                            AccountIdentifier.create(
                                    AccountIdentifier.Type.IBAN, DESTINATION_ACCOUNT));
            Debtor debtor =
                    new Debtor(
                            AccountIdentifier.create(AccountIdentifier.Type.IBAN, SOURCE_ACCOUNT));

            ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1.0);
            LocalDate executionDate = LocalDate.now();
            String currency = "EUR";

            listOfMockedPayments.add(
                    new Payment.Builder()
                            .withCreditor(creditor)
                            .withDebtor(debtor)
                            .withExactCurrencyAmount(amount)
                            .withExecutionDate(executionDate)
                            .withCurrency(currency)
                            .build());
        }

        return listOfMockedPayments;
    }
}
