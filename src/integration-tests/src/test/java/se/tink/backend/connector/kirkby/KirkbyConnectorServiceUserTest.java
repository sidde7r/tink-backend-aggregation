package se.tink.backend.connector.kirkby;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.connector.TestBase;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.rpc.UserEntity;
import se.tink.backend.connector.transport.ConnectorUserServiceJerseyTransport;
import se.tink.backend.core.User;
import se.tink.backend.rpc.DeleteUserRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static se.tink.backend.connector.TestBase.createUserEntity;
import static se.tink.backend.connector.TestBase.usersEqual;

/**
 * TODO this is a unit test
 */
public class KirkbyConnectorServiceUserTest extends KirkbyConnectorServiceIntegrationTest {
    private ConnectorUserServiceJerseyTransport userServiceTransport;
    private UserRepository userRepository;
    private TestBase testBase;

    @Before
    public void setUp() throws Exception {
        userServiceTransport = injector.getInstance(ConnectorUserServiceJerseyTransport.class);
        userRepository = injector.getInstance(UserRepository.class);
        testBase = injector.getInstance(TestBase.class);
    }

    @Test
    public void userCreateRequest_savesCorrectlyToDB() throws RequestException {
        UserEntity userEntity = createUserEntity(null);
        userServiceTransport.createUser(userEntity);

        User user = userRepository.findOneByUsername(userEntity.getExternalId());
        assertNotNull(user);
        assertTrue(usersEqual(userEntity, user));
    }

    @Test
    public void userDeleteRequest_sendsCorrectDeleteRequest() throws RequestException {
        User user = createUserAndSaveToDB();

        successfulPostStubFor("/update/user/delete");
        userServiceTransport.deleteUser(user.getUsername());

        verify(1, postRequestedFor(urlEqualTo("/update/user/delete")));

        LoggedRequest request = findAll(postRequestedFor(urlEqualTo("/update/user/delete"))).get(0);
        DeleteUserRequest deleteUserRequest = SerializationUtils
                .deserializeFromString(request.getBodyAsString(), DeleteUserRequest.class);

        assertEquals(user.getId(), deleteUserRequest.getUserId());
    }

    @Test
    public void userCreateRequest_whenUserAlreadyInDB_givesError() throws RequestException {
        UserEntity userEntity = createUserEntity(null);
        User user = testBase.createUser(userEntity);
        userRepository.save(user);

        try {
            userServiceTransport.createUser(userEntity);
            fail();
        } catch (RequestException e) {
            assertEquals(RequestError.USER_ALREADY_EXISTS, e.getError());
        }
    }
}
