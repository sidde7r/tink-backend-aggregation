package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class LinkDetailsEntity {

    private final String href;

    @JsonCreator
    public LinkDetailsEntity(@JsonProperty("href") String href) {
        this.href = href;
    }
}
