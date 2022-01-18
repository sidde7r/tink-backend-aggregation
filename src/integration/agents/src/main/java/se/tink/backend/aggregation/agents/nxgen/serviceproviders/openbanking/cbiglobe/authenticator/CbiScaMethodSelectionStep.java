package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.SelectAuthorizationMethodRequest;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public abstract class CbiScaMethodSelectionStep {

    private final CbiGlobeAuthApiClient authApiClient;
    private final URL baseUrlForOperation;

    public CbiConsentResponse pickScaMethod(CbiScaMethodSelectable selectable) {
        ScaMethodEntity scaMethodEntity = selectMethod(selectable.getScaMethods());
        return authApiClient.selectScaMethod(
                baseUrlForOperation.concat(selectable.getSelectAuthenticationMethodLink()),
                new SelectAuthorizationMethodRequest(scaMethodEntity.getAuthenticationMethodId()));
    }

    protected abstract ScaMethodEntity selectMethod(List<ScaMethodEntity> scaMethods);
}
