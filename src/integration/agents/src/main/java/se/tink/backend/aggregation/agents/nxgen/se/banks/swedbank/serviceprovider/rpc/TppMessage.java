package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TppMessage {
    private final String code;
    private final String text;
    private final String category;

    @JsonCreator
    public TppMessage(
            @JsonProperty("code") String code,
            @JsonProperty("text") String text,
            @JsonProperty("category") String category) {
        this.code = code;
        this.text = text;
        this.category = category;
    }
}
