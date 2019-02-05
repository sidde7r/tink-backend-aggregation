package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EbankingBusinessMessageBulk;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EbankingUsersValue;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EbankingUsersResponse {
    private EbankingBusinessMessageBulk businessMessageBulk;
    private EbankingUsersValue value;

    public EbankingBusinessMessageBulk getBusinessMessageBulk() {
        return businessMessageBulk;
    }

    public EbankingUsersValue getValue() {
        return value;
    }
}
