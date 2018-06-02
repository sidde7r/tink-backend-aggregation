package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.entities.BankingServiceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NordeaResponse {
    @JsonProperty("bankingServiceResponse")
    private BankingServiceEntity bankingServiceEntity;

    public BankingServiceEntity getBankingServiceEntity() {
        return bankingServiceEntity;
    }

    @JsonIgnore
    public Optional<String> getErrorCode() {
        return bankingServiceEntity != null ? bankingServiceEntity.getErrorCode() : Optional.empty();
    }
}
