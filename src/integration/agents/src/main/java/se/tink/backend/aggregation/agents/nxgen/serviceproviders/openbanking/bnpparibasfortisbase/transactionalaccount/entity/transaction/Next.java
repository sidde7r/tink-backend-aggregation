package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Next {

    private String href;

    public String getHref() {
        return href;
    }
}
