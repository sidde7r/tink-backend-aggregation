package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class TransactionDetailsBaseEntityTest {
    private static final String REMITTANCE_INFOMATION_UNSTRUCTURED =
            "remittanceInformationUnstructured";
    private static final String CREDITOR_NAME = "creditorName";
    private static final String DEBTOR_NAME = "debtorName";

    @Test
    public void getDescritpionShouldProvideRemittenceUnsctructedInfoAtFirstIfAvailable() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.remittanceInformationUnstructured =
                REMITTANCE_INFOMATION_UNSTRUCTURED;
        transactionDetailsBaseEntity.creditorName = CREDITOR_NAME;
        transactionDetailsBaseEntity.debtorName = DEBTOR_NAME;

        // when
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description).isEqualTo(REMITTANCE_INFOMATION_UNSTRUCTURED);
    }

    @Test
    public void getDescritpionShouldProvideDebtorNameIfRemittanceUnstructuredInfoNotAvailable() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.creditorName = CREDITOR_NAME;
        transactionDetailsBaseEntity.debtorName = DEBTOR_NAME;

        // when
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description).isEqualTo(DEBTOR_NAME);
    }

    @Test
    public void
            getDescritpionShouldProvideCreditorNameIfRemittanceUnstructuredInfoAndDebtorNameNotAvailable() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.creditorName = CREDITOR_NAME;

        // when
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description).isEqualTo(CREDITOR_NAME);
    }
}
