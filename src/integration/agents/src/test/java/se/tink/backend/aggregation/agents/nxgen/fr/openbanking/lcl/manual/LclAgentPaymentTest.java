package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.manual;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Reference;

public class LclAgentPaymentTest {

    private AgentIntegrationTest.Builder builder;

    private final ArgumentManager<ArgumentManager.PsuIdArgumentEnum> manager =
            new ArgumentManager<>(ArgumentManager.PsuIdArgumentEnum.values());
    private final ArgumentManager<LclAgentPaymentTest.Arg> creditorDebtorManager =
            new ArgumentManager<>(LclAgentPaymentTest.Arg.values());

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("fr", "fr-lcl-ob")
                        .setFinancialInstitutionId("lcl")
                        .setAppId("tink")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testPayments() throws Exception {
        manager.before();
        creditorDebtorManager.before();

        builder.build().testTinkLinkPayment(createRealDomesticPayment());
    }

    private List<Payment> createRealDomesticPayment() {
        AccountIdentifier creditorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(LclAgentPaymentTest.Arg.CREDITOR_ACCOUNT));

        AccountIdentifier debtorAccountIdentifier =
                new IbanIdentifier(
                        creditorDebtorManager.get(LclAgentPaymentTest.Arg.DEBTOR_ACCOUNT));

        return Collections.singletonList(
                new Payment.Builder()
                        .withCreditor(new Creditor(creditorAccountIdentifier))
                        .withDebtor(new Debtor(debtorAccountIdentifier))
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                        .withCurrency("EUR")
                        .withReference(new Reference("Message", "ReferenceToCreditor"))
                        .withUniqueId(UUID.randomUUID().toString())
                        .build());
    }

    private enum Arg implements ArgumentManager.ArgumentManagerEnum {
        DEBTOR_ACCOUNT,
        CREDITOR_ACCOUNT;

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }
}
