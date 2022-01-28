package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SmsEntity extends ScaEntity {

    @JsonProperty("movil")
    private String mobile;

    public String getMobile() {
        return mobile;
    }
}
