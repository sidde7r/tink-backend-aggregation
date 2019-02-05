package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SigningRequestHeaderDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeEncodedPair;

public class EnrollDeviceRoundTwoRequest {
    private final SigningRequestHeaderDto header;

    private EnrollDeviceRoundTwoRequest(SigningRequestHeaderDto header) {
        this.header = header;
    }

    public static EnrollDeviceRoundTwoRequest create(String signingId) {
        return new EnrollDeviceRoundTwoRequest(
                SigningRequestHeaderDto.create(
                        TypeEncodedPair.createHidden(signingId)));
    }

    public SigningRequestHeaderDto getHeader() {
        return header;
    }
}
