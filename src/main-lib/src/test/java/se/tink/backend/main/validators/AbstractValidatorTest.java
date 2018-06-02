package se.tink.backend.main.validators;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import se.tink.backend.common.config.TransfersConfiguration;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.Amount;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.main.validators.exception.AbstractTransferException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import se.tink.libraries.date.DateUtils;

import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractValidatorTest {
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    protected static final AccountIdentifier PG_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "4321-8765");
    protected static final AccountIdentifier BARNCANCERFONDEN_BG = AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9020900");
    protected static final AccountIdentifier BARNCANCERFONDEN_PG = AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9020900");
    protected static final AccountIdentifier NORDEA_SSN_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE, "33008607015537");
    protected static final AccountIdentifier SHB_IDENTIFIER = AccountIdentifier.create(AccountIdentifier.Type.SE, "6152135538858");

    TransfersConfiguration transferConfiguration;

    protected static final String USER_ID = "627fba23f10a4eb9bb667be6f144151f";
    protected Transfer transfer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    protected Transfer createPayment(AccountIdentifier destination) {
        Transfer payment = createTransfer(BARNCANCERFONDEN_BG);
        payment.setType(TransferType.PAYMENT);
        payment.setDestination(destination);
        payment.setDestinationMessage("destination message");
        return payment;
    }

    protected Transfer createEInvoice(AccountIdentifier destination) {
        Transfer eInvoice = createPayment(destination);
        eInvoice.setType(TransferType.EINVOICE);

        return eInvoice;
    }

    TransfersConfiguration mockTransferConfiguration() {
        TransfersConfiguration configuration = mock(TransfersConfiguration.class);

        when(configuration.isEnabled()).thenReturn(true);

        return configuration;
    }

    protected Transfer createTransfer(AccountIdentifier destination) {
        Transfer transfer = new Transfer();
        transfer.setUserId(UUIDUtils.fromTinkUUID(USER_ID));
        transfer.setAmount(Amount.inSEK(150.00));
        transfer.setType(TransferType.BANK_TRANSFER);
        transfer.setSource(SHB_IDENTIFIER);
        transfer.setDestination(destination);
        transfer.setDueDate(DateUtils.getNextBusinessDay(new Date()));
        transfer.setId(UUIDUtils.fromTinkUUID("2a4a3f01038d45879037e41d7d58322e"));

        return transfer;
    }

    protected User createUser() {
        User user = new User();
        user.setId(USER_ID);
        UserProfile profile = new UserProfile();
        profile.setLocale("en_US");
        user.setProfile(profile);
        user.setFlags(Lists.newArrayList(FeatureFlags.TRANSFERS));

        return user;
    }

    protected void expect(AbstractTransferException.LogMessage logMessage) {
        expect(getExpectedMessage(logMessage), TransferValidationException.class);
    }

    protected void expect(AbstractTransferException.LogMessageParametrized message, Object... parameters) {
        expect(message.with(parameters), TransferValidationException.class);
    }

    protected void expect(String message, Class<? extends Throwable> exception) {
        expectedException.expect(exception);
        expectedException.expectMessage(message);
    }

    public abstract String getExpectedMessage(AbstractTransferException.LogMessage message);
}
