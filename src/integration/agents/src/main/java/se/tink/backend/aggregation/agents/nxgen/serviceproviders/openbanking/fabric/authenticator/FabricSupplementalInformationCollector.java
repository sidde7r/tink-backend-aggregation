package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RequiredArgsConstructor
public class FabricSupplementalInformationCollector {

    private static final String SMS_OTP_FIELD_NAME = "smsOtpField";
    private static final LocalizableKey SMS_OTP_DESCRIPTION = new LocalizableKey("SMS code");
    private static final LocalizableKey SMS_OTP_HELP_TEXT =
            new LocalizableKey("Please enter the code you received.");
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public String collectSmsOtp() {
        String otp =
                supplementalInformationController
                        .askSupplementalInformationSync(getOtpField())
                        .get(SMS_OTP_FIELD_NAME);

        if (otp == null) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(
                    "Supplemental info did not come with otp code!");
        }
        return otp;
    }

    private Field getOtpField() {
        return Field.builder()
                .description(catalog.getString(SMS_OTP_DESCRIPTION))
                .helpText(catalog.getString(SMS_OTP_HELP_TEXT))
                .name(SMS_OTP_FIELD_NAME)
                .build();
    }
}
