package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.apiclient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BpceErrorResponse {
    @JsonIgnore private static final String NOT_IMPLEMENTED = "NIMP";

    private String message;
    private String path;

    public boolean isNotImplemented() {
        return NOT_IMPLEMENTED.equals(message);
    }
}
