package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.entities.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class LaBanquePostaleApiClient {

    private final TinkHttpClient client;
    private final Form formBuilder;

    public LaBanquePostaleApiClient(TinkHttpClient client) {
        this.client = client;
        formBuilder = new Form();
    }

    public String initLogin() {

        String rawHtml = client.request(LaBanquePostaleConstants.Urls.INIT_LOGIN)
                .queryParam(LaBanquePostaleConstants.QueryParams.TAM_OP,
                        LaBanquePostaleConstants.QueryDefaultValues.LOGIN)
                .queryParam(LaBanquePostaleConstants.QueryParams.ERROR_CODE,
                        LaBanquePostaleConstants.QueryDefaultValues.ZERO)
                .queryParam(LaBanquePostaleConstants.QueryParams.URL,
                        LaBanquePostaleConstants.QueryDefaultValues.AUTH_INIT)
                .get(String.class);

        Matcher m = LaBanquePostaleConstants.Regex.NUMPAD_QUERY_PATTERN.matcher(rawHtml);
        if (m.find()) {
            return m.group(LaBanquePostaleConstants.Regex.NUMPAD_URL_GROUP_NAME);
        }

        throw new IllegalStateException(LaBanquePostaleConstants.ErrorMessages.NO_NUMPAD_PARAMS);
    }

    public byte[] getLoginNumpad(String numpadUrlExt) {

        return client.request(
                LaBanquePostaleConstants.Urls.GET_NUMPAD_BASE.concat(numpadUrlExt))
                .get(byte[].class);
    }

    public Optional<String> submitLogin(String username, String password) {

        formBuilder.put(LaBanquePostaleConstants.QueryParams.URL_BACKEND,
                LaBanquePostaleConstants.QueryDefaultValues.SUBMIT_AUTH);
        formBuilder.put(LaBanquePostaleConstants.QueryParams.ORIGIN,
                LaBanquePostaleConstants.QueryDefaultValues.TACTILE);
        formBuilder.put(LaBanquePostaleConstants.QueryParams.PASSWORD,
                password);
        formBuilder.put(LaBanquePostaleConstants.QueryParams.CV,
                LaBanquePostaleConstants.QueryDefaultValues.TRUE);
        formBuilder.put(LaBanquePostaleConstants.QueryParams.CVVS,
                "");
        formBuilder.put(LaBanquePostaleConstants.QueryParams.USERNAME,
                username);

        HttpResponse response = client.request(LaBanquePostaleConstants.Urls.SUBMIT_LOGIN)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED)
                .post(HttpResponse.class);

        return errorFromResponse(response);
    }

    public Optional<String> isAlive() {

        HttpResponse response = client.request(LaBanquePostaleConstants.Urls.KEEP_ALIVE)
                .get(HttpResponse.class);
        return errorFromResponse(response);
    }

    private Optional<String> errorFromResponse(HttpResponse response) {

        List<URI> redirects = response.getRedirects();
        if (!redirects.isEmpty()) {

            return errorFromURI(redirects.get(redirects.size() - 1));
        }

        return Optional.empty();
    }

    private Optional<String> errorFromURI(URI uri) {

        Matcher m = LaBanquePostaleConstants.Regex.ERROR_REDIRECT_PATTERN.matcher(uri.toString());
        if (m.find()) {

            return Optional.of(m.group(LaBanquePostaleConstants.Regex.ERROR_CODE_GROUP_NAME));
        }

        return Optional.empty();
    }
}
