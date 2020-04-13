package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SessionEntity {
    private String msgKey;
    private String message;
}
