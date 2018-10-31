package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.CurrenciesItem;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetCurrenciesResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetUserDataResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AsLhvSessionStorage {
    SessionStorage sessionStorage;

    public AsLhvSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void setCurrentUser(final String currentUser) {
        sessionStorage.put(AsLhvConstants.Storage.CURRENT_USER, currentUser);
    }

    public Optional<String> getCurrentUser() {
        return Optional.ofNullable(sessionStorage.get(AsLhvConstants.Storage.CURRENT_USER));
    }

    private HashMap<Integer, String> convertCurrenciesToMap(List<CurrenciesItem> currencies) {
        HashMap<Integer, String> result = new HashMap<>();
        for (CurrenciesItem currency : currencies) {
            Optional<String> currencyName = currency.getName();
            if (currencyName.isPresent()) {
                result.put(currency.getCurrencyId(), currencyName.get());
            }
        }
        return result;
    }

    public void setCurrencies(GetCurrenciesResponse currencies) {
        sessionStorage.put(AsLhvConstants.Storage.CURRENCIES, currencies);
    }

    public Optional<String> getCurrency(int currencyId) {
        Optional<GetCurrenciesResponse> res = sessionStorage.get(AsLhvConstants.Storage.CURRENCIES,
                                                                 GetCurrenciesResponse.class);
        if (!res.isPresent()) {
            return Optional.empty();
        }

        Optional<List<CurrenciesItem>> currencies = res.get().getCurrencies();
        if (!currencies.isPresent()) {
            return Optional.empty();
        }

        HashMap<Integer, String> currencyMap = convertCurrenciesToMap(currencies.get());
        return Optional.ofNullable(currencyMap.get(currencyId));
    }

    public void setBaseCurrencyId(int baseCurrencyId) {
        sessionStorage.put(AsLhvConstants.Storage.BASE_CURRENCY_ID, baseCurrencyId);
    }

    public Optional<Integer> getBaseCurrencyId() {
        return sessionStorage.get(AsLhvConstants.Storage.BASE_CURRENCY_ID, Integer.class);
    }

    public void setUserData(final GetUserDataResponse userData) {
        sessionStorage.put(AsLhvConstants.Storage.USER_DATA, userData);
    }

    public Optional<GetUserDataResponse> getUserData() {
        return sessionStorage.get(AsLhvConstants.Storage.USER_DATA, GetUserDataResponse.class);
    }
}
