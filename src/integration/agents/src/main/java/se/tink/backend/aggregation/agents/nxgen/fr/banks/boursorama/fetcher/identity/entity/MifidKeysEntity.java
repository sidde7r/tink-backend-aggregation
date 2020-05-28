package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MifidKeysEntity {
    private String key;
    private int priority;
}
