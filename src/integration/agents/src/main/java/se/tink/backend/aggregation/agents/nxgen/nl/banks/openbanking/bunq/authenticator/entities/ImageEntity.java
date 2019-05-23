package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ImageEntity {
    @JsonProperty("attachment_public_uuid")
    private String attachmentPublicUuid;

    @JsonProperty("content_type")
    private String contentType;

    private int height;
    private int width;
}
