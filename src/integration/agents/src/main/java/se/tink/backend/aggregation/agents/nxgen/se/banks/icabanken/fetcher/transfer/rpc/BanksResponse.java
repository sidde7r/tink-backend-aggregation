package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.BanksResponseBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BanksResponse extends BaseResponse<BanksResponseBody> {}
