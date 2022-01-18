package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiCredentialsAuthenticatable;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiRedirectAuthorizable;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiScaMethodSelectable;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.CbiConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entity.PsuCredentialsDefinition;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CbiConsentResponse
        implements CbiCredentialsAuthenticatable, CbiScaMethodSelectable, CbiRedirectAuthorizable {
    private String consentId;
    private CbiConsentStatus consentStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    // Special way of transferring what fields CBI auth requires in embedded flow
    private PsuCredentialsDefinition psuCredentials;

    // Used by banks with embedded flows. This is different than most BerlinGroup banks, which have
    // this data on separate entity
    private List<ScaMethodEntity> scaMethods;

    @Override
    public String getUpdatePsuAuthenticationLink() {
        return links.getUpdatePsuAuthentication();
    }

    @Override
    public String getSelectAuthenticationMethodLink() {
        return links.getSelectAuthenticationMethod();
    }

    @Override
    public String getScaRedirectLink() {
        return links.getScaRedirect();
    }
}
