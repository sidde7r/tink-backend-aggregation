package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ParticipantAccountEntity {
    private String name;
    private String association;
}
