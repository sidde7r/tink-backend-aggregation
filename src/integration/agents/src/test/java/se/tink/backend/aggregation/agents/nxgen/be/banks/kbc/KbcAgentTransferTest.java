package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.UsernameArgumentEnum;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.enums.MessageType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class KbcAgentTransferTest {
    // NB  m4ri needs to be installed
    // See ../tools/libkbc_wbaes_src/README

    private enum Arg implements ArgumentManagerEnum {
        SOURCE_ACCOUNT,
        DESTINATION_ACCOUNT,
        DESTINATION_NAME;

        private final boolean optional;

        Arg(boolean optional) {
            this.optional = optional;
        }

        Arg() {
            this.optional = false;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }
    }

    private final ArgumentManager<Arg> helper = new ArgumentManager<>(Arg.values());
    private final ArgumentManager<UsernameArgumentEnum> usernameHelper =
            new ArgumentManager<>(UsernameArgumentEnum.values());

    private final AgentIntegrationTest.Builder builder =
            new AgentIntegrationTest.Builder("be", "be-kbc-cardreader")
                    .setUserLocale(Locale.FRENCH.getLanguage())
                    .loadCredentialsBefore(true)
                    .saveCredentialsAfter(true);

    private static int AMOUNT_SMALL = 10;
    private static final Optional<Date> INSTANT_TRANSFER = Optional.empty();
    private static final Optional<Date> TRANSFER_NEXT_WEEK =
            Optional.of(
                    Date.from(
                            LocalDate.now()
                                    .plus(1, ChronoUnit.WEEKS)
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()));

    @Before
    public void before() {
        helper.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    private AgentIntegrationTest buildWithCredentials() {
        return builder.addCredentialField(
                        Field.Key.USERNAME, usernameHelper.get(UsernameArgumentEnum.USERNAME))
                .build();
    }

    // Will create a real transfer, handle with care
    @Test
    public void testTransfer() throws Exception {
        String sourceCheckingAccount = helper.get(Arg.SOURCE_ACCOUNT);
        String destinationAccount = helper.get(Arg.DESTINATION_ACCOUNT);
        String holderNameOfDestinationAccount = helper.get(Arg.DESTINATION_NAME);
        Amount amount = Amount.inEUR(AMOUNT_SMALL);
        SepaEurIdentifier sourceAccount = new SepaEurIdentifier(sourceCheckingAccount);
        SepaEurIdentifier destionationAccount = new SepaEurIdentifier(destinationAccount);
        String message = "Tink trans " + toHumanAmount(amount);
        Transfer transfer =
                createTransfer(
                        amount,
                        sourceAccount,
                        message,
                        destionationAccount,
                        holderNameOfDestinationAccount,
                        TRANSFER_NEXT_WEEK);
        buildWithCredentials().testBankTransfer(transfer);
    }

    private String toHumanAmount(Amount amount) {
        return String.format("%.2f %s", amount.getValue(), amount.getCurrency());
    }

    private Transfer createTransfer(
            Amount amount,
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
