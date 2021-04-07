package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.transactionalaccount;

import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaNoTransactionEntity extends TransactionEntity {

    @Override
    public String getDescription() {
        return Stream.of(getCounterpartyName(), getNarrative(), getTypeDescription())
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse("");
    }

    @Override
    public String getNarrative() {
        String narrative = super.getNarrative();
        return StringUtils.trim(
                StringUtils.substring(narrative, StringUtils.indexOf(narrative, ",") + 1));
    }
}
