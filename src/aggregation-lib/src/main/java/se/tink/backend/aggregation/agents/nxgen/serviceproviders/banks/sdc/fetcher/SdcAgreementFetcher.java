package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcServiceConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc.SdcAgreementServiceConfigurationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.SelectAgreementRequest;

public abstract class SdcAgreementFetcher {

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

    protected SdcServiceConfigurationEntity selectAgreement(SessionStorageAgreement currentAgreement,
            SessionStorageAgreements agreements) {
        Optional<SdcServiceConfigurationEntity> configurationEntity = sessionStorage
                .getCurrentServiceConfiguration()
                .map(SdcAgreementServiceConfigurationResponse::getServiceConfiguration);
        if (configurationEntity.isPresent() && agreements.size() == 1) {
            return configurationEntity.get();
        }
        SdcAgreementServiceConfigurationResponse serviceConfiguration =
                updateServerWithAgreementInUse(currentAgreement);
        sessionStorage.putCurrentServiceConfiguration(serviceConfiguration);
        return serviceConfiguration.getServiceConfiguration();
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
