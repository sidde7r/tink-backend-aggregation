package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorCodeEntity {
    @JsonProperty("codErrh")
    private Object codErrh;

    @JsonProperty("indErr")
    private String indErr;

    @JsonProperty("descErr")
    private Object descErr;

    public Object getCodErrh() {
        return codErrh;
    }

    public String getIndErr() {
        return indErr;
    }

    public Object getDescErr() {
        return descErr;
    }
}
