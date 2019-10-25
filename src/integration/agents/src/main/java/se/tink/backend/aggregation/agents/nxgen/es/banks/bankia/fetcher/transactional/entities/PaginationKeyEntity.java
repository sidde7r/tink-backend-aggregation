package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaginationKeyEntity {
    @JsonProperty("cantidadUltimosMovimientos")
    private int numberOfLastTransactions;

    @JsonProperty("fechaOperacionContinuacion")
    private DateEntity continuationDate;

    @JsonProperty("numeroSecuencialContinuacion")
    private int continuationSequenceNumber;
}
