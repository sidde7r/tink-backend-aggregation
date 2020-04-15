package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_EMPTY)
public class InitializationEntity {

    private String version;
    private Integer status;

    public String getVersion() {
        return version;
    }

    public Integer getStatus() {
        return status;
    }
}
