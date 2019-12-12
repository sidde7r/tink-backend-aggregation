package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import java.util.Calendar;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.SsnArgumentEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ToAccountFromAccountArgumentEnum;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

/** Careful with this test! Actual transfers made! */
public class SwedbankSEPaymentTest {
    private final ArgumentManager<SsnArgumentEnum> ssnManager =
            new ArgumentManager<>(SsnArgumentEnum.values());
    private final ArgumentManager<ToAccountFromAccountArgumentEnum> toFromManager =
            new ArgumentManager<>(ToAccountFromAccountArgumentEnum.values());

    @Before
    public void setUp() throws Exception {
        toFromManager.before();
        ssnManager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testTransfer() throws Exception {
        final AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("SE", "swedbank-bankid")
                        .addCredentialField(Field.Key.USERNAME, ssnManager.get(SsnArgumentEnum.SSN))
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
        builder.build().testBankTransfer(createTransfer());
    }

    private Transfer createTransfer() {
        Transfer transfer = new Transfer();
        transfer.setSource(
                AccountIdentifier.create(
                        Type.SE, toFromManager.get(ToAccountFromAccountArgumentEnum.FROM_ACCOUNT)));
        transfer.setDestination(
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE,
                        toFromManager.get(ToAccountFromAccountArgumentEnum.TO_ACCOUNT)));
        transfer.setAmount(Amount.inSEK(1d));
        transfer.setType(TransferType.BANK_TRANSFER);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 1);
        transfer.setDueDate(c.getTime());
        transfer.setDestinationMessage("This is a test transfer");

        return transfer;
    }
}
