package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.ing.fetcher.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.TransactionEntity;

public class IngNLTransactionEntity extends TransactionEntity {

    @Override
    public String toTinkDescription() {
        return getRemittanceInformationUnstructured();
    }
}
