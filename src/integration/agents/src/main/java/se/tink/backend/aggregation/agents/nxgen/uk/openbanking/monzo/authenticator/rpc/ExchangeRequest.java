package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.authenticator.rpc;

import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class ExchangeRequest extends AbstractForm {

    @Override
    public void put(@Nonnull String key, @Nonnull String value) {
        super.put(key, value);
    }
}
