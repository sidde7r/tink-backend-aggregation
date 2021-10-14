package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields.ScaMethodEntityToIconMapper;

public class PostbankIconUrlMapper implements ScaMethodEntityToIconMapper {

    @JsonIgnore
    public String getIconUrl(ScaMethodEntity scaMethodEntity) {
        return scaMethodEntity.getAuthenticationTypeMethod().map(this::getUrl).orElse(null);
    }

    private String getUrl(AuthenticationType authenticationTypeMethod) {
        switch (authenticationTypeMethod) {
            case PUSH_OTP:
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-bestSign.png";
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
