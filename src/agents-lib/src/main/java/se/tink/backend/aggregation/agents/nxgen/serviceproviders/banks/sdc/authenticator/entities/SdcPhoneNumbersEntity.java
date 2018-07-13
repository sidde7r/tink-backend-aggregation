package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SdcPhoneNumbersEntity {
    private String phoneNumber;
    private String countryType;

    public boolean hasPhoneNumber() {
        return !Strings.isNullOrEmpty(this.phoneNumber);
    }

    public String toString(SdcConfiguration agentConfiguration) {
        String countryCode = agentConfiguration.getPhoneCountryCode();
        if (this.phoneNumber.startsWith(countryCode)) {
            return this.phoneNumber;
        }

        return countryCode + this.phoneNumber;
    }

}
