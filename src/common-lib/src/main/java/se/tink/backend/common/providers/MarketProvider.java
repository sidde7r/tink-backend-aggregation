package se.tink.backend.common.providers;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.core.Market;

public class MarketProvider implements Provider<List<Market>> {
    private final Supplier<List<Market>> marketSupplier;

    @Inject
    public MarketProvider(MarketRepository marketRepository, CurrencyRepository currencyRepository, AuthenticationConfiguration authenticationConfiguration) {
        this.marketSupplier = Suppliers.memoizeWithExpiration(
                () -> Collections.unmodifiableList(Lists.newArrayList(Iterables.transform(marketRepository.findAll(),
                        m -> {
                            m.setCurrencies(currencyRepository.findAll());
                            m.setRegisterMethods(authenticationConfiguration.getMarketRegisterMethods(m.getCode()));
                            m.setLoginMethods(authenticationConfiguration.getMarketLoginMethods(m.getCode()));
                            m.setGdprLoginMethods(authenticationConfiguration.getGdprMarketLoginMethods(m.getCode()));

                            return m;
                        }))), 20, TimeUnit.MINUTES);
    }

    @Override
    public List<Market> get() {
        return marketSupplier.get();
    }
}
