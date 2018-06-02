package se.tink.backend.main.auth.validators;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.common.providers.MarketProvider;
import se.tink.backend.core.Client;
import se.tink.backend.core.Market;
import se.tink.backend.main.auth.exceptions.UnsupportedClientException;
import se.tink.backend.main.providers.ClientProvider;

public class ClientValidator {

    private final ClientProvider clientProvider;
    private final MarketProvider marketProvider;

    @Inject
    public ClientValidator(ClientProvider clientProvider, MarketProvider marketProvider) {
        this.clientProvider = clientProvider;
        this.marketProvider = marketProvider;
    }

    public Optional<Client> validateClient(String clientKey) {
        return validateClient(clientKey, null);
    }

    public Optional<Client> validateClient(String clientKey, String locale) {
        if (Strings.isNullOrEmpty(locale)) {
            locale = getDefaultLocale();
        }

        if (Strings.isNullOrEmpty(clientKey)) {
            return Optional.empty(); // Allow legacy web and oauth clients.
        }

        Client client = clientProvider.get().get(clientKey);

        if (client == null) {
            throw new UnsupportedClientException("Client doesn't exist", clientKey);
        } else if (!client.isAllowed()) {
            throw new UnsupportedClientException(client.getMessage(locale).orElse(null), clientKey);
        }

        return Optional.of(client);
    }

    private String getDefaultLocale() {
        Optional<Market> market = marketProvider.get().stream().filter(Market::isDefaultMarket).findFirst();

        if (market.isPresent()) {
            return market.get().getDefaultLocale();
        }
        return null;
    }
}
