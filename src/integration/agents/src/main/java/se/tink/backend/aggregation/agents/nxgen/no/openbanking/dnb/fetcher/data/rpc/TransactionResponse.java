package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc;

import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionResponse {
    @Getter private TransactionEntity transactions;
    private TransactionLinksEntity links;

    public Optional<String> getNextKey() {
        return Optional.ofNullable(links).map(TransactionLinksEntity::getNext);
    }
}
