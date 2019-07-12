package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.SelfEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.StartAuthorisationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.StatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private StartAuthorisationEntity startAuthorisation;
    private SelfEntity self;
    private StatusEntity status;

    public StartAuthorisationEntity getStartAuthorisation() {
        return startAuthorisation;
    }

    public String getDetailsLink() {
        return self.getHref();
    }
}
