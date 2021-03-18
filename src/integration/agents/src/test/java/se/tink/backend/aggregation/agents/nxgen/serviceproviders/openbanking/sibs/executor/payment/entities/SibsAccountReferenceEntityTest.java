package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@RunWith(JUnitParamsRunner.class)
public class SibsAccountReferenceEntityTest {

    private final Creditor creditor = Mockito.mock(Creditor.class);
    private final Debtor debtor = Mockito.mock(Debtor.class);
    private final Payment payment = Mockito.mock(Payment.class);
    private static final String DUMMY_IBAN = "dummyIban";
    private static final String DUMMY_PAN = "dummyPan";
    private static final String DUMMY_MSISDN = "dummyMsisdn";

    @Before
    public void init() {
        when(payment.getCreditor()).thenReturn(creditor);
        when(payment.getDebtor()).thenReturn(debtor);
        when(payment.getExactCurrencyAmountFromField())
                .thenReturn(new ExactCurrencyAmount(new BigDecimal("1.0"), "EUR"));
        when(creditor.getAccountIdentifierType()).thenReturn(AccountIdentifierType.IBAN);
        when(debtor.getAccountIdentifierType()).thenReturn(AccountIdentifierType.IBAN);
        when(creditor.getAccountNumber()).thenReturn("");
        when(debtor.getAccountNumber()).thenReturn("");
        when(payment.isSepa()).thenReturn(true);
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithIbanFromPaymentCreditor()
            throws PaymentValidationException {
        when(creditor.getAccountNumber()).thenReturn(DUMMY_IBAN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromCreditor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isEqualTo(DUMMY_IBAN);
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isNull();
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithIbanFromPaymentDebtor()
            throws PaymentValidationException {
        when(debtor.getAccountNumber()).thenReturn(DUMMY_IBAN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromDebtor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isEqualTo(DUMMY_IBAN);
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isNull();
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithPanFromPaymentCreditor()
            throws PaymentValidationException {
        when(creditor.getAccountIdentifierType())
                .thenReturn(AccountIdentifierType.PAYMENT_CARD_NUMBER);
        when(creditor.getAccountNumber()).thenReturn(DUMMY_PAN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromCreditor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isEqualTo(DUMMY_PAN);
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithPanFromPaymentDebtor()
            throws PaymentValidationException {
        when(debtor.getAccountIdentifierType())
                .thenReturn(AccountIdentifierType.PAYMENT_CARD_NUMBER);
        when(debtor.getAccountNumber()).thenReturn(DUMMY_PAN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromDebtor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isEqualTo(DUMMY_PAN);
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithMsisdnFromPaymentCreditor()
            throws PaymentValidationException {
        when(creditor.getAccountIdentifierType())
                .thenReturn(AccountIdentifierType.PAYM_PHONE_NUMBER);
        when(creditor.getAccountNumber()).thenReturn(DUMMY_MSISDN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromCreditor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isEqualTo(DUMMY_MSISDN);
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isNull();
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithMisidnFromPaymentDebtor()
            throws PaymentValidationException {
        when(debtor.getAccountIdentifierType()).thenReturn(AccountIdentifierType.PAYM_PHONE_NUMBER);
        when(debtor.getAccountNumber()).thenReturn(DUMMY_MSISDN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromDebtor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isEqualTo(DUMMY_MSISDN);
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isNull();
    }

    private Object[] unsupportedTypes() {
        AccountIdentifierType[] types = AccountIdentifierType.values();
        return ArrayUtils.removeElements(
                types,
                AccountIdentifierType.PAYM_PHONE_NUMBER,
                AccountIdentifierType.IBAN,
                AccountIdentifierType.PAYMENT_CARD_NUMBER);
    }

    @Test(expected = PaymentValidationException.class)
    @Parameters(method = "unsupportedTypes")
    public void shouldThrowExceptionWhenUnsupportedExceptionIsThrown(AccountIdentifierType type)
            throws PaymentValidationException {
        SibsAccountReferenceEntity.of(() -> type, () -> "");
    }

    @Test
    public void shouldReturnSibsAccountReferenceEntityFromIban() throws PaymentValidationException {
        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.of(() -> AccountIdentifierType.IBAN, () -> DUMMY_IBAN);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isEqualTo(DUMMY_IBAN);
    }

    @Test
    public void shouldReturnSibsAccountReferenceEntityFromPan() throws PaymentValidationException {
        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.of(
                        () -> AccountIdentifierType.PAYMENT_CARD_NUMBER, () -> DUMMY_PAN);

        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isEqualTo(DUMMY_PAN);
    }

    @Test
    public void shouldReturnSibsAccountReferenceEntityFromMsisdn()
            throws PaymentValidationException {
        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.of(
                        () -> AccountIdentifierType.PAYM_PHONE_NUMBER, () -> DUMMY_MSISDN);

        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isEqualTo(DUMMY_MSISDN);
    }
}
