package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class ErrorModelEntity {
    @JsonProperty("timestamp")
    private OffsetDateTime timestamp = null;

    @JsonProperty("status")
    private Integer status = null;

    @JsonProperty("error")
    private String error = null;

    @JsonProperty("message")
    private String message = null;

    @JsonProperty("path")
    private String path = null;
}
