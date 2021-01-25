package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ParticipantsDataEntity {

    private List<ParticipantAccountEntity> participants;
    private String hasMoreData;
}
