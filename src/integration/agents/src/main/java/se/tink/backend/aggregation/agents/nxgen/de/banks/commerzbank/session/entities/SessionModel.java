package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SessionModel {

    private Object error;

    private SessionResultEntity result;
}
