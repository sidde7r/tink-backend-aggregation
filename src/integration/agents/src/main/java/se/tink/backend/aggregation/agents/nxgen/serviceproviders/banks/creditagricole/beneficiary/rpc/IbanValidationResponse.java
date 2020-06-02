package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.entity.ExternalAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IbanValidationResponse extends DefaultResponse {
    private ExternalAccountEntity externalAccount;

    public String getBic() {
        return externalAccount.getBic();
    }
}

