package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities.ResultBankIdEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc.NordeaSEResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultBankIdResponse extends NordeaSEResponse {
    @JsonProperty("getBankIdAuthenticationResultOut")
    private ResultBankIdEntity resultBankIdEntity;

    public String getBankIdStatus() {
        return resultBankIdEntity.getProgressStatus();
    }

    public String getToken() {
        return resultBankIdEntity.getToken();
    }

    public String getId(String orgNumber) {
        return resultBankIdEntity.getId(orgNumber);
    }

    public String getHolderName() {
        return resultBankIdEntity.getHolderName();
    }
}
