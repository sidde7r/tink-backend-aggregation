package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.transactionalaccount;

import com.google.common.base.Strings;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaNoTransactionEntity extends TransactionEntity {

    @Override
    public String getDescription() {
        return Stream.of(getCounterPartyName(), getTypeDescription(), getNarrative())
                .filter(x -> !Strings.isNullOrEmpty(x))
                .findFirst()
                .orElse("");
    }
}
