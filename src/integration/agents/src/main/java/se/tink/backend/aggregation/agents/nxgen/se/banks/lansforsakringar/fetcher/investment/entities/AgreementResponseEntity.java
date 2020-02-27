package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementResponseEntity {
    private LifeInsuranceAgreementEntity lifeInsuranceAgreement;

    public LifeInsuranceAgreementEntity getLifeInsuranceAgreement() {
        return lifeInsuranceAgreement;
    }
}
