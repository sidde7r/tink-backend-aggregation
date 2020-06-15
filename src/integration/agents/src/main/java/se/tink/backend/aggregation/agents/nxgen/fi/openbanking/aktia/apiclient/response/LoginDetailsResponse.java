package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response;

import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.LoginDetailsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OpenAmErrorResponseDto;

@Value
public class LoginDetailsResponse {

    private final boolean successful;

    private final LoginDetailsResponseDto loginDetailsResponseDto;

    private final OpenAmErrorResponseDto openAmErrorResponseDto;

    public LoginDetailsResponse(LoginDetailsResponseDto loginDetailsResponseDto) {
        this.successful = true;
        this.loginDetailsResponseDto = loginDetailsResponseDto;
        this.openAmErrorResponseDto = null;
    }

    public LoginDetailsResponse(OpenAmErrorResponseDto openAmErrorResponseDto) {
        this.successful = false;
        this.loginDetailsResponseDto = null;
        this.openAmErrorResponseDto = openAmErrorResponseDto;
    }
}
