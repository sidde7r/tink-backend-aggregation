package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NationalityEntity {
    private String id;
    private String code;
    private String name;

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
