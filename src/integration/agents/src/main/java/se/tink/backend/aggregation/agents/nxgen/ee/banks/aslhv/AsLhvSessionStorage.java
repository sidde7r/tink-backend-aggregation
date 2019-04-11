package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.CurrenciesItem;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.CurrentUser;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetCurrenciesResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetUserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.IsAuthenticatedResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AsLhvSessionStorage {
    private final SessionStorage sessionStorage;

    public AsLhvSessionStorage(final SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void setCurrentUser(final String currentUser) {
        sessionStorage.put(AsLhvConstants.Storage.CURRENT_USER, currentUser);
    }

    public String getCurrentUser() {
        return Optional.ofNullable(sessionStorage.get(AsLhvConstants.Storage.CURRENT_USER))
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Current user was not set during session."));
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

    public String getCurrency(int currencyId) {
        return translateCurrency(currencyId)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Base currency could not be mapped during session."));
    }

    private Optional<String> translateCurrency(int currencyId) {
        Optional<GetCurrenciesResponse> res =
                sessionStorage.get(AsLhvConstants.Storage.CURRENCIES, GetCurrenciesResponse.class);
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

    public int getBaseCurrencyId() {
        return sessionStorage
                .get(AsLhvConstants.Storage.BASE_CURRENCY_ID, Integer.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Base currency was not set during session."));
    }

    public void setUserData(final GetUserDataResponse userData) {
        sessionStorage.put(AsLhvConstants.Storage.USER_DATA, userData);
    }

    public GetUserDataResponse getUserData() {
        Optional<GetUserDataResponse> userData =
                sessionStorage.get(AsLhvConstants.Storage.USER_DATA, GetUserDataResponse.class);
        if (!userData.isPresent()) {
            throw new IllegalStateException("No user data found.");
        } else if (!userData.get().requestSuccessful()) {
            final String message =
                    String.format("User data request failed: %s", userData.get().getErrorMessage());
            throw new IllegalStateException(message);
        }
        return userData.get();
    }

    public void setIsAuthenticatedResponseData(
            final IsAuthenticatedResponse isAuthenticatedResponse) {
        Optional<CurrentUser> currentUser = isAuthenticatedResponse.getCurrentUser();
        if (currentUser.isPresent()) {
            Optional<String> name = currentUser.get().getName();
            int baseCurrencyId = currentUser.get().getBaseCurrencyId();
            if (name.isPresent()) {
                setCurrentUser(name.get());
            }
            setBaseCurrencyId(baseCurrencyId);
        }
    }
}
