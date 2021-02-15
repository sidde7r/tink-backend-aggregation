package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SupplyBankIdRequest {
    private PsuDataEntity psuData;

    public SupplyBankIdRequest(String bankId) {
        psuData = new PsuDataEntity(bankId);
    }
}
