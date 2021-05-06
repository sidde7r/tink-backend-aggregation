package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthenticationType;
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
        switch (AuthenticationType.fromString(authenticationType).get()) {
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
