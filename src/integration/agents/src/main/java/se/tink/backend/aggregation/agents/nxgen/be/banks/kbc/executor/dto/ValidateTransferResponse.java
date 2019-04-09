package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.executor.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateTransferResponse extends HeaderResponse {
    private TypeValuePair scenario;
    private TypeValuePair scenarioText;
    private TypeValuePair newBICOfBeneficiary;
    private TypeValuePair beneficiaryAccountNo;
    private TypeValuePair remittanceFolderId;
    private TypeValuePair crossReference;

    public TypeValuePair getScenario() {
        return scenario;
    }

    public TypeValuePair getScenarioText() {
        return scenarioText;
    }

    public String getSignType() {
        switch (scenario.getValue()) {
            case "B":
                return KbcConstants.Predicates.SIGN_TYPE_MANUAL;
            case "A":
            default:
                return KbcConstants.Predicates.SIGN_TYPE_SOTP;
        }
    }
}
