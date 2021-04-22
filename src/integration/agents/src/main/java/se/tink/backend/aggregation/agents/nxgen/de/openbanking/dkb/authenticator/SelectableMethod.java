package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.utils.supplementalfields.GermanFields;

public interface SelectableMethod extends GermanFields.SelectEligible {
    String getIdentifier();

    default String getIconUrl() {
        switch (StringUtils.remove(getAuthenticationType().toUpperCase(), "_")) {
            case "PUSHOTP":
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-pushTAN-DKB.png";
            case "PHOTOOTP":
                return "https://cdn.tink.se/provider-images/de/otp-icons/icon-authenticationType-photoTAN.png";
            case "SMSOTP":
                return "https://cdn.tink.se/provider-images/generic-otp-icons/icon-authenticationType-generic_smsCode.png";
            case "CHIPOTP":
                return "https://cdn.tink.se/provider-images/generic-otp-icons/icon-authenticationType-generic_cardReader.png";
            default:
                return null;
        }
    }
}
