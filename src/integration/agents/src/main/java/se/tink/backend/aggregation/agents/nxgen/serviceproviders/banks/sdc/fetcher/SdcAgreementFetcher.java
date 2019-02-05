package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcServiceConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.SdcAgreementServiceConfigurationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SelectAgreementRequest;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public abstract class SdcAgreementFetcher {
    private static final Logger log = LoggerFactory.getLogger(SdcAgreementFetcher.class);

    protected final SdcApiClient bankClient;
    private final SdcSessionStorage sessionStorage;

    public SdcAgreementFetcher(SdcApiClient bankClient, SdcSessionStorage sessionStorage) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    protected SessionStorageAgreements getAgreements() {
        return sessionStorage.getAgreements();
    }

    protected void setAgreements(SessionStorageAgreements agreements) {
        sessionStorage.setAgreements(agreements);
    }

    protected Optional<SdcServiceConfigurationEntity> selectAgreement(SessionStorageAgreement currentAgreement,
            SessionStorageAgreements agreements) {
        Optional<SdcServiceConfigurationEntity> configurationEntity = sessionStorage
                .getCurrentServiceConfiguration()
                .map(SdcAgreementServiceConfigurationResponse::getServiceConfiguration);

        if (configurationEntity.isPresent() && agreements.size() == 1) {
            return configurationEntity;
        }

        try {
            SdcAgreementServiceConfigurationResponse serviceConfiguration =
                    updateServerWithAgreementInUse(currentAgreement);
            sessionStorage.putCurrentServiceConfiguration(serviceConfiguration);
            return Optional.of(serviceConfiguration.getServiceConfiguration());
        } catch (HttpResponseException e) {
            // SDC seems to return 500 with no explanation for some agreements. Since we've seen that we're able
            // to select other agreements sucessfully for the same credential we don't want this to break the refresh.
            log.warn("Unable to select agreement.", e);
            return Optional.empty();
        }
    }

    /*
        Bankend needs to know which agreement is currently in use or otherwise will not accept ANY operations.
     */
    private SdcAgreementServiceConfigurationResponse updateServerWithAgreementInUse (
        SessionStorageAgreement currentAgreement) {
        return bankClient.selectAgreement(new SelectAgreementRequest()
                .setUserNumber(currentAgreement.getUserNumber())
                .setAgreementNumber(currentAgreement.getAgreementId())
        );
    }
}
