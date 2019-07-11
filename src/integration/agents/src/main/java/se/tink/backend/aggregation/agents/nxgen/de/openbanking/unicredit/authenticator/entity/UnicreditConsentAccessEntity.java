package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.entity;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentPayloadEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity.ConsentAccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnicreditConsentAccessEntity implements ConsentAccessEntity {

    private List<ConsentPayloadEntity> accounts = Collections.emptyList();
    private String allPsd2;

    public UnicreditConsentAccessEntity(String allPsd2) {
        this.allPsd2 = allPsd2;
    }
}
