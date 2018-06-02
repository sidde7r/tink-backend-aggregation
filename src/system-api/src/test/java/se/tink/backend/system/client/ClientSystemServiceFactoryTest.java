package se.tink.backend.system.client;

import com.sun.jersey.api.client.ClientHandlerException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.rpc.DeleteUserRequest;

/**
 * If one wants, one can start a test server with `python -m SimpleHTTPServer` to verify calls are being initiated.
 * However, it's not necessary to make these tests go through.
 */
public class ClientSystemServiceFactoryTest {
    private ClientSystemServiceFactory factory;

    @Before
    public void setUp() {
        // Expected: no service is running on port 31415.
        factory = ClientSystemServiceFactory.buildWithoutPinning("http://127.0.0.1:31415/");
    }


    @Test(expected = ClientHandlerException.class)
    public void testDeleteUserCall() {
        DeleteUserRequest request = new DeleteUserRequest();
        request.setComment("");
        request.setReasons(Collections.<String> emptyList());
        request.setUserId("abdefghijklmnopqrstuvwxyz");

        factory.getUpdateService().deleteUser(request);
    }
}
