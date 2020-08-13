package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class LoansResponse {
    private List<LoansEntity> loans;
}
