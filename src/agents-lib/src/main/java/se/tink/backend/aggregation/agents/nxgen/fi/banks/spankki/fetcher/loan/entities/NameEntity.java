package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NameEntity {
    private String sv;
    private String fi;

    public String getSv() {
        return sv;
    }

    public String getFi() {
        return fi;
    }
}
