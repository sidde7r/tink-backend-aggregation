package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.loan.rpc;

import java.util.ArrayList;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FetchLoanResponse extends ArrayList<LoanEntity> {}
