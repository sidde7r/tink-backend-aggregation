package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

class TransactionsUtils {

    private static final SimpleDateFormat DATE_FORMATTER =
            new SimpleDateFormat(CreditAgricoleBaseConstants.DateFormat.API_DATE_FORMAT);

    HttpRequest constructFetchTransactionRequest(
            final String id,
            final Date dateFrom,
            final Date dateTo,
            final PersistentStorage persistentStorage,
            final CreditAgricoleBaseConfiguration creditAgricoleConfiguration) {

        URL requestUrl = constructURL(id, dateFrom, dateTo, creditAgricoleConfiguration);
        HttpRequestImpl request = new HttpRequestImpl(HttpMethod.GET, requestUrl);
        addHeaders(persistentStorage, creditAgricoleConfiguration, request);

        return request;
    }

    private URL constructURL(
            String id,
            Date dateFrom,
            Date dateTo,
            CreditAgricoleBaseConfiguration creditAgricoleConfiguration) {
        return new URL(creditAgricoleConfiguration.getBaseUrl() + ApiServices.TRANSACTIONS)
                .parameter(IdTags.ACCOUNT_ID, id)
                .queryParam(
                        CreditAgricoleBaseConstants.QueryKeys.DATE_FROM,
                        DATE_FORMATTER.format(dateFrom))
                .queryParam(
                        CreditAgricoleBaseConstants.QueryKeys.DATE_TO,
                        DATE_FORMATTER.format(dateTo));
    }

    private void addHeaders(
            PersistentStorage persistentStorage,
            CreditAgricoleBaseConfiguration creditAgricoleConfiguration,
            HttpRequestImpl request) {
        MultivaluedMap<String, Object> headers = request.getHeaders();

        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.add(
                HeaderKeys.AUTHORIZATION,
                "Bearer " + StorageUtils.getTokenFromStorage(persistentStorage));
        headers.add(HeaderKeys.PSU_IP_ADDRESS, creditAgricoleConfiguration.getPsuIpAddress());
    }
}
