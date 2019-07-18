package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.entity;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit.authenticator.entity.ConsentPayloadEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BuddybankConsentAccessEntity implements ConsentAccessEntity {

    private List<ConsentPayloadEntity> accounts = Collections.emptyList();
    private String allPsd2;

    public BuddybankConsentAccessEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }
}
