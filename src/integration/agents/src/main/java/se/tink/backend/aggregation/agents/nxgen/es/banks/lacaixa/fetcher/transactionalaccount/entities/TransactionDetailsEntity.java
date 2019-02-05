package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetailsEntity {
    @JsonProperty("literal")
    private String type;
    @JsonProperty("valor")
    private List<String> value;

    public String getType() {
        return type;
    }

    public List<String> getValue() {
        return value;
    }
}
