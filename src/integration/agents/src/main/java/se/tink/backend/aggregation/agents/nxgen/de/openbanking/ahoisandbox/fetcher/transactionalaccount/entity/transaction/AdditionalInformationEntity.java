package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AdditionalInformationEntity {

    private String type;
    private String id;
    private String name;
    private String originId;
    private String origin;
    private Integer contractorId;
}
