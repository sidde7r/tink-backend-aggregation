package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OccupationalPensionsEntity {
    private String name;
    private String agreementId;
    private double value;
    private boolean hasDetails;
    private List<FootNotesEntity> footNotes;
}
