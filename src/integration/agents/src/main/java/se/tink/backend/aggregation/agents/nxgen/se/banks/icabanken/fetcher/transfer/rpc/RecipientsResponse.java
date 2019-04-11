package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientsResponseBody;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RecipientsResponse extends BaseResponse<RecipientsResponseBody> {}
