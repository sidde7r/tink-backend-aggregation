package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankSEAgentTest {
    private AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("se", "swedbank-bankid")
                    .loadCredentialsBefore(false)
                    .saveCredentialsAfter(false)
                    .expectLoggedIn(false);

    public enum Arg implements ArgumentManagerEnum {
        SSN(false),
        FROM_ACCOUNT(true),
        TO_ACCOUNT(true);

        private final boolean optional;

        Arg(boolean optional) {
            this.optional = optional;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void setUp() throws Exception {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefresh() throws Exception {
        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }

    @Test
    public void testPayment() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(AccountIdentifier.Type.SE, manager.get(Arg.FROM_ACCOUNT)));
        transfer.setDestination(AccountIdentifier.create(Type.SE_BG, manager.get(Arg.TO_ACCOUNT)));
        transfer.setAmount(Amount.inSEK(1d));
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(getPaymentDueDate());
        transfer.setDestinationMessage("Reference");

        builder.addCredentialField(Field.Key.USERNAME, manager.get(Arg.SSN))
                .build()
                .testBankTransfer(transfer);
    }

    private Date getPaymentDueDate() {
        return Date.from(
                LocalDate.now().atStartOfDay().plusDays(5).atZone(ZoneId.of("CET")).toInstant());
    }
}
