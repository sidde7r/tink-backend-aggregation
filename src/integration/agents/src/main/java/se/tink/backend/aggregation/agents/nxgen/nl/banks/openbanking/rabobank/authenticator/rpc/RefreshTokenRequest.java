package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc;

import se.tink.backend.aggregation.nxgen.http.AbstractForm;

import javax.annotation.Nonnull;

public class RefreshTokenRequest extends AbstractForm {

    @Override
    public void put(@Nonnull String key, @Nonnull String value) {
        super.put(key, value);
    }
}
