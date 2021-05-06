package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@EqualsAndHashCode
public class MetroProcessState {

    private List<Integer> seedPosition;
    private String sessionId;
    private String challenge;
    private String assertionId;
    private String token;

    public MetroProcessState setSeedPosition(List<Integer> seedPosition) {
        this.seedPosition = seedPosition;
        return this;
    }

    public MetroProcessState setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public MetroProcessState setChallenge(String challenge) {
        this.challenge = challenge;
        return this;
    }

    public MetroProcessState setAssertionId(String assertionId) {
        this.assertionId = assertionId;
        return this;
    }

    public MetroProcessState setToken(String token) {
        this.token = token;
        return this;
    }
}
