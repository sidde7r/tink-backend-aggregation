package se.tink.backend.main.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.common.repository.mysql.main.UserOAuth2ClientRoleRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserOAuth2ClientRole;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.rpc.UserLoginResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * A command for creating OAuth2 clients.
 *
 * NOTE: this command must be modified or moved when we have fully migrated the OAuth2 related logic to the internal
 * OAuth gRPC service.
 */
public class AddOauth2ClientCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(AddOauth2ClientCommand.class);

    private static final String URL = System.getProperty("url", null);
    private static final String NAME = System.getProperty("name", null);
    private static final String EMAIL = System.getProperty("email", null);
    private static final String PAYLOAD = System.getProperty("payload",
            "{\"PROVIDERS\":\"handelsbanken-bankid,danskebank-bankid,seb-bankid,nordea-bankid,lansforsakringar-bankid,swedbank-bankid,savingsbank-bankid,icabanken-bankid,avanza,americanexpress,avanza-bankid,sbab-bankid\", \"REFRESHABLE_ITEMS\": \"ACCOUNTS\", \"AUTO_AUTHORIZE\": \"true\", \"ALLOW_DEMO_CREDENTIALS\": \"true\", \"DOESNT_PRODUCE_TINK_USERS\": \"true\"}");
    private static final String SCOPE = System.getProperty("scope", "accounts:read");
    private static final String REDIRECT_URIS = System
            .getProperty("redirectUris", "[\"http://localhost:3000/callback\"]");

    public AddOauth2ClientCommand() {
        super("oauth-add-client", "Add an OAuth2 client in our database");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        Preconditions.checkNotNull(URL, "Url cannot be null");
        Preconditions.checkNotNull(NAME, "Name cannot be null");
        Preconditions.checkNotNull(EMAIL, "Email cannot be null");

        String clientId = UUIDUtils.generateUUID();
        String secret = UUIDUtils.generateUUID();
        String tmpTinkUserPassword = UUIDUtils.generateUUID();

        log.info(String.format("\nemail=%s\nname=%s\nurl=%s\npayload=%s\nscope=%s\nredirectUris=%s", EMAIL, NAME, URL,
                PAYLOAD, SCOPE, REDIRECT_URIS));

        User user = createAndGetUser(serviceContext, tmpTinkUserPassword);

        log.info(String.format("Created client with clientId=%s and userId=%s", clientId, user.getId()));

        createClient(clientId, secret, serviceContext);

        createRole(user.getId(), clientId, serviceContext);
    }

    private void createRole(String userId, String clientId, ServiceContext serviceContext) {
        UserOAuth2ClientRoleRepository roleRepository = serviceContext
                .getRepository(UserOAuth2ClientRoleRepository.class);

        UserOAuth2ClientRole role = new UserOAuth2ClientRole();
        role.setClientId(clientId);
        role.setUserId(userId);
        role.setRole("ADMIN");

        roleRepository.save(role);
    }

    private void createClient(String clientId, String secret, ServiceContext serviceContext) {
        OAuth2Client client = new OAuth2Client();
        client.setSecret(secret);
        client.setRedirectUrisSerialized(REDIRECT_URIS);
        client.setPayloadSerialized(PAYLOAD);
        client.setId(clientId);
        client.setUrl(URL);
        client.setScope(SCOPE);
        client.setName(NAME);

        OAuth2ClientRepository clientRepository = serviceContext.getRepository(OAuth2ClientRepository.class);
        clientRepository.save(client);
    }

    private User createAndGetUser(ServiceContext serviceContext, String tmpTinkUserPassword) {
        User user = new User();
        user.setUsername(EMAIL);
        user.setPassword(tmpTinkUserPassword);
        UserProfile profile = new UserProfile();
        profile.setMarket("SE");
        profile.setLocale("sv_SE");
        profile.setCurrency("SEK");
        user.setProfile(profile);

        UserLoginResponse response = serviceContext.getServiceFactory().getUserService().register(user);

        return response.getContext().getUser();
    }
}
