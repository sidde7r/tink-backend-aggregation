package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BankdataCryptoComponentsProviderModule extends AbstractModule {

    @Override
    public void configure() {
        bind(BankdataCryptoComponentsProvider.class)
                .toInstance(new BankdataCryptoComponentsProviderImpl());
    }
}
