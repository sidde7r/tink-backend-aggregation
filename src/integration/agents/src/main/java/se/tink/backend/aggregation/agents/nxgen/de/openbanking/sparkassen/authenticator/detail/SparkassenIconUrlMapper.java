package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;

public class SparkassenIconUrlMapper implements GermanFields.ScaMethodEntityToIconMapper {

    public String getIconUrl(ScaMethodEntity scaMethodEntity) {
        return getUrl(scaMethodEntity.getAuthenticationType());
    }

    private String getUrl(String authenticationType) {
        switch (authenticationType) {
            case "PUSH_OTP":
            case "PUSH_DEC":
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-pushTan.png";
            case "PHOTO_OTP":
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-photoTAN.png";
            case "SMS_OTP":
                return "https://cdn.tink.se/provider-images/generic-otp-icons/icon-authenticationType-generic_smsCode.png";
            case "CHIP_OTP":
                return "https://cdn.tink.se/provider-images/generic-otp-icons/icon-authenticationType-generic_cardReader.png";
            default:
                return null;
        }
    }
}
