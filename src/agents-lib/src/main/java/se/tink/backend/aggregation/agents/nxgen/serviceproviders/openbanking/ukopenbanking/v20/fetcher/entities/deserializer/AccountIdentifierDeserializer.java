package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.deserializer;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.account.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20Constants;
import se.tink.backend.aggregation.utils.json.deserializers.IdentifierMapDeserializer;

public class AccountIdentifierDeserializer extends IdentifierMapDeserializer<String, AccountIdentifierEntity> {
    public AccountIdentifierDeserializer() {
        super(UkOpenBankingV20Constants.ModelAttributes.SCHEME_NAME, String.class, AccountIdentifierEntity.class);
    }
}
