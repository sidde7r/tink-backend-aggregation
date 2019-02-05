package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignTypesResponse {
    private List<SignTypeDto> signTypes;
    private SignTypesInfoDto signTypesInfo;
    private SigningIdHeaderDto header;

    public List<SignTypeDto> getSignTypes() {
        return signTypes;
    }

    public SignTypesInfoDto getSignTypesInfo() {
        return signTypesInfo;
    }

    public SigningIdHeaderDto getHeader() {
        return header;
    }

    public String getSignTypeId(String signType) {
        return getSignTypes().stream()
                .filter(signTypeDto ->
                        Optional.ofNullable(signTypeDto)
                                .map(SignTypeDto::getSignType)
                                .map(TypeValuePair::getValue)
                                .filter(signTypeValue -> Objects.equals(signType, signTypeValue))
                                .isPresent())
                .findFirst()
                .map(SignTypeDto::getSignTypeId)
                .map(TypeEncodedPair::getEncoded)
                .orElseThrow(() -> new IllegalStateException("Could not retrieve signTypeId"));
    }
}
