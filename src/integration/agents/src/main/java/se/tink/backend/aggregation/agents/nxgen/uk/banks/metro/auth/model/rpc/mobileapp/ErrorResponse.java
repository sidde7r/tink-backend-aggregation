package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.control.Option;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Setter
public class ErrorResponse {

    private String code;

    private String message;

    @JsonIgnore
    public boolean hasCode(String code) {
        return !Option.of(this.code).filter(code::equals).isEmpty();
    }
}
