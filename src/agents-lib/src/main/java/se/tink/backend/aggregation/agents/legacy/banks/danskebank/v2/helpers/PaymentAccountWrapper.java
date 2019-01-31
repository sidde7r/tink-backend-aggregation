package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.TransferAccountEntity;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.enums.SwedishGiroType;

public class PaymentAccountWrapper implements GeneralAccountEntity {

    private TransferAccountEntity account;
    private SwedishGiroType type;

    public PaymentAccountWrapper(TransferAccountEntity account, SwedishGiroType type) {
        this.account = account;
        this.type = type;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return AccountIdentifier.create(type.toAccountIdentifierType(), account.getAccountNumber());
    }

    @Override
    public String generalGetBank() {
        return null;
    }

    @Override
    public String generalGetName() {
        return account.getAccountName();
    }

}
