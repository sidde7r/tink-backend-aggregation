package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class LoanFetchingResponse {
    @JsonProperty("list")
    private List<LoanAccountEntity> loanAccountEntities;
}
