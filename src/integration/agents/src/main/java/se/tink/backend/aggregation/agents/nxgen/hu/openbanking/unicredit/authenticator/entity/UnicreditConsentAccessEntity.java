package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnicreditConsentAccessEntity implements ConsentAccessEntity {

    private List<ConsentPayloadEntity> accounts;
    private List<ConsentPayloadEntity> balances;
    private List<ConsentPayloadEntity> transactions;

    public UnicreditConsentAccessEntity(List<ConsentPayloadEntity> ibans) {
        this.accounts = ibans;
        this.balances = ibans;
        this.transactions = ibans;
    }
}
