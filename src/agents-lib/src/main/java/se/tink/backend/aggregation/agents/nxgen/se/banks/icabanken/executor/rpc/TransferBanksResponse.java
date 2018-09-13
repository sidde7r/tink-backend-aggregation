package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.TransferBanksBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferBanksResponse extends BaseResponse<TransferBanksBodyEntity> {
}
