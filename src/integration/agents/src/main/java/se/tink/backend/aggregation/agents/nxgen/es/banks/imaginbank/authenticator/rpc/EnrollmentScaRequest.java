package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EnrollmentScaRequest {

    @JsonProperty("codigo")
    private List<String> codes;

    public EnrollmentScaRequest(String code) {
        codes = new ArrayList<>();
        codes.add(code);
    }
}
