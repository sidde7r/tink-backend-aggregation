package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

public class SkandiaBankenPaymentExecutorTest {
    private SkandiaBankenPaymentExecutor objectUnderTest;

    @Before
    public void setUp() {
        objectUnderTest =
                new SkandiaBankenPaymentExecutor(
                        mock(SkandiaBankenApiClient.class),
                        mock(SupplementalInformationController.class));
    }

    @Test
    public void shouldThrowTransferExceptionWhenDestinationIsNotBgOrPg() {
        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "throwIfNotBgOrPgPayment", getA2ATransfer());

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage(
                        "Provided payment type is not supported. Only PG and BG type is supported.");
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenDestinationIsBg() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfNotBgOrPgPayment",
                                        getBgTransfer()));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenDestinationIsPg() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfNotBgOrPgPayment",
                                        getPgTransfer()));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldThrowTransferExceptionWhenAmountIsLessThan1Sek() {
        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest,
                                "throwIfAmountIsLessThanMinAmount",
                                getLessThan1SekTransfer());

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage(
                        "Minimum amount of payment is 1 SEK. This is a restriction set by the bank.");
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenAmountIs1Sek() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfAmountIsLessThanMinAmount",
                                        get1SekTransfer()));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenAmountIsGreaterThan1Sek() {
        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfAmountIsLessThanMinAmount",
                                        getGreaterThan1SekTransfer()));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldThrowTransferExceptionWhenUnstructuredRefIsLongerThan175Chars() {
        // given
        Transfer transfer =
                getTransferWithUnstructuredRemittanceInformation(
                        "asdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwerty");

        // when
        final ThrowingCallable callable =
                () ->
                        ReflectionTestUtils.invokeMethod(
                                objectUnderTest, "throwIfUnstructuredRefLongerThanMax", transfer);

        // then
        assertThatThrownBy(callable)
                .isInstanceOf(TransferExecutionException.class)
                .hasMessage(
                        "Unstructured reference longer than 175 chars. Bank crops reference if longer, therefore we cancel the payment.");
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenUnstructuredRefIs175Chars() {
        // given
        Transfer transfer =
                getTransferWithUnstructuredRemittanceInformation(
                        "asdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfqwertyasdfq");

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfUnstructuredRefLongerThanMax",
                                        transfer));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldNotThrowTransferExceptionWhenUnstructuredRefIsShorterThan175Chars() {
        // given
        Transfer transfer =
                getTransferWithUnstructuredRemittanceInformation("asdfqwertyasdfqwerty");

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        objectUnderTest,
                                        "throwIfUnstructuredRefLongerThanMax",
                                        transfer));

        // then
        assertNull(thrown);
    }

    private Transfer getA2ATransfer() {
        Transfer transfer = new Transfer();
        transfer.setDestination(new SwedishIdentifier("91599999999"));
        return transfer;
    }

    private Transfer getBgTransfer() {
        Transfer transfer = new Transfer();
        transfer.setDestination(new BankGiroIdentifier("9999999"));
        return transfer;
    }

    private Transfer getPgTransfer() {
        Transfer transfer = new Transfer();
        transfer.setDestination(new PlusGiroIdentifier("9999999"));
        return transfer;
    }

    private Transfer getLessThan1SekTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(0.01));
        return transfer;
    }

    private Transfer get1SekTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(1.0));
        return transfer;
    }

    private Transfer getGreaterThan1SekTransfer() {
        Transfer transfer = new Transfer();
        transfer.setAmount(ExactCurrencyAmount.inSEK(100.12));
        return transfer;
    }

    private Transfer getTransferWithUnstructuredRemittanceInformation(String reference) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(reference);
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        Transfer transfer = new Transfer();
        transfer.setRemittanceInformation(remittanceInformation);

        return transfer;
    }
}
