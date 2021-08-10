package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ScaMethod implements GermanFields.SelectEligible {

    private String name;
    private String authenticationType;
    private String authenticationMethodId;

    @JsonIgnore
    public String getIconUrl() {
        Optional<AuthenticationType> authenticationTypeMethod =
                AuthenticationType.fromString(authenticationType);
        return authenticationTypeMethod.map(this::getUrl).orElse(null);
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
