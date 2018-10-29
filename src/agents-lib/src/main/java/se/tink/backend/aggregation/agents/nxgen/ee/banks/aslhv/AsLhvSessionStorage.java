package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.CurrenciesItem;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetCurrenciesResponse;
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

    public void setUserId(int userId) {
        sessionStorage.put(AsLhvConstants.Storage.USER_ID, userId);
    }

    public Optional<Integer> getUserId() {
        try {
            return Optional.of(Integer.valueOf(sessionStorage.get(AsLhvConstants.Storage.USER_ID)));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private HashMap<Integer, String> convertCurrenciesToMap(List<CurrenciesItem> currencies) {
        HashMap<Integer, String> result = new HashMap<>();
        for (CurrenciesItem currency : currencies) {
            result.put(currency.getCurrencyId(), currency.getName());
        }
        return result;
    }

    public void setCurrencies(GetCurrenciesResponse currencies) {
        sessionStorage.put(AsLhvConstants.Storage.CURRENCIES, currencies);
    }

    public Optional<String> getCurrency(int currencyId) {
        Optional<GetCurrenciesResponse> res = sessionStorage.get(AsLhvConstants.Storage.CURRENCIES,
                                                                 GetCurrenciesResponse.class);
        Optional<String> result;
        if (res.isPresent()) {
            HashMap<Integer, String> currencies = convertCurrenciesToMap(res.get().getCurrencies());
            result = Optional.ofNullable(currencies.get(currencyId));
        } else {
            result = Optional.empty();
        }
        return result;
    }

    public void setBaseCurrencyId(int baseCurrencyId) {
        sessionStorage.put(AsLhvConstants.Storage.BASE_CURRENCY_ID, baseCurrencyId);
    }

    public Optional<Integer> getBaseCurrencyId() {
        return sessionStorage.get(AsLhvConstants.Storage.BASE_CURRENCY_ID, Integer.class);
    }
}
