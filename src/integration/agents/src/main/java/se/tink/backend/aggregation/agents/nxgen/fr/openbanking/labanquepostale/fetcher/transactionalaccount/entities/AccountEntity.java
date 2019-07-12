package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountEntityBaseEntityWithHref;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity extends AccountEntityBaseEntityWithHref {

    @Override
    public AccountIdentifier getIdentifier() {
        return AccountIdentifier.create(AccountIdentifier.Type.IBAN, resourceId);
    }

    @Override
    public String getUniqueIdentifier() {
        return resourceId;
    }

    @Override
    public String getAccountNumber() {
        return resourceId;
    }
}
