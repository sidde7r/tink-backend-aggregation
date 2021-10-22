package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.transfer.rpc.Transfer;

public class SkandiaBankenExecutorUtilsTest {

    @Test
    public void shouldFormatSevenDigitBankgiroCorrectly() {
        // given
        Transfer transfer = new Transfer();
        transfer.setDestination(new BankGiroIdentifier("9008004"));

        // when
        String formattedGirNumber = SkandiaBankenExecutorUtils.formatGiroNumber(transfer);

        // then
        assertThat(formattedGirNumber).isEqualTo("900-8004");
    }

    @Test
    public void shouldFormatEightDigitBankgiroCorrectly() {
        // given
        Transfer transfer = new Transfer();
        transfer.setDestination(new BankGiroIdentifier("51225860"));

        // when
        String formattedGirNumber = SkandiaBankenExecutorUtils.formatGiroNumber(transfer);

        // then
        assertThat(formattedGirNumber).isEqualTo("5122-5860");
    }

    @Test
    public void shouldFormatPlusgiroCorrectly() {
        // given
        Transfer transfer = new Transfer();
        transfer.setDestination(new PlusGiroIdentifier("9003518"));

        // when
        String formattedGirNumber = SkandiaBankenExecutorUtils.formatGiroNumber(transfer);

        // then
        assertThat(formattedGirNumber).isEqualTo("900351-8");
    }

    @Test
    public void shouldThrowExceptionIfNotBankgiroOrPlusgiro() {
        // given
        Transfer transfer = new Transfer();
        transfer.setDestination(new IbanIdentifier("SE1390200000090242222222"));

        // when
        Throwable throwable =
                catchThrowable(() -> SkandiaBankenExecutorUtils.formatGiroNumber(transfer));

        // then
        assertThat(throwable).isInstanceOf(NotImplementedException.class);
    }
}
