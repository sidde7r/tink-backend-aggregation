package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EbankingUsersValue;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.BusinessMessageResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EbankingUsersResponse extends BusinessMessageResponse<EbankingUsersValue> {}
