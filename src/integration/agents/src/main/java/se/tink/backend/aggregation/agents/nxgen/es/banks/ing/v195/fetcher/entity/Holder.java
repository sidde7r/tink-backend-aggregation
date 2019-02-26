package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class Holder {

    private InterventionDegree interventionDegree;
    private String name;
    private String nif;
    private String completeName;
    private String type;
    private String typeCode;

    public String getNif() {
        return nif;
    }

    public String getCompleteName() {
        return completeName;
    }

    public String getType() {
        return type;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public InterventionDegree getInterventionDegree() {
        return interventionDegree;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public String getAnyName() {
        if (completeName != null) {
            return completeName;
        }
        return name;
    }
}
