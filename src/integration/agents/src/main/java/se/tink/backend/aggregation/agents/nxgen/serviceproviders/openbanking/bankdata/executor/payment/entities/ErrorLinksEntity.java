package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.ScaOAuthEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.ScaStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.SelfEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.StartAuthorisationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities.StatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorLinksEntity {

    private ScaOAuthEntity scaOAuth;
    private StartAuthorisationEntity startAuthorisation;
    private SelfEntity self;
    private StatusEntity status;
    private ScaStatusEntity scaStatus;
    private FirstEntity first;
    private NextEntity next;
    private PreviousEntity previous;
    private LastEntity last;
}
