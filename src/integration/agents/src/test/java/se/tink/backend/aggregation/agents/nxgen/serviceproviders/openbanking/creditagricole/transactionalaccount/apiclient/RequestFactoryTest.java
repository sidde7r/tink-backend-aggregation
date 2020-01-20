package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RequestFactoryTest {

    private RequestFactory requestFactory;
    private PersistentStorage persistentStorage;
    private CreditAgricoleBaseConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        requestFactory = new RequestFactory();
        persistentStorage = Mockito.mock(PersistentStorage.class);
        configuration = Mockito.mock(CreditAgricoleBaseConfiguration.class);
    }

    @Test
    public void shouldProduceValidRequest() throws ParseException {
        // given
        Date dateFrom = new SimpleDateFormat("yyyy-MM-dd").parse("2019-01-01");
        Date dateTo = new SimpleDateFormat("yyyy-MM-dd").parse("2019-03-01");
        OAuth2Token accesToken = OAuth2Token.createBearer("ACCESS_TOKEN", "refresh", 99999L);

        // when
        when(configuration.getBaseUrl()).thenReturn("http://base.url");
        when(configuration.getPsuIpAddress()).thenReturn("1.2.3.4");
        when(persistentStorage.get(
                        CreditAgricoleBaseConstants.StorageKeys.OAUTH_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(accesToken));

        HttpRequest request =
                requestFactory.constructFetchTransactionRequest(
                        "123_ACCOUNT_ID", dateFrom, dateTo, persistentStorage, configuration);

        // then
        assertThat(request.getMethod()).isEqualTo(HttpMethod.GET);
        assertThat(request.getUrl().toUri())
                .hasPath("/dsp2/v1/accounts/123_ACCOUNT_ID/transactions");
        assertThat(request.getUrl().toUri()).hasHost("base.url");
        assertThat(request.getUrl().toUri()).hasScheme("http");
        assertThat(request.getUrl().toUri().getQuery()).contains("dateFrom=2019-01-01");
        assertThat(request.getUrl().toUri().getQuery()).contains("dateTo=2019-03-01");
        assertThat(request.getHeaders().getFirst(HttpHeaders.ACCEPT))
                .isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo("Bearer ACCESS_TOKEN");
        assertThat(request.getHeaders().getFirst(HeaderKeys.PSU_IP_ADDRESS)).isEqualTo("1.2.3.4");
    }
}
