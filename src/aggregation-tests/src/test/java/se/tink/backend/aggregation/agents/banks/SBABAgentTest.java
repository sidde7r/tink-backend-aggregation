package se.tink.backend.aggregation.agents.banks;

import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.banks.sbab.SBABAgent;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.utils.transfer.TransferMessageException;
import se.tink.libraries.account.identifiers.TestAccount;
import se.tink.libraries.social.security.TestSSN;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.date.DateUtils;

public class SBABAgentTest extends AbstractAgentTest<SBABAgent> {

    private Credentials credentials;
    private SBABAgent agent;

    public SBABAgentTest() throws Exception {
        super(SBABAgent.class);

        credentials = createCredentials(TestSSN.TOLVAN, null, CredentialsTypes.MOBILE_BANKID);

        testContext = new AgentTestContext(credentials);
        agent = (SBABAgent) factory
                .create(SBABAgent.class, createRefreshInformationRequest(credentials, new Provider()), testContext);
    }

    @Test
    public void testLoginWithMobileBankIdAndRefreshAccounts() throws Exception {
        testAgent(credentials);
        System.out.println(MAPPER.writeValueAsString(testContext.getUpdatedAccounts()));
        System.out.println(MAPPER.writeValueAsString(testContext.getTransactions()));
        System.out.println(MAPPER.writeValueAsString(testContext.getTransfers()));
    }

    @Test
    public void testTransferInternal() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.SBAB_ANOTHER_NO));

        testTransfer(credentials, transfer);
    }

    @Test
    public void testTransferExternalAvanza() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.AVANZA_NO));

        testTransfer(credentials, transfer);
    }

    @Test
    public void testTransferExternalHandelsbanken() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.HANDELSBANKEN_NO));

        testTransfer(credentials, transfer);
    }

    @Test
    public void testTransferInternalWithTwoDecimals() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setAmount(Amount.inSEK(1.34));
        transfer.setDestinationMessage("Tink 2dec");
        transfer.setSourceMessage("Tink 2dec");
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(DateUtils.getToday());

        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.SBAB_ANOTHER_NO));

        testTransfer(credentials, transfer);
    }

    @Test
    public void testTransferInternalWithThreeDecimals() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setAmount(Amount.inSEK(1.567));
        transfer.setDestinationMessage("Tink 3dec");
        transfer.setSourceMessage("Tink 3dec");
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(DateUtils.getToday());

        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.SBAB_ANOTHER_NO));

        testTransfer(credentials, transfer);
    }

    @Test
    public void testTransferExternalWithThreeDecimals() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setAmount(Amount.inSEK(1.567));
        transfer.setDestinationMessage("Tink 3dec");
        transfer.setSourceMessage("Tink 3dec");
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setDueDate(DateUtils.getToday());

        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.HANDELSBANKEN_NO));

        testTransfer(credentials, transfer);
    }

    /**
     * Prior to running the below test one must make sure that the recipient is not saved at SBAB.
     * Note: the new recipient is not saved at SBAB when doing a transfer to a new recipient through Tink.
     */
    @Test
    public void testTransferExternalNewRecipient() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.HANDELSBANKEN_ANOTHER_NO));

        testTransfer(credentials, transfer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferWithInvalidDestination() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier("12345"));

        // The destination is invalid and will not be able to be converted to a URI.
        // This will result in an IllegalArgumentException.

        testTransferException(credentials, transfer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferWithInvalidSource() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSource(new SwedishIdentifier("12345"));
        transfer.setDestination(new SwedishIdentifier(TestAccount.HANDELSBANKEN_NO));

        // The source is invalid and will not be able to be converted to a URI.
        // This will result in an IllegalArgumentException.

        testTransferException(credentials, transfer);
    }

    @Test(expected = TransferExecutionException.class)
    public void testTransferWithSourceFromAnotherBank() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSource(new SwedishIdentifier(TestAccount.HANDELSBANKEN_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.HANDELSBANKEN_ANOTHER_NO));

        // The source account is valid but is not a user account.
        // This will result in a TransferExecutionException.

        testTransferException(credentials, transfer);
    }

    @Test(expected = TransferExecutionException.class)
    public void testTransferWithSameSourceAndDestination() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.SBAB_NO));

        // The source and destination accounts are valid but refer to the same account.
        // This will result in a TransferExecutionException.

        testTransferException(credentials, transfer);
    }

    @Test(expected = TransferMessageException.class)
    public void testTransferExternalWithTooLongDestinationMessage() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setDestinationMessage("This message is too long for being a destination message");
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.HANDELSBANKEN_NO));

        // The destination message is too long.
        // This will result in a TransferMessageException.

        testTransferException(credentials, transfer);
    }

    @Test(expected = TransferMessageException.class)
    public void testTransferExternalWithTooLongSourceMessage() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSourceMessage("This message is too long for being a source message");
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.HANDELSBANKEN_NO));

        // The source message is too long.
        // This will result in a TransferMessageException.

        testTransferException(credentials, transfer);
    }

    @Test(expected = TransferMessageException.class)
    public void testTransferInternalWithTooLongDestinationMessage() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setDestinationMessage("This message is too long for being a destination message");
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.SBAB_ANOTHER_NO));

        // The destination message is too long.
        // This will result in a TransferMessageException.

        testTransferException(credentials, transfer);
    }

    @Test(expected = TransferMessageException.class)
    public void testTransferInternalWithTooLongSourceMessage() throws Exception {
        Transfer transfer = SBABAgentTestBase.create1SEKTransfer();
        transfer.setSourceMessage("This message is too long for being a source message");
        transfer.setSource(new SwedishIdentifier(TestAccount.SBAB_NO));
        transfer.setDestination(new SwedishIdentifier(TestAccount.SBAB_ANOTHER_NO));

        // The source message is too long.
        // This will result in a TransferMessageException.

        testTransferException(credentials, transfer);
    }

    /**
     * This is to be able to use a client without SSL validation when testing mortgages.
     */
    private void switchToMortgageTestAgent(String ssn) throws Exception {
        if (ssn == null) {
            ssn = TestSSN.TOLVAN;
        }

        Provider provider = new Provider();
        provider.setPayload("{\"isSwitchMortgageProviderTest\":\"true\"}");
        credentials = createCredentials(ssn, null, CredentialsTypes.MOBILE_BANKID);
        agent = (SBABAgent) factory
                .create(SBABAgent.class, createRefreshInformationRequest(credentials, provider), testContext);
    }
}
