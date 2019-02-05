package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.openbanking.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.NordeaTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.entities.TransactionEntity;

public class NordeaDKTransactionParser extends NordeaTransactionParser {

    @Override
    protected String getDescription(TransactionEntity transactionEntity) {
        if (hasContent(transactionEntity.getTypeDescription())) {
            return transactionEntity.getTypeDescription();
        }

        return transactionEntity.getNarrative();
    }
}
