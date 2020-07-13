package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class TransactionDetailsBaseEntityTest {

    @Test
    public void getDescritpionShouldProvideRemittenceUnsctructedInfoAtFirstIfAvailable() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.remittanceInformationUnstructured =
                "remittanceInformationUnstructured";
        transactionDetailsBaseEntity.creditorName = "creditorName";
        transactionDetailsBaseEntity.debtorName = "debtorName";

        // when
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description).isEqualTo("remittanceInformationUnstructured");
    }

    @Test
    public void getDescritpionShouldProvideDebtorNameIfRemittanceUnstructuredInfoNotAvailable() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.creditorName = "creditorName";
        transactionDetailsBaseEntity.debtorName = "debtorName";

        // when
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description).isEqualTo("debtorName");
    }

    @Test
    public void
            getDescritpionShouldProvideCreditorNameIfRemittanceUnstructuredInfoAndDebtorNameNotAvailable() {
        // given
        TransactionDetailsBaseEntity transactionDetailsBaseEntity = new BookedTransactionEntity();
        transactionDetailsBaseEntity.creditorName = "creditorName";

        // when
        String description = transactionDetailsBaseEntity.getDescription();

        // then
        assertThat(description).isEqualTo("creditorName");
    }
}
