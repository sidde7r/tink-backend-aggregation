package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.InvestmentsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentAccountResponse extends BaseResponse<InvestmentsBodyEntity> {}
