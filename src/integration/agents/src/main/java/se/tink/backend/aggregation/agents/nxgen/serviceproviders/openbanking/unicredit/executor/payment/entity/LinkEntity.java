package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {

    private String href;

    public String getHref() {
        return href;
    }
}
