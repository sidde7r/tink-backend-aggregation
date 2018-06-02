package se.tink.backend.main.resources;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse.Status;
import java.net.URI;
import java.util.Date;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.repository.mysql.main.UserOriginRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserOrigin;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.metrics.MetricRegistry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UserServiceResourceTest {
    @Mock
    private UserOriginRepository userOriginRepository;
    @Mock
    private UserStateRepository userStateRepository;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceConfiguration serviceConfiguration;
    @Mock
    private MetricRegistry metricRegistry;
    @Mock
    private AnalyticsController analyticsController;
    @Mock
    private ListenableThreadPoolExecutor<Runnable> executor;

    private UserServiceResource userService;
    private User user;

    private static final String someUserId = StringUtils.generateUUID();
    private static final String someExternalId = "someExternalId";
    private static final String someOtherUserId = StringUtils.generateUUID();
    private static final String someMediaSource = "someMediaSource";

    private static User mockUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(someUserId);

        // one second over the limit
        when(user.getCreated()).thenReturn(new Date(UserServiceResource.USER_ORIGIN_RELEASE_DATE.getTime() + 1000));
        return user;
    }

    @Before
    public void setUp() {
        userService = new UserServiceResource(false, null, null, null, null, null, null, null, null, null, null, null,
                serviceConfiguration, executor, null, null, null, null, null, null, userOriginRepository, null, null,
                userStateRepository, analyticsController, null, null, null, null, null, null, null, null, null, null,
                null, null, null, metricRegistry, null, null);
        user = mockUser();
        mockHeaders();
    }

    private void mockHeaders() {
        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.getRequestHeader(HttpHeaders.USER_AGENT)).thenReturn(Lists.newArrayList(
                "Mozilla/5.0 (ios) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.111 Safari/537.36"));
        userService.headers = headers;
    }

    @Test
    public void nullOriginThrowsBadRequest() {
        try {
            userService.setOrigin(user, null);
            fail("Should respond with 401 BadRequest");
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
        }
    }

    @Test
    public void validOriginDoesNotThrowException() {
        UserOrigin origin = new UserOrigin();
        origin.setMediaSource(someMediaSource);
        origin.setExternalServiceId(someExternalId);

        userService.setOrigin(user, origin);
    }

    @Test
    public void validNonOrganicOriginDoesNotThrowException() {
        UserOrigin origin = new UserOrigin();
        origin.setOrganic(false);
        origin.setMediaSource(someMediaSource);
        origin.setExternalServiceId(someExternalId);

        userService.setOrigin(user, origin);
    }

    @Test
    public void validOriginSuccessfullyCallsSaveOnRepository() {
        UserOrigin origin = new UserOrigin();
        origin.setExternalServiceId(someExternalId);
        origin.setMediaSource(someMediaSource);

        userService.setOrigin(user, origin);
        verify(userOriginRepository, times(1)).save(any(UserOrigin.class));
    }

    @Test
    public void validOriginDoesNotThrowExceptionEvenThoughThereAreOtherOriginsInRepository() {
        UserOrigin origin = new UserOrigin();
        origin.setExternalServiceId(someExternalId);
        origin.setOrganic(true);

        when(userOriginRepository.findOneByUserId(someOtherUserId)).thenReturn(new UserOrigin());

        userService.setOrigin(user, origin);
    }

    @Test
    public void validOriginThrowsBadRequestIfOriginAlreadyExistsForUser() {
        UserOrigin origin = new UserOrigin();
        origin.setOrganic(true);

        when(userOriginRepository.findOneByUserId(someUserId)).thenReturn(new UserOrigin());

        try {
            userService.setOrigin(user, origin);
            fail("Should respond with 401 BadRequest");
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
        }

        verify(userOriginRepository, never()).save(any(UserOrigin.class));
    }

    @Test
    public void originWithoutExternalServiceIdThrowsBadRequest() {
        UserOrigin origin = new UserOrigin();

        try {
            userService.setOrigin(user, origin);
            fail("Should respond with 401 BadRequest");
        } catch (WebApplicationException e) {
            assertEquals(Status.BAD_REQUEST.getStatusCode(), e.getResponse().getStatus());
        }

        verify(userOriginRepository, never()).save(any(UserOrigin.class));
    }

    @Test
    public void validNonOrganicOriginReturnsSilentlyWithoutSaveToDBIfUserWasCreatedBeforeUserOriginReleaseDate() {
        UserOrigin origin = new UserOrigin();
        origin.setOrganic(false);
        origin.setExternalServiceId(someExternalId);
        origin.setMediaSource(someMediaSource);

        when(user.getCreated()).thenReturn(new Date(UserServiceResource.USER_ORIGIN_RELEASE_DATE.getTime() - 1000));

        userService.setOrigin(user, origin);
        verify(userOriginRepository, never()).save(any(UserOrigin.class));
    }

    @Test
    public void rewriteDeepLinkUriToWebUriForTink_shouldFailForPartnersWithoutWebResetSupport() {
        URI tinkTooShort = UserServiceResource.getDesktopURI(URI.create("tink://reset/facade"));
        URI unsupportedDeepLinkPrefix = UserServiceResource.getDesktopURI(
                URI.create("genericpartner://reset/012345678901234567890123456789ab"));
        URI tinkSuccessful = UserServiceResource.getDesktopURI(
                URI.create("tink://reset/012345678901234567890123456789ab"));
        assertEquals("Reset id is expected to contain exactly 32 characters", null, tinkTooShort);
        assertEquals("Reset id should only work for supported deep link prefixes", null, unsupportedDeepLinkPrefix);
        assertNotEquals("Valid reset link should be correctly transformed to desktop URI", null, tinkSuccessful);
    }
}
