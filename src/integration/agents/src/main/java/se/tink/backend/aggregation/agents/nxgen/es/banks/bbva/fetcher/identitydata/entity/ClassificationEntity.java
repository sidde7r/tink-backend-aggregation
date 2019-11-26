package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClassificationEntity {
    private List<SegmentsEntity> segments;
    private boolean isVIP;
    private TypeEntity endOfRelationship;
}
