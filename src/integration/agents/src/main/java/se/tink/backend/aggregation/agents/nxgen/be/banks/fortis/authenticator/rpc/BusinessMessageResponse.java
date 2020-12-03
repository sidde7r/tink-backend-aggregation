package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.EBankingBusinessMessageBulkEntity;

@Getter
@Setter
public abstract class BusinessMessageResponse<T> {

    private T value;
    private EBankingBusinessMessageBulkEntity businessMessageBulk;

    public boolean isError() {
        return businessMessageBulk != null && businessMessageBulk.getPewCode() != null;
    }
}
