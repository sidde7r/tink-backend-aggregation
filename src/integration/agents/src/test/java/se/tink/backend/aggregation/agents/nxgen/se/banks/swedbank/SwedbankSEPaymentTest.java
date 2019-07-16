package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import java.util.Calendar;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

/** Careful with this test! Actual transfers made! */
public class SwedbankSEPaymentTest {
    private enum Arg {
        USERNAME, // 12 digit SSN
        FROMACCOUNT, // swedish account number
        TOACCOUNT, // swedish account number
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
    public void testTransfer() throws Exception {
        final AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("SE", "swedbank-bankid")
                        .addCredentialField(Field.Key.USERNAME, manager.get(Arg.USERNAME))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
        builder.build().testBankTransfer(createTransfer());
    }

    private Transfer createTransfer() {
        Transfer transfer = new Transfer();
        transfer.setSource(AccountIdentifier.create(Type.SE, manager.get(Arg.FROMACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(AccountIdentifier.Type.SE, manager.get(Arg.TOACCOUNT)));
        transfer.setAmount(Amount.inSEK(1d));
        transfer.setType(TransferType.BANK_TRANSFER);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        transfer.setDestinationMessage("This is a test transfer");

        return transfer;
    }
}
