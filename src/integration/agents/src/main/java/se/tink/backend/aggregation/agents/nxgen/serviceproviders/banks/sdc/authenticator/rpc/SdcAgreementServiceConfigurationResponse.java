package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcPaymentProfileEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcPhoneNumbersEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcServiceConfigurationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcAgreementServiceConfigurationResponse {

    private SdcPaymentProfileEntity paymentProfile;
    private SdcServiceConfigurationEntity serviceConfiguration;
    private Boolean ownAgreement;
    private List<SdcPhoneNumbersEntity> phoneNumbers;

    public SdcPaymentProfileEntity getPaymentProfile() {
        return this.paymentProfile;
    }

    public SdcServiceConfigurationEntity getServiceConfiguration() {
        return this.serviceConfiguration;
    }

    public Boolean getOwnAgreement() {
        return this.ownAgreement;
    }

    public List<SdcPhoneNumbersEntity> getPhoneNumbers() {
        return this.phoneNumbers;
    }

    public Optional<SdcPhoneNumbersEntity> findFirstPhoneNumber() {
        return Optional.ofNullable(this.phoneNumbers).orElse(Collections.emptyList()).stream()
                .filter(SdcPhoneNumbersEntity::hasPhoneNumber)
                .findFirst();
    }
}
