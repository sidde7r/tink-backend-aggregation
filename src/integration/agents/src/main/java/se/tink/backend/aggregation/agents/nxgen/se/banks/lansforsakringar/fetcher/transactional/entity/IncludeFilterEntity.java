package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IncludeFilterEntity {
    private String transactionStatus;

    @JsonIgnore
    public IncludeFilterEntity(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
