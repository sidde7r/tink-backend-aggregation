package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v20.entities.deserializer;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v20.entities.account.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20Constants;
import se.tink.backend.aggregation.utils.json.deserializers.IdentifierMapDeserializer;

public class AccountIdentifierDeserializer extends IdentifierMapDeserializer<AccountIdentifierEntity> {
    public AccountIdentifierDeserializer() {
        super(UkOpenBankingV20Constants.ModelAttributes.SCHEME_NAME, AccountIdentifierEntity.class);
    }
}
