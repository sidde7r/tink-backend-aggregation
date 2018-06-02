package se.tink.backend.common.providers;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.repository.mysql.main.ProviderImageRepository;
import se.tink.backend.utils.ClearingNumberBankToProviderMapImpl;
import se.tink.backend.utils.ProviderImageMap;

@Singleton
public class ProviderImageProvider implements Provider<ProviderImageMap> {
    private final Supplier<ProviderImageMap> providerImageSupplier;

    @Inject
    public ProviderImageProvider(final ProviderImageRepository providerImageRepository) {
        this.providerImageSupplier = Suppliers.memoizeWithExpiration(() -> new ProviderImageMap(
                providerImageRepository.findAll(),
                new ClearingNumberBankToProviderMapImpl()), 30, TimeUnit.MINUTES);
    }

    @Override
    public ProviderImageMap get() {
        return providerImageSupplier.get();
    }

    public Supplier<ProviderImageMap> getSupplier() {
        return providerImageSupplier;
    }
}
