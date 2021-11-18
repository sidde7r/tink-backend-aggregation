package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdEntity {
    private String id;
    private String type;
    private String schemeNameCode;
}
