package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.Values;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.configuration.CitadeleMarketConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class CitadeleConsentManager {

    private final CitadeleBaseApiClient apiClient;
    private final StrongAuthenticationState strongAuthenticationState;
    private final String providerMarket;
    private final CitadeleMarketConfiguration baseConfiguration;
    private final PersistentStorage persistentStorage;

    URL getConsentRequest() {
        String state = strongAuthenticationState.getState();
        String code = strongAuthenticationState.getState();
        ConsentResponse consent = apiClient.getConsent(state, code);
        persistentStorage.put(StorageKeys.CONSENT_ID, consent.getConsentId());
        persistentStorage.put(
                StorageKeys.CONSENT_ID_EXPIRATION_DATA,
                LocalDateTime.now().plusDays(Values.HISTORY_MAX_DAYS));
        return new URL(
                replaceLocAndLang(
                        consent.getLinks().getScaRedirect().getHref(),
                        providerMarket,
                        baseConfiguration.getMarketLanguage()));
    }

    private String replaceLocAndLang(String url, String marketLoc, String marketLang) {
        int loc = url.indexOf("loc=");
        int lang = url.indexOf("lang=");
        StringBuilder sb = new StringBuilder(url);
        sb.replace(loc + 4, loc + 6, marketLoc);
        sb.replace(lang + 5, lang + 7, marketLang);
        return sb.toString();
    }
}
