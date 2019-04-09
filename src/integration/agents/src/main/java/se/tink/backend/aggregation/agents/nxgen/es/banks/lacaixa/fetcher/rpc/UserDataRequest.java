package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserDataRequest {

    @JsonProperty("datosUsuario")
    private List<String> dataRequests;

    public UserDataRequest(String ... dataRequests) {
        this.dataRequests = Arrays.asList(dataRequests);
    }
}
