package se.tink.backend.common.providers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.core.Currency;

public class CurrenciesByCodeProvider implements Provider<ImmutableMap<String, Currency>> {
    private final ImmutableMap<String, Currency> currenciesByCode;

    @Inject
    public CurrenciesByCodeProvider(CurrencyRepository currencyRepository) {
        this.currenciesByCode = Maps.uniqueIndex(currencyRepository.findAll(), Currency::getCode);
    }

    @Override
    public ImmutableMap<String, Currency> get() {
        return currenciesByCode;
    }
}
