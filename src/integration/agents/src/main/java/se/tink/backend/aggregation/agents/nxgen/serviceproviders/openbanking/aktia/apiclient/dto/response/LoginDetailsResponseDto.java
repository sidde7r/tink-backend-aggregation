package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.dto.response;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LoginDetailsResponseDto {

    private DomainSettingsDto domainSettings;

    private OtpChallengeDto otpChallenge;

    private UserAccountInfoDto userAccountInfo;

    private TermsAcceptanceInfoDto termsAcceptanceInfo;
}
