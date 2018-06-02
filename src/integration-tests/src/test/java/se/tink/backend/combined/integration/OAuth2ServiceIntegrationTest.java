package se.tink.backend.combined.integration;

import com.google.common.collect.Sets;
import com.sun.jersey.api.client.WebResource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.client.ClientAuthorizationConfigurator;
import se.tink.backend.client.ClientServiceFactory;
import se.tink.backend.combined.AbstractServiceIntegrationTest;
import se.tink.libraries.jersey.utils.InterContainerJerseyClientFactory;
import se.tink.libraries.http.client.BasicWebServiceClassBuilder;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2AuthenticationTokenResponse;
import se.tink.backend.core.oauth2.OAuth2AuthorizationDescription;
import se.tink.backend.core.oauth2.OAuth2AuthorizeRequest;
import se.tink.backend.core.oauth2.OAuth2AuthorizeResponse;
import se.tink.backend.core.oauth2.OAuth2Client;

/**
 * TODO this is a unit test
 */
public class OAuth2ServiceIntegrationTest extends AbstractServiceIntegrationTest {
    private static final String TEST_REDIRECT_URI = "tinktest://login/";
    private static final String TEST_CLIENT_ID = "30d2dbaafcc446769a399f9496b2d1a7";
    private static final String TEST_CLIENT_SECRET = "76651a8b1cc843c1a7e573d4ed4a156d";

    @BeforeClass
    public static void preOAuth2Setup() throws Exception {
        serviceContext.getRepository(OAuth2ClientRepository.class).save(getTestClient());
    }

    @AfterClass
    public static void postOAuth2CleanUp() throws Exception {
        serviceContext.getRepository(OAuth2ClientRepository.class).delete(getTestClient());
    }

    @Test
    public void testUnauthorizedPermission() throws Exception {
        // Register a user using basic authorization.

        User user = registerTestUserWithDemoCredentialsAndData();

        // Delegate access to a third-party API consumer.

        OAuth2AuthorizeRequest request = new OAuth2AuthorizeRequest();

        request.setScope("transactions:read");
        request.setClientId(TEST_CLIENT_ID);
        request.setRedirectUri(TEST_REDIRECT_URI);

        OAuth2AuthorizeResponse response = serviceFactory.getOAuth2Service().authorize(user, request);

        // Have the third-party consumer request an access token based on the authorization code.

        OAuth2AuthenticationTokenResponse tokenResponse = serviceFactory.getOAuth2Service().token(
                TEST_CLIENT_ID, TEST_CLIENT_SECRET, "authorization_code", response.getCode(), null);

        Assert.assertNotNull(tokenResponse);
        Assert.assertNotNull(tokenResponse.getAccessToken());
        Assert.assertNotNull(tokenResponse.getRefreshToken());

        WebResource mainJerseyResource = InterContainerJerseyClientFactory.withoutPinning().build().resource(
                "http://localhost:9090/");
        ClientAuthorizationConfigurator authenticationConfigurator = ClientAuthorizationConfigurator
                .decorateAndInstantiate(mainJerseyResource);
        ClientServiceFactory thirdPartyServiceFactory = new ClientServiceFactory(new BasicWebServiceClassBuilder(
                mainJerseyResource),
                authenticationConfigurator);
        authenticationConfigurator.setBearerToken(tokenResponse.getAccessToken());

        // Fetch the accounts using basic authorization.

        boolean caughtException = false;

        try {
            thirdPartyServiceFactory.getAccountService().listAccounts(user).getAccounts();
        } catch (Exception e) {
            caughtException = true;
        }

        Assert.assertTrue("should catch exception", caughtException);

        deleteUser(user);
    }

    @Test
    public void testAuthorizationDescription() throws Exception {
        // Register a user using basic authorization.

        User user = registerTestUserWithDemoCredentialsAndData();

        // Delegate access to a third-party API consumer.

        OAuth2AuthorizeRequest request = new OAuth2AuthorizeRequest();

        request.setScope("transactions:read,accounts:read,statistics:read:expenses-by-category");
        request.setClientId(TEST_CLIENT_ID);
        request.setRedirectUri(TEST_REDIRECT_URI);

        OAuth2AuthorizationDescription description = serviceFactory.getOAuth2Service().describe(user, request);

        Assert.assertNotNull("client name should not be null", description.getClientName());
        Assert.assertNotNull("client url should not be null", description.getClientUrl());
        Assert.assertNotNull("client icon url should not be null", description.getClientIconUrl());
        Assert.assertNotEquals("client scope description should not be empty", 0, description.getScopesDescriptions()
                .size());

        deleteUser(user);
    }

    private static OAuth2Client getTestClient() {
        OAuth2Client client = new OAuth2Client();

        client.setName("Test");
        client.setRedirectUris(Sets.newHashSet(TEST_REDIRECT_URI));
        client.setScope(
                "transactions:read,accounts:read,statistics:read:expenses-by-category,statistics:read:expenses-by-category/by-count");
        client.setSecret(TEST_CLIENT_SECRET);
        client.setId(TEST_CLIENT_ID);
        client.setUrl("http://www.jhma.se/");
        client.setIconUrl("http://www.jhma.se/jhma-short.png");

        return client;
    }
}
