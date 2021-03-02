package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@RunWith(JUnitParamsRunner.class)
public class TransactionEntityTest {

    private static final String REMITTANCE_INFOMATION_UNSTRUCTURED =
            "remittanceInformationUnstructured";
    private static final String UNSPECIFIED_CREDITOR_NAME = "creditorName";
    private static final String SPECIFIED_CREDITOR_NAME = "klarna";
    private static final String DEBTOR_NAME = "debtorName";

    @Test
    public void getDescriptionShouldProvideDebtorNameIfIncome() {
        // given
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.remittanceInformationUnstructured = REMITTANCE_INFOMATION_UNSTRUCTURED;
        transactionEntity.creditorName = SPECIFIED_CREDITOR_NAME;
        transactionEntity.debtorName = DEBTOR_NAME;

        AmountEntity amountEntity = new AmountEntity();
        amountEntity.amount = BigDecimal.valueOf(10.00);
        amountEntity.currency = "EUR";
        transactionEntity.transactionAmount = amountEntity;

        // when
        Transaction transaction = transactionEntity.toBookedTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEqualTo(DEBTOR_NAME);
    }

    @Test
    public void getDescriptionShouldProvideRemittanceUnstructuredInfoIfKlarnaDebtorNamePurchase() {
        // given
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.creditorName = SPECIFIED_CREDITOR_NAME;
        transactionEntity.debtorName = DEBTOR_NAME;
        transactionEntity.remittanceInformationUnstructured = REMITTANCE_INFOMATION_UNSTRUCTURED;

        AmountEntity amountEntity = new AmountEntity();
        amountEntity.amount = BigDecimal.valueOf(-5.00);
        amountEntity.currency = "EUR";
        transactionEntity.transactionAmount = amountEntity;

        // when
        Transaction transaction = transactionEntity.toBookedTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEqualTo(REMITTANCE_INFOMATION_UNSTRUCTURED);
    }

    @Test
    public void getDescriptionShouldProvideCreditorNameIfDebtorNameNotSpecifiedAndPurchase() {
        // given
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.creditorName = UNSPECIFIED_CREDITOR_NAME;
        transactionEntity.debtorName = DEBTOR_NAME;
        transactionEntity.remittanceInformationUnstructured = REMITTANCE_INFOMATION_UNSTRUCTURED;

        AmountEntity amountEntity = new AmountEntity();
        amountEntity.amount = BigDecimal.valueOf(-5.00);
        amountEntity.currency = "EUR";
        transactionEntity.transactionAmount = amountEntity;

        // when
        Transaction transaction = transactionEntity.toBookedTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEqualTo(UNSPECIFIED_CREDITOR_NAME);
    }

    @Test
    public void getDescriptionShouldReturnCreditorNameIfPurchaseSpecifiedButEmptyRemittanceInfo() {
        // given
        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.creditorName = SPECIFIED_CREDITOR_NAME;
        transactionEntity.remittanceInformationUnstructured = "";

        AmountEntity amountEntity = new AmountEntity();
        amountEntity.amount = BigDecimal.valueOf(-5.00);
        amountEntity.currency = "EUR";
        transactionEntity.transactionAmount = amountEntity;

        // when
        Transaction transaction = transactionEntity.toBookedTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isEqualTo(SPECIFIED_CREDITOR_NAME);
    }
}
