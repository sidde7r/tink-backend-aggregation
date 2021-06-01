package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;

public class FiduciaIconUrlMapper implements GermanFields.ScaMethodEntityToIconMapper {

    public String getIconUrl(ScaMethodEntity scaMethodEntity) {
        Optional<AuthenticationType> authenticationTypeMethod =
                AuthenticationType.fromString(scaMethodEntity.getAuthenticationType());
        return authenticationTypeMethod.map(this::getUrl).orElse(null);
    }

    private String getUrl(AuthenticationType authenticationTypeMethod) {
        switch (authenticationTypeMethod) {
            case PUSH_OTP:
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-vrSecureGo.png";
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
}
