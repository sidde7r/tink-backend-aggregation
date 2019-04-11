package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetailsResponse {
    @JsonProperty("infoAdicional")
    private List<TransactionDetailsEntity> additionalInfo;

    public Optional<String> getDetailedDescription(String key) {

        return Optional.ofNullable(additionalInfo).orElseGet(Collections::emptyList).stream()
                .filter(details -> key.equalsIgnoreCase(details.getType()))
                .map(TransactionDetailsEntity::getValue)
                .flatMap(List::stream)
                .findFirst();
    }
}
