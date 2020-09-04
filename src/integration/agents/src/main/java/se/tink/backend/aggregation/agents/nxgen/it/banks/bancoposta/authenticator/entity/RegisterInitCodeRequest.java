package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class RegisterInitCodeRequest extends BaseRequest {
    private final Body body;

    @AllArgsConstructor
    public static class Body {
        @JsonProperty("numeroConto")
        private String accountNumber;
    }

    public RegisterInitCodeRequest(String accountNumber) {
        this.body = new Body(accountNumber);
    }
}
