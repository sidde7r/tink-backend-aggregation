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
import se.tink.libraries.account.AccountIdentifier;
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
        when(creditor.getAccountIdentifierType()).thenReturn(AccountIdentifier.Type.IBAN);
        when(debtor.getAccountIdentifierType()).thenReturn(AccountIdentifier.Type.IBAN);
        when(creditor.getAccountNumber()).thenReturn("");
        when(debtor.getAccountNumber()).thenReturn("");
        when(payment.isSepa()).thenReturn(true);
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithIbanFromPaymentCreditor() {
        when(creditor.getAccountNumber()).thenReturn(DUMMY_IBAN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromCreditor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isEqualTo(DUMMY_IBAN);
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isNull();
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithIbanFromPaymentDebtor() {
        when(debtor.getAccountNumber()).thenReturn(DUMMY_IBAN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromDebtor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isEqualTo(DUMMY_IBAN);
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isNull();
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithPanFromPaymentCreditor() {
        when(creditor.getAccountIdentifierType())
                .thenReturn(AccountIdentifier.Type.PAYMENT_CARD_NUMBER);
        when(creditor.getAccountNumber()).thenReturn(DUMMY_PAN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromCreditor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isEqualTo(DUMMY_PAN);
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithPanFromPaymentDebtor() {
        when(debtor.getAccountIdentifierType())
                .thenReturn(AccountIdentifier.Type.PAYMENT_CARD_NUMBER);
        when(debtor.getAccountNumber()).thenReturn(DUMMY_PAN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromDebtor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isEqualTo(DUMMY_PAN);
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithMsisdnFromPaymentCreditor() {
        when(creditor.getAccountIdentifierType())
                .thenReturn(AccountIdentifier.Type.PAYM_PHONE_NUMBER);
        when(creditor.getAccountNumber()).thenReturn(DUMMY_MSISDN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromCreditor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isEqualTo(DUMMY_MSISDN);
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isNull();
    }

    @Test
    public void shouldReturnSibsAccountReferenceWithMisidnFromPaymentDebtor() {
        when(debtor.getAccountIdentifierType())
                .thenReturn(AccountIdentifier.Type.PAYM_PHONE_NUMBER);
        when(debtor.getAccountNumber()).thenReturn(DUMMY_MSISDN);

        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.fromDebtor(payment);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isNull();
        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isEqualTo(DUMMY_MSISDN);
        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isNull();
    }

    private Object[] unsupportedTypes() {
        AccountIdentifier.Type[] types = AccountIdentifier.Type.values();
        return ArrayUtils.removeElements(
                types,
                AccountIdentifier.Type.PAYM_PHONE_NUMBER,
                AccountIdentifier.Type.IBAN,
                AccountIdentifier.Type.PAYMENT_CARD_NUMBER);
    }

    @Test(expected = RuntimeException.class)
    @Parameters(method = "unsupportedTypes")
    public void shouldThrowExceptionWhenUnsupportedExceptionIsThrown(AccountIdentifier.Type type) {
        SibsAccountReferenceEntity.of(() -> type, () -> "");
    }

    @Test
    public void shouldReturnSibsAccountReferenceEntityFromIban() {
        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.of(() -> AccountIdentifier.Type.IBAN, () -> DUMMY_IBAN);

        Assertions.assertThat(sibsAccountReferenceEntity.getIban()).isEqualTo(DUMMY_IBAN);
    }

    @Test
    public void shouldReturnSibsAccountReferenceEntityFromPan() {
        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.of(
                        () -> AccountIdentifier.Type.PAYMENT_CARD_NUMBER, () -> DUMMY_PAN);

        Assertions.assertThat(sibsAccountReferenceEntity.getPan()).isEqualTo(DUMMY_PAN);
    }

    @Test
    public void shouldReturnSibsAccountReferenceEntityFromMsisdn() {
        SibsAccountReferenceEntity sibsAccountReferenceEntity =
                SibsAccountReferenceEntity.of(
                        () -> AccountIdentifier.Type.PAYM_PHONE_NUMBER, () -> DUMMY_MSISDN);

        Assertions.assertThat(sibsAccountReferenceEntity.getMsisdn()).isEqualTo(DUMMY_MSISDN);
    }
}
