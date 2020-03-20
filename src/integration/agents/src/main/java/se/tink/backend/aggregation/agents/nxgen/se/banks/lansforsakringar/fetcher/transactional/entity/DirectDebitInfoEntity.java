package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DirectDebitInfoEntity {
    private String bankGiroNumber;
    private String creditorShortName;
}
