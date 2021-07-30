package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts;

import static org.mockito.Mockito.times;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.RepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsXmlUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SantanderEsTransactionFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/santander/resources";
    private static final String USERNAME = "someone";
    private static final String PASSWORD = "fakepassword";

    private static final String NEW_TOKEN =
            "QUMxNjI5MTY2QUE5RDc1MTBEMzA3NjJCIzE4OC43OS4yMjMuNTgjMTYxOTQyOTc4MDE1MiNQRDk0Yld3Z2RtVnljMmx2YmowaU1TNHdJaUJsYm1OdlpHbHVaejBpU1ZOUExUZzROVGt0TVNJL1BqeDBiMnRsYmtSbFptbHVhWFJwYjI0K1BIVnpaWEpKUkQ0d01ETXpNalkxTlR3dmRYTmxja2xFUGp4amIyUnBaMjlRWlhKemIyNWhQakUzT1RjeE5qQTVQQzlqYjJScFoyOVFaWEp6YjI1aFBqeHVZVzFsUGtSSlJVZFBJRXBQVWtkRklGWkpWa0ZTSUVGYVEwRlNRVlJGUEM5dVlXMWxQangwYVhCdlVHVnljMjl1WVQ1R1BDOTBhWEJ2VUdWeWMyOXVZVDQ4TDNSdmEyVnVSR1ZtYVc1cGRHbHZiajQ9I0RFU2VkZS9DQkMvUEtDUzVQYWRkaW5nI3YxI1BhcnRpY3VsYXJlc1NBTlBSTyNOT1QgVVNFRCNTSEExd2l0aFJTQSNuNFk1Y0l6Yjc1OWhkZ2JIeDdJZXl6SldWbEdrTmRkUGVZVFVuRUpJWmk2cjZONE8rNnNsWVpKc0NaVXY2U0w1RTgzMVphemszWHlhMjRqRlNoTVQ3ZTBpVFlRdFo5M0UvWXJjTHlJdlp5YjdpSTBlQlhVNEZoVExzd3cxUmVtWUhjVkRFSW15azhhWGhnU2tjdmU5WFVKT1VpU1RjSk5FRU9ZVGd4ZEU0U1E9";

    private SantanderEsApiClient apiClient;
    private SantanderEsSessionStorage santanderEsSessionStorage;
    private SantanderEsTransactionFetcher santanderEsTransactionFetcher;
    private TransactionalAccount transactionalAccount;

    @Before
    public void init() {
        apiClient = Mockito.mock(SantanderEsApiClient.class);
        santanderEsSessionStorage = Mockito.mock(SantanderEsSessionStorage.class);

        transactionalAccount = Mockito.mock(TransactionalAccount.class);

        Mockito.when(santanderEsSessionStorage.getUserId()).thenReturn(USERNAME);
        Mockito.when(santanderEsSessionStorage.getPassword()).thenReturn(PASSWORD);
        santanderEsTransactionFetcher =
                new SantanderEsTransactionFetcher(apiClient, santanderEsSessionStorage);
    }

    @Test
    public void testFetchTransactionRetry() throws IOException {

        // given
        Mockito.when(apiClient.authenticateCredentials(USERNAME, PASSWORD))
                .thenReturn(
                        new String(
                                Files.readAllBytes(
                                        Paths.get(
                                                TEST_DATA_PATH,
                                                "authenticateCredentialsResponse.xml"))));

        String expected =
                SerializationUtils.serializeToString(
                        SantanderEsXmlUtils.getTagNodeFromSoapString(
                                new String(
                                        Files.readAllBytes(
                                                Paths.get(
                                                        TEST_DATA_PATH,
                                                        "accountTransactionCompleted.xml"))),
                                SantanderEsConstants.NodeTags.METHOD_RESULT));

        HttpResponseException httpResponseException = new HttpResponseException(null, null);

        Mockito.when(
                        apiClient.fetchTransactions(
                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenThrow(httpResponseException)
                .thenReturn(expected);

        // when
        TransactionKeyPaginatorResponse<RepositionEntity> transactionKeyPaginatorResponse =
                santanderEsTransactionFetcher.getTransactionsFor(transactionalAccount, null);

        // then
        Assert.assertNotNull(transactionKeyPaginatorResponse);
        Mockito.verify(apiClient, times(1)).setTokenCredential(NEW_TOKEN);
    }
}
