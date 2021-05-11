package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.identitydata.entities.PreferredNameEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.giro.validation.LuhnCheck;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@JsonObject
public class FetchIdentityDataResponse {
    private static final Logger log = LoggerFactory.getLogger(FetchIdentityDataResponse.class);
    private static final String SSN_PATTERN =
            "^(19|20)\\d{2}(0[1-9]|1[0-2])([0-2]\\d|3[0-1])\\d{4}$";

    // e.g, 1992-01-23
    @JsonProperty("birth_date")
    private String birthDate;

    @JsonProperty("branch_id")
    private String branchId;

    // ssn e.g. 199201234567
    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("loyalty_group")
    private String loyaltyGroup;

    @JsonProperty("loyalty_group_in_raw")
    private String loyaltyGroupInRaw;

    // ssn e.g. 199201234567
    @JsonProperty("person_id")
    private String personId;

    @JsonProperty("preferred_name")
    private PreferredNameEntity preferredName;

    private String segment;

    public IdentityData toTinkIdentityData(NordeaConfiguration nordeaConfiguration) {
        if (nordeaConfiguration.isBusinessAgent()) {
            return toBusinessIdentityData();
        }
        return toPrivateIdentityData();
    }

    private IdentityData toBusinessIdentityData() {
        return IdentityData.builder()
                .setSsn(customerId)
                .addFirstNameElement(preferredName.getGivenName())
                .addSurnameElement(preferredName.getFamilyName())
                .setDateOfBirth(LocalDate.parse(birthDate))
                .build();
    }

    public IdentityData toPrivateIdentityData() {
        // Both customerId and personId are SSN, but personId is missing sometimes.
        if (!Strings.isNullOrEmpty(personId)
                && !Strings.isNullOrEmpty(customerId)
                && !personId.equals(customerId)) {
            log.warn("person_id doesn't match customer_id");
        }

        final String ssn = Optional.ofNullable(personId).orElse(customerId);
        if (isValidSsn(ssn)) {
            return SeIdentityData.of(
                    preferredName.getGivenName(), preferredName.getFamilyName(), ssn);
        }

        return IdentityData.builder()
                .addFirstNameElement(preferredName.getGivenName())
                .addSurnameElement(preferredName.getFamilyName())
                .setDateOfBirth(LocalDate.parse(birthDate))
                .build();
    }

    private boolean isValidSsn(String ssn) {
        return ssn.matches(SSN_PATTERN)
                && LuhnCheck.isLastCharCorrectLuhnMod10Check(ssn.substring(2));
    }
}
