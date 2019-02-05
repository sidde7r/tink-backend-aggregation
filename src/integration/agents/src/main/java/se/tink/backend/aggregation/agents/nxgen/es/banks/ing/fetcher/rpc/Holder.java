package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class Holder {

    private Status interventionDegree;
    private String name;
    private String nif;
    private String completeName;
    private String type;
    private String typeCode;

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getCompleteName() {
        return completeName;
    }

    public void setCompleteName(String completeName) {
        this.completeName = completeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Status getInterventionDegree() {
        return interventionDegree;
    }

    public void setInterventionDegree(Status interventionDegree) {
        this.interventionDegree = interventionDegree;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
