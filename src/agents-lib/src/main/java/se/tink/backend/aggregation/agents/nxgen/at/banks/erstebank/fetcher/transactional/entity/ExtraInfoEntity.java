package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExtraInfoEntity {
    private String request;

    public String getRequest() {
        return request;
    }
}
