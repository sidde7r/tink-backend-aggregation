package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.BankverlagAspspId.CONSORSFINANZ;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.BankverlagAspspId.DEGUSSABANK;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants.BankverlagAspspId.TARGOBANK;

import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;

@AllArgsConstructor
public class BankverlagIconUrlMapper implements GermanFields.ScaMethodEntityToIconMapper {

    private String aspspId;

    public String getIconUrl(ScaMethodEntity scaMethodEntity) {
        Optional<AuthenticationType> authenticationTypeMethod =
                AuthenticationType.fromString(scaMethodEntity.getAuthenticationType());
        return authenticationTypeMethod.map(this::getUrl).orElse(null);
    }

    private String getUrl(AuthenticationType authenticationTypeMethod) {
        switch (authenticationTypeMethod) {
            case PUSH_OTP:
                return getPushOTPImageUrl();
            case PHOTO_OTP:
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-photoTAN.png";
            case SMS_OTP:
                return "https://cdn.tink.se/provider-images/generic-otp-icons/icon-authenticationType-generic_smsCode.png";
            case CHIP_OTP:
                return "https://cdn.tink.se/provider-images/generic-otp-icons/icon-authenticationType-generic_cardReader.png";
            default:
                return null;
        }
    }

    private String getPushOTPImageUrl() {
        switch (aspspId) {
            case DEGUSSABANK:
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-degussabank-pushTAN.png";
            case TARGOBANK:
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-targobank-pushTAN.png";
            case CONSORSFINANZ:
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-consorsfinanz-pushTAN.png";
            default:
                return null;
        }
    }
}
