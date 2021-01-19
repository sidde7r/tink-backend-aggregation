package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia;

import java.util.function.BiFunction;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants.Default;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class RequestFactory {
    private final TinkHttpClient httpClient;

    RequestFactory(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    RequestBuilder create(Scope scope, URL url) {
        return scope.request(httpClient, url);
    }

    enum Scope {
        NORMAL(
                (client, url) ->
                        client.request(url)
                                .queryParam(
                                        BankiaConstants.Query.CM_FORCED_DEVICE_TYPE,
                                        BankiaConstants.Default.JSON)
                                .queryParam(
                                        BankiaConstants.Query.OIGID, BankiaConstants.Default.TRUE)
                                .queryParam(
                                        BankiaConstants.Query.J_GID_COD_APP,
                                        BankiaConstants.Default.LOWER_CASE_AM)
                                .queryParam(
                                        BankiaConstants.Query.J_GID_COD_DS,
                                        BankiaConstants.Default.LOWER_CASE_OIP)
                                .queryParam(
                                        BankiaConstants.Query.ORIGEN,
                                        BankiaConstants.Default.UPPER_CASE_AM)
                                .accept(MediaType.APPLICATION_JSON)
                                .acceptLanguage(Default.ACCEPT_LANGUAGE)),
        WITH_SESSION(
                (client, url) ->
                        client.request(url)
                                .queryParam(
                                        BankiaConstants.Query.J_GID_COD_APP,
                                        BankiaConstants.Default.O3)
                                .queryParam(
                                        BankiaConstants.Query.J_GID_COD_DS,
                                        BankiaConstants.Default.LOWER_CASE_OIP)
                                .queryParam(
                                        BankiaConstants.Query.X_J_GID_COD_APP,
                                        BankiaConstants.Default.LOWER_CASE_AM)
                                .queryParam(
                                        BankiaConstants.Query.CM_FORCED_DEVICE_TYPE,
                                        BankiaConstants.Default.JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .acceptLanguage(Default.ACCEPT_LANGUAGE));

        private final BiFunction<TinkHttpClient, URL, RequestBuilder> function;

        Scope(BiFunction<TinkHttpClient, URL, RequestBuilder> function) {
            this.function = function;
        }

        public RequestBuilder request(TinkHttpClient httpClient, URL url) {
            return this.function.apply(httpClient, url);
        }
    }
}
