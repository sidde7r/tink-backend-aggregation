package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.utils.berlingroup.AmountEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(JUnitParamsRunner.class)
public class TransactionDetailsBaseEntityTest {
    private static final String REMITTANCE_INFOMATION_UNSTRUCTURED =
            "remittanceInformationUnstructured";
    private static final String UNSPECIFIED_CREDITOR_NAME = "creditorName";
    private static final String SPECIFIED_CREDITOR_NAME = "klarna";
    private static final String DEBTOR_NAME = "debtorName";
    private static final ExactCurrencyAmount INCOME_AMOUNT = ExactCurrencyAmount.of(10.00, "EUR");
    private static final ExactCurrencyAmount PURCHASE_AMOUNT = ExactCurrencyAmount.of(-5.00, "EUR");

    @Test
    public void getDescritpionShouldProvideDebtorNameIfIncome() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.remittanceInformationUnstructured =
                REMITTANCE_INFOMATION_UNSTRUCTURED;
        transactionDetailsBaseEntity.creditorName = SPECIFIED_CREDITOR_NAME;
        transactionDetailsBaseEntity.debtorName = DEBTOR_NAME;
        AmountEntity amountEntityMock = Mockito.mock(AmountEntity.class);
        transactionDetailsBaseEntity.transactionAmount = amountEntityMock;

        // when
        when(amountEntityMock.toTinkAmount()).thenReturn(INCOME_AMOUNT);
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description).isEqualTo(DEBTOR_NAME);
    }

    @Test
    public void getDescritpionShouldProvideRemittanceUnstructuredInfoIfKlarnaDebtorNamePurchase() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.creditorName = SPECIFIED_CREDITOR_NAME;
        transactionDetailsBaseEntity.debtorName = DEBTOR_NAME;
        transactionDetailsBaseEntity.remittanceInformationUnstructured =
                REMITTANCE_INFOMATION_UNSTRUCTURED;
        AmountEntity amountEntityMock = Mockito.mock(AmountEntity.class);
        transactionDetailsBaseEntity.transactionAmount = amountEntityMock;

        // when
        when(amountEntityMock.toTinkAmount()).thenReturn(PURCHASE_AMOUNT);
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description).isEqualTo(REMITTANCE_INFOMATION_UNSTRUCTURED);
    }

    @Test
    public void getDescritpionShouldProvideCreditorNameIfDebtorNameNotSpecifiedAndPurchase() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.creditorName = UNSPECIFIED_CREDITOR_NAME;
        transactionDetailsBaseEntity.debtorName = DEBTOR_NAME;
        transactionDetailsBaseEntity.remittanceInformationUnstructured =
                REMITTANCE_INFOMATION_UNSTRUCTURED;
        AmountEntity amountEntityMock = Mockito.mock(AmountEntity.class);
        transactionDetailsBaseEntity.transactionAmount = amountEntityMock;

        // when
        when(amountEntityMock.toTinkAmount()).thenReturn(PURCHASE_AMOUNT);
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description).isEqualTo(UNSPECIFIED_CREDITOR_NAME);
    }

    @Test
    public void getDescriptionShouldReturnCreditorNameIfPurchaseSpecifiedButEmptyRemittanceInfo() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.creditorName = SPECIFIED_CREDITOR_NAME;
        transactionDetailsBaseEntity.remittanceInformationUnstructured = "";
        AmountEntity amountEntityMock = Mockito.mock(AmountEntity.class);
        transactionDetailsBaseEntity.transactionAmount = amountEntityMock;

        // when
        when(amountEntityMock.toTinkAmount()).thenReturn(PURCHASE_AMOUNT);
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description.trim()).isEqualTo(SPECIFIED_CREDITOR_NAME);
    }
}
