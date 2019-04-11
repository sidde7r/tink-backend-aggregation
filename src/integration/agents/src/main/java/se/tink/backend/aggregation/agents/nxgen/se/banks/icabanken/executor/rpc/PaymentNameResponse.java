package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.PaymentNameBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentNameResponse extends BaseResponse<PaymentNameBodyEntity> {}
