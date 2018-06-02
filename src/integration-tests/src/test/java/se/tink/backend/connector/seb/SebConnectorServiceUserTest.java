package se.tink.backend.connector.seb;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import javax.ws.rs.WebApplicationException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.connector.api.SEBConnectorService;
import se.tink.backend.connector.resources.SEBConnectorServiceResource;
import se.tink.backend.connector.rpc.seb.UserEntity;
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
import static se.tink.backend.connector.seb.SebConnectorTestBase.createUserEntity;
import static se.tink.backend.connector.seb.SebConnectorTestBase.usersEqual;

/**
 * TODO this is a unit test
 */
public class SebConnectorServiceUserTest extends SebConnectorServiceIntegrationTest {
    private SEBConnectorService sebConnectorService;
    private UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        sebConnectorService = injector.getInstance(SEBConnectorServiceResource.class);
        userRepository = injector.getInstance(UserRepository.class);
    }

    @Test
    public void userCreateRequest_savesCorrectlyToDB() {
        UserEntity userEntity = createUserEntity(null);
        sebConnectorService.user(userEntity);

        User user = userRepository.findOneByUsername(userEntity.getExternalId());
        assertNotNull(user);
        assertTrue(usersEqual(userEntity, user));
    }

    @Test
    public void userDeleteRequest_sendsCorrectDeleteRequest() {
        User user = createUserAndSaveToDB();

        successfulPostStubFor("/update/user/delete");
        sebConnectorService.deleteUser(user.getUsername());

        verify(1, postRequestedFor(urlEqualTo("/update/user/delete")));

        LoggedRequest request = findAll(postRequestedFor(urlEqualTo("/update/user/delete"))).get(0);
        DeleteUserRequest deleteUserRequest = SerializationUtils
                .deserializeFromString(request.getBodyAsString(), DeleteUserRequest.class);

        assertEquals(user.getId(), deleteUserRequest.getUserId());
    }

    @Test
    public void userCreateRequest_whenUserAlreadyInDB_givesConflictError() {
        UserEntity userEntity = createUserEntity(null);
        User user = SebConnectorTestBase.createUser(userEntity);
        userRepository.save(user);

        try {
            sebConnectorService.user(userEntity);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(409, e.getResponse().getStatus());
        }
    }

    @Test
    public void userCreateRequest_withNullExternalId_givesError() {
        UserEntity userEntity = createUserEntity(null);
        userEntity.setExternalId(null);

        try {
            sebConnectorService.user(userEntity);
            fail();
        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }
}
