package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaAuthorizationLinksEntity {

    private Href scaStatus;
    private Href authoriseTransaction;
    private Href selectAuthenticationMethod;

    public Href getScaStatus() {
        return Preconditions.checkNotNull(scaStatus);
    }

    public Href getAuthoriseTransaction() {
        return Preconditions.checkNotNull(authoriseTransaction);
    }
}
