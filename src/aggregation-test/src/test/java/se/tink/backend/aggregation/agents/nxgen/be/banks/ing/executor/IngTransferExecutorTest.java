package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor;

import java.util.Date;
import java.util.Locale;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngTestConfig;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngAutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.enums.MessageType;
import se.tink.libraries.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.i18n.Catalog;
import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage.OTP_COUNTER;

@Ignore
public class IngTransferExecutorTest {
    //
    // Use IngTwoFactorAuthenticatorTest to populate
    // persistent storage in IngTestConfig
    // VIRTUAL_CARD_NUMBER below and OTP Counter in setUP

    public static final String VIRTUAL_CARD_NUMBER = "";
    private Credentials credentials;
    private IngApiClient apiClient;
    private PersistentStorage persistentStorage;
    private IngHelper ingHelper;
    private IngAutoAuthenticator autoAuthenticator;
    private TransferController transferController;
    private IngTransferExecutor ingTransferExecutor;
    private AccountIdentifier currentAccountSource;
    private AccountIdentifier savingsAccountSource;
    private AccountIdentifier savedBeneficiaryDestination;
    private AccountIdentifier thirdPartyDestination;
    private IngTransferHelper ingTransferHelper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        IngTestConfig ingTestConfig = IngTestConfig.createForAutoAuthentication();
        this.credentials = ingTestConfig.getTestUserCredentials();
        this.apiClient = ingTestConfig.getTestApiClient();
        this.persistentStorage = ingTestConfig.getTestPersistentStorage();
        // Change each test round
        this.persistentStorage.put(OTP_COUNTER, "");
        this.ingHelper = ingTestConfig.getTestIngHelper();
        this.persistentStorage.put(IngConstants.Storage.VIRTUAL_CARDNUMBER, VIRTUAL_CARD_NUMBER);
        this.ingTransferHelper = new IngTransferHelper(new Catalog(new Locale("fr", "BE")));
        this.autoAuthenticator = new IngAutoAuthenticator(apiClient, persistentStorage, ingHelper);
        autoAuthenticator.autoAuthenticate();
        this.ingTransferExecutor =  new IngTransferExecutor(apiClient, persistentStorage, ingHelper, ingTransferHelper);
        this.transferController = new TransferController(null, ingTransferExecutor, null, null);

        // Populate with account numbers for test user
        this.currentAccountSource = new SepaEurIdentifier("");
        this.savingsAccountSource = new SepaEurIdentifier("");

        this.savedBeneficiaryDestination = new SepaEurIdentifier("");
        this.thirdPartyDestination = new SepaEurIdentifier("");
        this.thirdPartyDestination.setName("");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Copy/paste new otp counter from: persistentStorage = " + this.persistentStorage);
        System.out.println("this.persistentStorage.put(OTP_COUNTER, \"" + this.persistentStorage.get(OTP_COUNTER) + "\");");
        persistentStorage.keySet().iterator().forEachRemaining(key -> {
            if (!key.equalsIgnoreCase(OTP_COUNTER)) {
                System.out.println("this.persistentStorage.put(\"" + key + "\", \"" + persistentStorage.get(key) + "\");" );
            }
        });
    }



    @Test
    public void ensureInternalTransfer_withFreeTextMsg_succeeds() throws Exception {
        Transfer transfer = createTransfer(currentAccountSource, savingsAccountSource,
                null, MessageType.FREE_TEXT, "Transfer test");
        transferController.execute(transfer);
    }

    @Test
    public void ensureTransferToSavedBeneficiary_withFreeTextMsg_succeeds() throws Exception {
        Transfer transfer = createTransfer(currentAccountSource, savedBeneficiaryDestination,
                DateUtils.addDays(DateUtils.getToday(), 7), MessageType.FREE_TEXT, "Transfer test");
        transferController.execute(transfer);
    }

    @Test
    public void ensureTransferToSavedBeneficiary_withStructuredMsg_succeeds() throws Exception {
        Transfer transfer = createTransfer(currentAccountSource, savedBeneficiaryDestination,
                DateUtils.addDays(DateUtils.getToday(), 7), MessageType.STRUCTURED, "+++010/8068/17183+++");
        transferController.execute(transfer);
    }

    @Test
    public void ensureThirdPartyTransfer_withFreeTextMsg_succeeds() throws Exception {
        Transfer transfer = createTransfer(currentAccountSource, thirdPartyDestination,
                DateUtils.addDays(DateUtils.getToday(), 7), MessageType.FREE_TEXT, "Transfer test");
        transferController.execute(transfer);
    }

    @Test
    public void ensureThirdPartyTransfer_withStructuredMsg_succeeds() throws Exception {
        Transfer transfer = createTransfer(currentAccountSource, thirdPartyDestination,
                DateUtils.addDays(DateUtils.getToday(), 7), MessageType.STRUCTURED, "+++010/8068/17183+++");
        transferController.execute(transfer);
    }

    @Test
    public void ensureSavingsAccountTransfer_toExternalAccount_fails() throws Exception {
        expectedException.expect(TransferExecutionException.class);
        expectedException.expectMessage(
                IngConstants.EndUserMessage.TRANSFER_TO_EXTERNAL_ACCOUNTS_NOT_ALLOWED.getKey().get());

        Transfer transfer = createTransfer(savingsAccountSource, savedBeneficiaryDestination, null,
                MessageType.FREE_TEXT, "Transfer test");
        transferController.execute(transfer);
    }

    private Transfer createTransfer(AccountIdentifier sourceAccount, AccountIdentifier destinationAccount,
            Date dueDate, MessageType messageType, String destinationMessage) {
        Transfer transfer = new Transfer();

        transfer.setSource(sourceAccount);
        transfer.setDestination(destinationAccount);
        transfer.setAmount(Amount.inEUR(1.0d));
        transfer.setDueDate(dueDate);
        transfer.setMessageType(messageType);
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDestinationMessage(destinationMessage);

        return transfer;
    }
}
