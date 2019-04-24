package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseCodeEntity {

    private String code;
    private List<LinkEntity> links;
    private String state;

    public String getCode() {
        return code;
    }
}
