package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class RegisterInitBody {
    private String initCodeChallenge;
    private String appName;
}
