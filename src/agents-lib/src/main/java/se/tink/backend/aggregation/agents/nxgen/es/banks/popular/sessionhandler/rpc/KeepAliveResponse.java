package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.sessionhandler.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeepAliveResponse extends BancoPopularResponse {
    private boolean Ok;

    public boolean isOk() {
        return Ok;
    }

    @JsonProperty("Ok")
    public void setOk(boolean ok) {
        Ok = ok;
    }
}
