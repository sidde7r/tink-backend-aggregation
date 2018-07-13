package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.SignValidationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SigningChallengeSotpResponse extends SignValidationResponse {
    private List<TypeValuePair> dataFields;
    private TypeValuePair cryptoApplicationName;

    public List<String> getDataFields() {
        return dataFields.stream().map(TypeValuePair::getValue).collect(Collectors.toList());
    }

    public String getCryptoApplicationName() {
        return cryptoApplicationName.getValue();
    }
}
