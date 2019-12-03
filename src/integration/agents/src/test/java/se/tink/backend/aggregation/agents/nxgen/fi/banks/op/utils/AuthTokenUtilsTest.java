package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.utils;

import org.junit.Assert;
import org.junit.Test;

public class AuthTokenUtilsTest {
    @Test
    public void testCalculateAuthToken() {
        String expectedAuthToken = "F6d1c4fa9816d8b328880d900266b6ced747b1e39";
        String seedAsHex =
                "76b4a79d4b527e3904e96c48318e43445b6061876e2e6b1528b3d568423f0e1d5343b3e18c8b2f97ab845c1026a31b86";
        String authToken = AuthTokenUtils.calculateAuthToken(seedAsHex);

        Assert.assertEquals("Calculated auth token was not correct.", expectedAuthToken, authToken);
    }
}
