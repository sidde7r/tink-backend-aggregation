package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea.fetcher.transactionalaccount;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaFiTransactionEntity extends TransactionEntity {

    @Override
    public String getDescription() {
        if (!Strings.isNullOrEmpty(getCounterpartyName())) {
            return getCounterpartyName();
        }
        return (!Strings.isNullOrEmpty(getNarrative())) ? getNarrative() : getTypeDescription();
    }
}
