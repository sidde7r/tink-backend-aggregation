package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.starling.filter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.filter.StarlingErrorsFilter;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.rpc.StarlingErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class StarlingErrorsFilterTest {
    private StarlingErrorsFilter starlingErrorsFilter;
    private Credentials credentials;

    @Before
    public void setup() {
        credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.AUTHENTICATING);
        starlingErrorsFilter = new StarlingErrorsFilter(mock(PersistentStorage.class), credentials);
    }

    @Test(expected = AuthorizationException.class)
    public void testInvalidGrantHandling() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockInvalidGrantResponse();
        when(nextFilter.handle(any())).thenReturn(response);
        starlingErrorsFilter.setNext(nextFilter);

        // when - handle next filter
        starlingErrorsFilter.handle(null);

        // then
        assertThat(credentials.getStatus()).isEqualTo(CredentialsStatus.AUTHENTICATION_ERROR);
    }

    @Test(expected = AuthorizationException.class)
    public void testInsufficientScopeHandling() {
        // given
        Filter nextFilter = mock(Filter.class);
        HttpResponse response = mockInsufficientScopeResponse();
        when(nextFilter.handle(any())).thenReturn(response);
        starlingErrorsFilter.setNext(nextFilter);

        // when - handle next filter
        starlingErrorsFilter.handle(null);

        // then
        assertThat(credentials.getStatus()).isEqualTo(CredentialsStatus.AUTHENTICATION_ERROR);
    }

    private HttpResponse mockInvalidGrantResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.getBody(StarlingErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"error\":\" invalid_grant\",\"errorDescription\":\"refresh_token could not be verified, it could be invalid, expired or revoked\"}",
                                StarlingErrorResponse.class));

        return httpResponse;
    }

    private HttpResponse mockInsufficientScopeResponse() {
        HttpResponse httpResponse = mock(HttpResponse.class);

        when(httpResponse.getStatus()).thenReturn(403);
        when(httpResponse.getBody(StarlingErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"error\":\" insufficient_scope\",\"errorDescription\":\"Required: [a:b]. Granted: [c:d, e:f]\"}",
                                StarlingErrorResponse.class));

        return httpResponse;
    }
}
