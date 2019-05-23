package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AvatarEntity {
    private String uuid;

    @JsonProperty("anchor_uuid")
    private String anchorUuid;

    private List<ImageEntity> image;
}
