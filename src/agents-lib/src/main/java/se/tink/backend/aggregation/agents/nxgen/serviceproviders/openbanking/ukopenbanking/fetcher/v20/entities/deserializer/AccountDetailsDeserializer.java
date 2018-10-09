package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v20.entities.deserializer;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v20.entities.account.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20Constants;
import se.tink.backend.aggregation.utils.json.deserializers.IdentifierMapDeserializer;

public class AccountDetailsDeserializer extends IdentifierMapDeserializer<AccountDetailsEntity> {
    public AccountDetailsDeserializer() {
        super(UkOpenBankingV20Constants.ModelAttributes.SCHEME_NAME, AccountDetailsEntity.class);
    }
}
