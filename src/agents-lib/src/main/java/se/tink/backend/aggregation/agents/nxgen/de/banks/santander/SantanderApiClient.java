package se.tink.backend.aggregation.agents.nxgen.de.banks.santander;

import java.util.Date;
import java.util.regex.Matcher;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.entities.RequestAccountDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.entities.TransactionQueryEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderApiClient {

    private final TinkHttpClient client;
    private final SessionStorage storage;
    Logger logger = LoggerFactory.getLogger(SantanderApiClient.class);
    AggregationLogger longlogger = new AggregationLogger(SantanderApiClient.class);

    public SantanderApiClient(TinkHttpClient client, SessionStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    private RequestBuilder getRequest(String baseUrl, String resource) {
        return client.request(new URL(baseUrl + resource))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
    }

    private RequestBuilder getRequest(String baseUrl, String resource, String token, String requestData) {
        return getRequest(baseUrl, resource)
                .queryParam(SantanderConstants.QUERYPARAMS.AUTHENTICATION_TYPE,
                        SantanderConstants.QUERYPARAMS.AUTHENTICATION_TYPE_TOKEN)
                .queryParam(SantanderConstants.QUERYPARAMS.TOKEN, token)
                .queryParam(SantanderConstants.QUERYPARAMS.REQUEST_DATA, requestData);
    }

    private String getTokenFromResult(String result) {
        Matcher matcher = SantanderConstants.REGEX.pattern.matcher(result);

        if (matcher.find()) {
            return matcher.group(SantanderConstants.REGEX.FULL_MATCH_ONLY);
        } else {
            logger.error("Unable to find token with regex!", SantanderConstants.LOGTAG.SANTANDER_REGEX_PARSE_ERROR);
            return null;
        }
    }

    public String getTokenFromStorage() {
        return this.storage.get(SantanderConstants.STORAGE.TOKEN);
    }

    public boolean tokenExists() {
        return this.storage.containsKey(SantanderConstants.STORAGE.TOKEN);
    }

    public void login(LoginRequest request) throws AuthenticationException {
        String stringRequest = request.toXml();

        String doc = getRequest(SantanderConstants.URL.BASEURL, SantanderConstants.URL.LOGIN)
                .post(String.class, stringRequest);

        this.storage.put(SantanderConstants.STORAGE.TOKEN, getTokenFromResult(doc));
    }

    public FetchAccountsResponse fetchAccounts() {
        String token = getTokenFromStorage();
        String requestData = new RequestAccountDataEntity().toJson();

        FetchAccountsResponse response = getRequest(SantanderConstants.URL.BASEURL, SantanderConstants.URL.ACCOUNT,
                token, requestData)
                .post(FetchAccountsResponse.class);

        try {
            longlogger.infoExtraLong(SerializationUtils.serializeToString(response),
                    SantanderConstants.LOGTAG.SANTANDER_ACCOUNT_LOGGING);
        } catch (Exception e) {
            longlogger.infoExtraLong(e.toString(), SantanderConstants.LOGTAG.SANTANDER_ACCOUNT_PARSING_ERROR);
        }

        storage.put(SantanderConstants.STORAGE.LOCAL_CONTRACT_DETAIL,
                response.getAccountResultEntity().getLocalContractDetail());
        storage.put(SantanderConstants.STORAGE.LOCAL_CONTRACT_TYPE,
                response.getAccountResultEntity().getLocalContractType());
        storage.put(SantanderConstants.STORAGE.COMPANY_ID, response.getAccountResultEntity().getCompanyId());

        return response;
    }

    public FetchTransactionsResponse fetchTransactions(Date fromDate, Date toDate) {
        String token = getTokenFromStorage();
        String contractDetail = storage.get(SantanderConstants.STORAGE.LOCAL_CONTRACT_DETAIL);
        String contractType = storage.get(SantanderConstants.STORAGE.LOCAL_CONTRACT_TYPE);
        String companyId = storage.get(SantanderConstants.STORAGE.COMPANY_ID);

        TransactionQueryEntity queryEntity = new TransactionQueryEntity(fromDate, toDate, contractType, contractDetail, companyId);

        FetchTransactionsResponse response = getRequest(SantanderConstants.URL.BASEURL,
                SantanderConstants.URL.TRANSACTIONS, token, SerializationUtils.serializeToString(queryEntity))
                .post(FetchTransactionsResponse.class);
        longlogger.infoExtraLong(SerializationUtils.serializeToString(response),
                SantanderConstants.LOGTAG.SANTANDER_TRANSACTION_LOGGING);
        return response;
    }

}
