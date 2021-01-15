package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.HolderEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListHoldersResponse {
    @JsonProperty("titulares")
    private List<HolderEntity> holders;

    @JsonProperty("masDatos")
    private boolean moreData;

    public List<HolderEntity> getHolders() {
        return holders;
    }
}
