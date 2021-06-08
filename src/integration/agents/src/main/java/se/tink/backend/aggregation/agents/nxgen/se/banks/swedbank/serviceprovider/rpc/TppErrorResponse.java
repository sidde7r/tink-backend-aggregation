package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TppErrorResponse {
    private static final String UNKNOWN_ERROR = "Unknown error";

    private final List<TppMessage> tppMessages;

    @JsonCreator
    public TppErrorResponse(@JsonProperty("tppMessages") List<TppMessage> tppMessages) {
        this.tppMessages = tppMessages;
    }

    @JsonIgnore
    public String getErrorMessage() {
        return getTppMessages().stream().findFirst().map(TppMessage::getText).orElse(UNKNOWN_ERROR);
    }
}
