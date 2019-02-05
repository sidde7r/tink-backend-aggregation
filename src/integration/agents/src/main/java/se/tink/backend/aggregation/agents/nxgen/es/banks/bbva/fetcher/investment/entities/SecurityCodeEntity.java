package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityCodeEntity {
    private String ricCode;

    public static SecurityCodeEntity create(String ricCode) {
        SecurityCodeEntity securityCodeEntity = new SecurityCodeEntity();
        securityCodeEntity.ricCode = ricCode;

        return securityCodeEntity;
    }
}
