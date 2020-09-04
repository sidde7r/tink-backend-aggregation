package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdAuthPollBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdAuthPollResponse extends BaseResponse<BankIdAuthPollBodyEntity> {}
