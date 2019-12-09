package se.tink.backend.aggregation.agents.nxgen.de.openbanking.ahoisandbox.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AdditionalInformationEntity {

    public String type;
    public String id;
    public String name;
    public String originId;
    public String origin;
    public Integer contractorId;
}
