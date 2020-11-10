package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingTestFixtures.JWKS;

import java.security.PublicKey;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.rpc.JsonWebKeySet;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class JwkTest {

    @Test
    public void testJwkSetParsing() {
        JWKS.forEach(
                jwks -> {
                    JsonWebKeySet set =
                            SerializationUtils.deserializeFromString(jwks, JsonWebKeySet.class);
                    List<PublicKey> keys = set.getAllKeys();
                    Assert.assertFalse(keys.isEmpty());
                });
    }
}
