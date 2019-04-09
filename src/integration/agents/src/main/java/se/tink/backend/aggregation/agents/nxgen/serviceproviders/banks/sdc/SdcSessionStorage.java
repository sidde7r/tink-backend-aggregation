package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.SdcAgreementServiceConfigurationResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SdcSessionStorage {

    private final SessionStorage sessionStorage;

    public SdcSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public SessionStorageAgreements getAgreements() {
        return sessionStorage
                .get(SdcConstants.Session.AGREEMENTS, SessionStorageAgreements.class)
                .orElseThrow(() -> new IllegalStateException("No agreements found"));
    }

    // add agreements to session storage for use when fetching accounts
    public void setAgreements(SessionStorageAgreements agreements) {
        Preconditions.checkNotNull(agreements);

        sessionStorage.put(SdcConstants.Session.AGREEMENTS, agreements);
    }

    public Optional<SdcAgreementServiceConfigurationResponse> getCurrentServiceConfiguration() {
        return sessionStorage.get(
                SdcConstants.Session.CURRENT_AGREEMENT,
                SdcAgreementServiceConfigurationResponse.class);
    }

    public void putCurrentServiceConfiguration(
            SdcAgreementServiceConfigurationResponse agreementServiceConfiguration) {
        Preconditions.checkNotNull(agreementServiceConfiguration);

        sessionStorage.put(SdcConstants.Session.CURRENT_AGREEMENT, agreementServiceConfiguration);
    }
}
