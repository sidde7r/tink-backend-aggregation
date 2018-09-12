package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoansBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanOverviewResponse extends BaseResponse<LoansBodyEntity> {
}
