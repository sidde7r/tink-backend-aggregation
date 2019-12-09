package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaRedirectEntity {
    private String href;

    public String getScaRedirectLink() {
        return href;
    }
}
