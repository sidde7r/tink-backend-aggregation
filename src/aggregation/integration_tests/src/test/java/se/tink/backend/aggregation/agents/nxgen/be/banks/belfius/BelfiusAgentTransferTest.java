package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.MessageType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Ignore
public class BelfiusAgentTransferTest {
    private final ArgumentHelper helper = new ArgumentHelper(
            "tink.username",
            "tink.password",
            "sourceaccount",
            "destinationaccount",
            "destinationname");

    // Transfer tests will make actual transfers verify before running.
    // https://www.belfius.be/retail/nl/mijn-belfius/index.aspx#pan=cardNr
    // Check with concerned and remember to keep accounting.
    private static final int AMOUNT_LAGOM = 10;
    private static final int AMOUNT_SMALL = 1;
    private static final int AMOUNT_LARGE_REQUIRES_SIGN_OF_AMOUNT = 2001; // Check if rules changed
    private static final int AMOUNT_LARGE_REQUIRES_SIGN_OF_AMOUNT_OR_BENEFICIARY = 2001; // Check if rules changed
    private static final Optional<Date> INSTANT_TRANSFER = Optional.empty();
    private static final Optional<Date> TRANSFER_NEXT_WEEK = Optional.of(Date.from(LocalDate.now()
            .plus(1, ChronoUnit.WEEKS).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    private String sourceCheckingAccount;
    private String ownSavingsAccountNotAllowedToTransferTo; // Sparrekking +
    private String ownSavingsAccount; // Sparrekking
    private String ownNameForDestination;
    private String externalAccount;
    private String externalAccountNameMandatory;

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-belfius-cardreader")
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true);
    @Before
    public void before() {
       helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentHelper.afterClass();
    }
    private AgentIntegrationTest buildWithCredentials() {
        return builder.addCredentialField(Field.Key.USERNAME, helper.get("tink.username"))
                .addCredentialField(Field.Key.PASSWORD, helper.get("tink.password"))
                .build();
    }

    @Ignore// Please check that sourceCheckingAccount and ownSavingsAccountNotAllowedToTransferTo set up
    @Test(expected = TransferExecutionException.class)
    public void testTransferToSavingsAccountThatIsNotAllowed() throws Exception {
        sourceCheckingAccount = helper.get("sourceaccount");
        ownSavingsAccountNotAllowedToTransferTo = helper.get("destinationaccount");
        ownNameForDestination = helper.get("destinationname");
        Amount amount = Amount.inEUR(AMOUNT_SMALL);
        SepaEurIdentifier sourceChecking = new SepaEurIdentifier(sourceCheckingAccount);
        SepaEurIdentifier destionationSparrekingPlus = new SepaEurIdentifier(ownSavingsAccountNotAllowedToTransferTo);
        String destName = ownNameForDestination;
        String message = "Spaarrekkening PLUS Tink trans " + toHumanAmount(amount);
        Transfer transfer = createTransfer(amount, sourceChecking, message, destionationSparrekingPlus, destName, INSTANT_TRANSFER);
        buildWithCredentials().testBankTransfer(transfer);
    }

    @Ignore // Please check that sourceCheckingAccount and ownSavingsAccount set up
    @Test
    public void testTransferToSavingsAccountThatIsAllowed() throws Exception {
        sourceCheckingAccount = helper.get("sourceaccount");
        ownSavingsAccount = helper.get("destinationaccount");
        ownNameForDestination = helper.get("destinationname");
        Amount amount = Amount.inEUR(AMOUNT_SMALL);
        SepaEurIdentifier sourceChecking = new SepaEurIdentifier(sourceCheckingAccount);
        SepaEurIdentifier destionationSparreking = new SepaEurIdentifier(ownSavingsAccount);
        String destName = ownNameForDestination;
        String message = "Spaarrekkening Tink trans " + toHumanAmount(amount);
        Transfer transfer = createTransfer(amount, sourceChecking, message, destionationSparreking, destName, INSTANT_TRANSFER);
        buildWithCredentials().testBankTransfer(transfer);
    }

    @Ignore// Please check that sourceCheckingAccount Beneficiaries and externalAccount set up
    @Test
    public void testTransferToRegisteredExternalAccountThatIsAllowed() throws Exception {
        sourceCheckingAccount = helper.get("sourceaccount");
        externalAccount = helper.get("destinationaccount");
        externalAccountNameMandatory = helper.get("destinationname");
        Amount amount = Amount.inEUR(AMOUNT_LARGE_REQUIRES_SIGN_OF_AMOUNT_OR_BENEFICIARY);
        SepaEurIdentifier sourceChecking = new SepaEurIdentifier(sourceCheckingAccount);
        SepaEurIdentifier externalAccount = new SepaEurIdentifier(this.externalAccount);
        String destName = externalAccountNameMandatory;
        String message = "Tink trans " + toHumanAmount(amount);
        Transfer transfer = createTransfer(amount, sourceChecking, message, externalAccount, destName, TRANSFER_NEXT_WEEK);
        buildWithCredentials().testBankTransfer(transfer);
    }

    @Ignore // Please check that sourceCheckingAccount Beneficiaries and externalAccount set up
    @Test
    public void testTransferToUnRegisteredExternalAccountThatIsAllowed() throws Exception {
        sourceCheckingAccount = helper.get("sourceaccount");
        externalAccount = helper.get("destinationaccount");
        externalAccountNameMandatory = helper.get("destinationname");

        Amount amount = Amount.inEUR(AMOUNT_LARGE_REQUIRES_SIGN_OF_AMOUNT_OR_BENEFICIARY);
        SepaEurIdentifier sourceChecking = new SepaEurIdentifier(sourceCheckingAccount);
        SepaEurIdentifier externalAccount = new SepaEurIdentifier(this.externalAccount);
        String destName = externalAccountNameMandatory;
        String message = "Tink trans " + toHumanAmount(amount);
        Transfer transfer = createTransfer(amount, sourceChecking, message, externalAccount, destName, INSTANT_TRANSFER);
        buildWithCredentials().testBankTransfer(transfer);
    }

    private String toHumanAmount(Amount amount) {
        return String.format("%.2f %s", amount.getValue(), amount.getCurrency());
    }

    private Transfer createTransfer(Amount amount,
                                    SepaEurIdentifier source,
                                    String message,
                                    SepaEurIdentifier destination,
                                    String destinationName,
                                    Optional<Date> dueDate) {
        Transfer transfer = new Transfer();
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSource(source);
        dueDate.ifPresent(transfer::setDueDate);
        transfer.setAmount(amount);
        destination.setName(destinationName);
        transfer.setDestination(destination);
        transfer.setMessageType(MessageType.FREE_TEXT);
        transfer.setSourceMessage(message);
        transfer.setDestinationMessage(message);
        return transfer;
    }
}
