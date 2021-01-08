package se.tink.backend.aggregation.agents.utils.berlingroup.consent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentResponse {

    private String consentStatus;

    @Getter private String consentId;

    @JsonProperty("_links")
    @Getter
    private LinksEntity links;

    @Getter @Setter private LocalDate validUntil;

    private String psuMessage;
}
