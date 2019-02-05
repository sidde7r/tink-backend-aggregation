package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.authenticatior.entities.SubmitLoginForm;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.agents.rpc.AccountTypes;

public class LaBanquePostaleApiClient {

    private final TinkHttpClient client;

    public LaBanquePostaleApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public String initLogin() {

        String rawHtml = client.request(LaBanquePostaleConstants.Urls.INIT_LOGIN)
                .queryParam(LaBanquePostaleConstants.QueryParams.TAM_OP,
                        LaBanquePostaleConstants.QueryDefaultValues.LOGIN)
                .queryParam(LaBanquePostaleConstants.QueryParams.ERROR_CODE,
                        LaBanquePostaleConstants.QueryDefaultValues.ZERO)
                .queryParam(LaBanquePostaleConstants.QueryParams.URL,
                        LaBanquePostaleConstants.QueryDefaultValues.MOBILE_AUTH_BACKEND)
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

        HttpResponse response = client.request(LaBanquePostaleConstants.Urls.SUBMIT_LOGIN)
                .body(new SubmitLoginForm(username, password),
                        MediaType.APPLICATION_FORM_URLENCODED)
                .post(HttpResponse.class);

        return errorFromResponse(response);
    }

    public boolean isAlive() {

        HttpResponse response = client.request(LaBanquePostaleConstants.Urls.KEEP_ALIVE)
                .get(HttpResponse.class);

        return !errorFromResponse(response).isPresent();
    }

    private Optional<String> errorFromResponse(HttpResponse response) {

        List<URI> redirects = response.getRedirects();
        if (!redirects.isEmpty()) {

            return errorFromUri(redirects.get(redirects.size() - 1));
        }

        return Optional.empty();
    }

    private Optional<String> errorFromUri(URI uri) {

        List<NameValuePair> query = URLEncodedUtils.parse(uri, Charset.forName("UTF-8").toString());

        Map<String, String> m = query.stream().collect(
                Collectors.toMap(NameValuePair::getName,
                        NameValuePair::getValue));

        return Optional.ofNullable(m.get(LaBanquePostaleConstants.QueryParams.ERROR_PARAM))
                .map(String::toLowerCase);
    }

    public AccountsResponse getAccounts() {
        return client.request(LaBanquePostaleConstants.Urls.ACCOUNTS)
                .queryParam(LaBanquePostaleConstants.QueryParams.APPEL_ASSUARANCES,
                        LaBanquePostaleConstants.QueryDefaultValues.TRUE)
                .queryParam(LaBanquePostaleConstants.QueryParams.APPEL_PRETS,
                        LaBanquePostaleConstants.QueryDefaultValues.TRUE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(AccountsResponse.class);
    }

    public TransactionsResponse getTransactions(String accountNumber, AccountTypes type) {
        URL url = (type == AccountTypes.CHECKING) ?
                LaBanquePostaleConstants.Urls.TRANSACTIONS_CHECKING_ACCOUNTS :
                LaBanquePostaleConstants.Urls.TRANSACTIONS_SAVINGS_ACCOUNTS;
        RequestBuilder request = client.request(url)
                .queryParam(LaBanquePostaleConstants.QueryParams.CODE_MEDIA,
                        LaBanquePostaleConstants.QueryDefaultValues._9241)
                .queryParam(LaBanquePostaleConstants.QueryParams.COMPTE_NUMERO, accountNumber)
                .accept(MediaType.APPLICATION_JSON_TYPE);
        if (type == AccountTypes.CHECKING) {
            request.queryParam(LaBanquePostaleConstants.QueryParams.TYPE_RECHERCHE,
                    LaBanquePostaleConstants.QueryDefaultValues._10);
        }
        return request.get(TransactionsResponse.class);
    }

    public void getDisconnection() {
        client.request(LaBanquePostaleConstants.Urls.DISCONNECTION)
                .queryParam(LaBanquePostaleConstants.QueryParams.CODE_MEDIA,
                        LaBanquePostaleConstants.QueryDefaultValues._9241).accept(MediaType.APPLICATION_JSON_TYPE)
                .get(HttpResponse.class);
    }

}
