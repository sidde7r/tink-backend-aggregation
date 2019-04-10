package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.rpc;

import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchLoansResponse extends ArrayList<LoanEntity> {}
