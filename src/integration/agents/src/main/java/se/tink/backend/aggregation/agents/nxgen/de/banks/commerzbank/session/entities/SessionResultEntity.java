package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SessionResultEntity {

    private List<SessionEntity> items;
}
