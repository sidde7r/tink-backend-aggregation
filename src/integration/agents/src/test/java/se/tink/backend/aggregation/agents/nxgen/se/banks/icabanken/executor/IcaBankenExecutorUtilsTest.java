package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor;

import static org.junit.Assert.*;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.junit.Test;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class IcaBankenExecutorUtilsTest {
    private static final String SHORT_SOURCE_MESSAGE = "Short source message";
    private static final String VERY_VERY_VERY_LONG_SOURCE_MESSAGE =
            "Very very very long source message";

    @Test
    public void getTruncatedSourceMessage_shouldTruncateAt25() {
        Transfer transfer = new Transfer();
        transfer.setType(TransferType.PAYMENT);
        transfer.setSourceMessage(VERY_VERY_VERY_LONG_SOURCE_MESSAGE);

        final String truncatedSourceMessage =
                IcaBankenExecutorUtils.getTruncatedSourceMessage(transfer);

        assertThat(truncatedSourceMessage, IsNot.not(VERY_VERY_VERY_LONG_SOURCE_MESSAGE));
        assertThat(truncatedSourceMessage.length(), Is.is(25));
    }

    @Test
    public void getTruncatedSourceMessage_shouldNotTruncateIfLessThan25() {
        Transfer transfer = new Transfer();
        transfer.setType(TransferType.PAYMENT);
        transfer.setSourceMessage(SHORT_SOURCE_MESSAGE);

        final String truncatedSourceMessage =
                IcaBankenExecutorUtils.getTruncatedSourceMessage(transfer);

        assertThat(truncatedSourceMessage, Is.is(SHORT_SOURCE_MESSAGE));
    }

    @Test
    public void getTruncatedSourceMessage_shouldHandleNull() {
        Transfer transfer = new Transfer();
        transfer.setType(TransferType.PAYMENT);
        transfer.setSourceMessage(null);

        final String truncatedSourceMessage =
                IcaBankenExecutorUtils.getTruncatedSourceMessage(transfer);

        assertNull(truncatedSourceMessage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTruncatedSourceMessage_shouldThrowWhenBankTransfer() {
        Transfer transfer = new Transfer();
        transfer.setSourceMessage(SHORT_SOURCE_MESSAGE);
        transfer.setType(TransferType.BANK_TRANSFER);

        final String truncatedSourceMessage =
                IcaBankenExecutorUtils.getTruncatedSourceMessage(transfer);
    }
}
