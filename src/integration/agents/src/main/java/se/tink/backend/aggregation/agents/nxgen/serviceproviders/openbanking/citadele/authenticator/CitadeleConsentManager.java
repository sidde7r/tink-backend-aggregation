package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.Values;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class CitadeleConsentManager {

    private final CitadeleBaseApiClient apiClient;
    private final StrongAuthenticationState strongAuthenticationState;
    private final String locale;
    private final String market;
    private final PersistentStorage persistentStorage;

    URL getConsentRequest() {
        String state = strongAuthenticationState.getState();
        ConsentResponse consent = apiClient.createConsent(state);
        persistentStorage.put(Psd2Headers.Keys.CONSENT_ID, consent.getConsentId());
        persistentStorage.put(
                StorageKeys.CONSENT_ID_EXPIRATION_DATE,
                LocalDateTime.now().plusDays(Values.HISTORY_MAX_DAYS));
        return new URL(
                replaceLocAndLang(consent.getLinks().getScaRedirect().getHref(), market, locale));
    }

    private String replaceLocAndLang(String url, String marketLoc, String marketLang) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance().path(url).build();
        return Objects.requireNonNull(uriComponents.getPath())
                .replace("lang=lv", "lang=" + marketLang)
                .replace("loc=LV", "loc=" + marketLoc);
    }
}
