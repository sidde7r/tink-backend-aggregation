package se.tink.backend.aggregation.agents.banks.seb.manual;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.banks.seb.SEBApiAgent;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SEBAgentTest extends AbstractAgentTest<SEBApiAgent> {

    private final ArgumentManager<UsernameArgumentEnum> manager =
            new ArgumentManager<>(UsernameArgumentEnum.values());
    private Credentials credentials;

    public SEBAgentTest() {
        super(SEBApiAgent.class);

        credentials = createCredentials("ssn", null, CredentialsTypes.MOBILE_BANKID);

        testContext = new AgentTestContext(credentials);
    }

    @Before
    public void setup() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("se", "seb-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                .addRefreshableItems(RefreshableItem.IDENTITY_DATA)
                .build()
                .testRefresh();
    }

    @Test
    public void testWholeTransferFlow() throws Exception {
        Transfer transfer = create1SEKTransfer();
        transfer.setSource(new SwedishIdentifier("source"));
        transfer.setDestination(new SwedishIdentifier("destination"));

        new AgentIntegrationTest.Builder("se", "seb-bankid")
                .addCredentialField(Field.Key.USERNAME, manager.get(UsernameArgumentEnum.USERNAME))
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .build()
                .testBankTransfer(transfer);
    }

    static Transfer create1SEKTransfer() {
        Transfer transfer = new Transfer();

        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);
        remittanceInformation.setValue("Tink dest");
        transfer.setRemittanceInformation(remittanceInformation);
        transfer.setSourceMessage("Tink source");
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(DateUtils.parseDate("2020-03-18"));

        return transfer;
    }
}
