package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

@RequiredArgsConstructor
public class CbiConsentCreationStep {

    private final CbiGlobeAuthApiClient authApiClient;
    private final LocalDateTimeSource localDateTimeSource;
    private final CbiStorage storage;

    public CbiConsentResponse createConsentAndSaveId() {
        ConsentRequest consentRequest =
                ConsentRequest.buildTypicalRecurring(
                        AccessEntity.builder()
                                .allPsd2(AccessType.ALL_ACCOUNTS_WITH_OWNER_NAME)
                                .build(),
                        localDateTimeSource);

        CbiConsentResponse consentResponse = authApiClient.createConsent(consentRequest);

        storage.saveConsentId(consentResponse.getConsentId());
        return consentResponse;
    }
}
