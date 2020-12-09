package se.tink.backend.aggregation.agents.utils.authentication.encap3.module;

import com.google.inject.AbstractModule;

public final class EncapClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EncapClientProvider.class).to(Encap3ClientProviderImpl.class);
    }
}
