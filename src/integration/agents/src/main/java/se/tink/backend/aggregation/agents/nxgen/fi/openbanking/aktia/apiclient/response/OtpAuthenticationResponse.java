package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response;

import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.ErrorResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.OtpAuthenticationResponseDto;

@Value
public class OtpAuthenticationResponse {

    private final boolean successful;

    private final OtpAuthenticationResponseDto otpAuthenticationResponseDto;

    private final ErrorResponseDto errorResponseDto;

    public OtpAuthenticationResponse(OtpAuthenticationResponseDto otpAuthenticationResponseDto) {
        this.successful = true;
        this.otpAuthenticationResponseDto = otpAuthenticationResponseDto;
        this.errorResponseDto = null;
    }

    public OtpAuthenticationResponse(ErrorResponseDto errorResponseDto) {
        this.successful = false;
        this.otpAuthenticationResponseDto = null;
        this.errorResponseDto = errorResponseDto;
    }
}
