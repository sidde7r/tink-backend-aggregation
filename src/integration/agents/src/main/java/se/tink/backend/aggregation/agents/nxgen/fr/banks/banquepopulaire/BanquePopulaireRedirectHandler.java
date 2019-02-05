package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire;

import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.AppConfigEntity;
import se.tink.backend.aggregation.nxgen.http.redirect.RedirectHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class BanquePopulaireRedirectHandler extends RedirectHandler {

    private final SessionStorage sessionStorage;

    public BanquePopulaireRedirectHandler(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public String modifyRedirectUri(String uri) {
        Optional<AppConfigEntity> appConf = getAppConfig();

        if (appConf.isPresent()) {
            String oldLocation = appConf.get().getWebSSOv3LoginScreenURL();

            if (uri.contains(oldLocation)) {
                String newLocation = appConf.get().getWebSSOv3WebAPIBaseURL();
                return updateRedirectUrlForWebApi(uri, newLocation);
            }
        }

        return uri;
    }

    private String updateRedirectUrlForWebApi(String oldLocation, String newLocation) {
        try {
            URI redirectUri = new URI(oldLocation);
            List<NameValuePair> parts = URLEncodedUtils.parse(redirectUri, "UTF-8");

            String transactionId = parts.stream()
                    .filter(p -> BanquePopulaireConstants.Query.TRANSACTION_ID.equalsIgnoreCase(p.getName()))
                    .map(NameValuePair::getValue)
                    .findFirst().orElseThrow(() -> new IllegalStateException("No transactionId found"));

            return new URL(redirectUri.getScheme(), redirectUri.getHost(), MessageFormat
                    .format(newLocation, transactionId)).toExternalForm();
        } catch (Exception e) {
            throw new IllegalStateException("Could not parse redirect URI", e);
        }
    }

    private Optional<AppConfigEntity> getAppConfig() {
        return sessionStorage.get(BanquePopulaireConstants.Storage.APP_CONFIGURATION, AppConfigEntity.class);
    }
}
