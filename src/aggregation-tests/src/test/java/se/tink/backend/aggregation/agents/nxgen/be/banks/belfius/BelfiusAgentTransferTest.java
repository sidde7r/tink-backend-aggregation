package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentHelper;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.MessageType;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Ignore
public class BelfiusAgentTransferTest {
    private final ArgumentHelper helper = new ArgumentHelper("tink.username", "tink.password");

    //private final ArgumentHelper helper = new ArgumentHelper("tink.username", "tink.password");
    // Transfer tests will make actual transfers verify before running.
    // https://www.belfius.be/retail/nl/mijn-belfius/index.aspx#pan=cardNr
    // Check with concerned and remember to keep accounting.
    private static final String SOURCE_CHECKING_ACCOUNT = "";
    private static final String OWN_SAVINGS_ACCOUNT_NOT_ALLOWED_TO_TRANSFER_TO = ""; // Sparrekking +
    private static final String OWN_SAVINGS_ACCOUNT = ""; // Sparrekking
    private static final String OWN_NAME_FOR_DESTINATION = "";
    private static final String EXTERNAL_ACCOUNT = "";
    private static final String EXTERNAL_ACCOUNT_NAME_MANDATORY = "";
    private static final int AMOUNT_LAGOM = 10;
    private static final int AMOUNT_SMALL = 1;
    private static final int AMOUNT_LARGE_REQUIRES_SIGN_OF_AMOUNT = 2001; // Check if rules changed
    private static final int AMOUNT_LARGE_REQUIRES_SIGN_OF_AMOUNT_OR_BENEFICIARY = 2001; // Check if rules changed
    private static final Optional<Date> INSTANT_TRANSFER = Optional.empty();
    private static final Optional<Date> TRANSFER_NEXT_WEEK = Optional.of(Date.from(LocalDate.now()
            .plus(1, ChronoUnit.WEEKS).atStartOfDay(ZoneId.systemDefault()).toInstant()));

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

    @Ignore // Please check that SOURCE_CHECKING_ACCOUNT and OWN_SAVINGS_ACCOUNT_NOT_ALLOWED_TO_TRANSFER_TO set up
    @Test(expected = TransferExecutionException.class)
    public void testTransferToSavingsAccountThatIsNotAllowed() throws Exception {
        Amount amount = Amount.inEUR(AMOUNT_SMALL);
        SepaEurIdentifier sourceChecking = new SepaEurIdentifier(SOURCE_CHECKING_ACCOUNT);
        SepaEurIdentifier destionationSparrekingPlus = new SepaEurIdentifier(OWN_SAVINGS_ACCOUNT_NOT_ALLOWED_TO_TRANSFER_TO);
        String destName = OWN_NAME_FOR_DESTINATION;
        String message = "Spaarrekkening PLUS Tink trans " + toHumanAmount(amount);
        Transfer transfer = createTransfer(amount, sourceChecking, message, destionationSparrekingPlus, destName, INSTANT_TRANSFER);
        buildWithCredentials().testBankTransfer(transfer);
    }

    @Ignore // Please check that SOURCE_CHECKING_ACCOUNT and OWN_SAVINGS_ACCOUNT set up
    @Test
    public void testTransferToSavingsAccountThatIsAllowed() throws Exception {
        Amount amount = Amount.inEUR(AMOUNT_SMALL);
        SepaEurIdentifier sourceChecking = new SepaEurIdentifier(SOURCE_CHECKING_ACCOUNT);
        SepaEurIdentifier destionationSparreking = new SepaEurIdentifier(OWN_SAVINGS_ACCOUNT);
        String destName = OWN_NAME_FOR_DESTINATION;
        String message = "Spaarrekkening Tink trans " + toHumanAmount(amount);
        Transfer transfer = createTransfer(amount, sourceChecking, message, destionationSparreking, destName, INSTANT_TRANSFER);
        buildWithCredentials().testBankTransfer(transfer);
    }

    @Ignore// Please check that SOURCE_CHECKING_ACCOUNT Beneficiaries and EXTERNAL_ACCOUNT set up
    @Test
    public void testTransferToRegisteredExternalAccountThatIsAllowed() throws Exception {
        Amount amount = Amount.inEUR(AMOUNT_LARGE_REQUIRES_SIGN_OF_AMOUNT_OR_BENEFICIARY);
        SepaEurIdentifier sourceChecking = new SepaEurIdentifier(SOURCE_CHECKING_ACCOUNT);
        SepaEurIdentifier externalAccount = new SepaEurIdentifier(EXTERNAL_ACCOUNT);
        String destName = EXTERNAL_ACCOUNT_NAME_MANDATORY;
        String message = "Tink trans " + toHumanAmount(amount);
        Transfer transfer = createTransfer(amount, sourceChecking, message, externalAccount, destName, TRANSFER_NEXT_WEEK);
        buildWithCredentials().testBankTransfer(transfer);
    }

    @Ignore // Please check that SOURCE_CHECKING_ACCOUNT Beneficiaries and EXTERNAL_ACCOUNT set up
    @Test
    public void testTransferToUnRegisteredExternalAccountThatIsAllowed() throws Exception {
        Amount amount = Amount.inEUR(AMOUNT_LARGE_REQUIRES_SIGN_OF_AMOUNT_OR_BENEFICIARY);
        SepaEurIdentifier sourceChecking = new SepaEurIdentifier(SOURCE_CHECKING_ACCOUNT);
        SepaEurIdentifier externalAccount = new SepaEurIdentifier(EXTERNAL_ACCOUNT);
        String destName = EXTERNAL_ACCOUNT_NAME_MANDATORY;
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
