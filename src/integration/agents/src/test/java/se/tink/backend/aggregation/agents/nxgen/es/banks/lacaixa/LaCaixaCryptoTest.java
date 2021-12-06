package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.libraries.cryptography.LaCaixaPasswordHash;

public class LaCaixaCryptoTest {

    private static final String SEED = "838A700CA8AF3720";
    private static final String PASSWORD = "password";

    @Test
    public void testPasswordHash() {
        // test data generated with frida on app:
        // ObjC.classes.MCALoginSecurityUtils.new().otp_iteraciones_pass_(seed, iter, pw).toString()
        assertEquals("008819414805a47c", getPasswordHash(SEED, 1, PASSWORD));
        assertEquals("9d2f2847ea95d085", getPasswordHash(SEED, 2, PASSWORD));
        assertEquals("b9e9732c2354caf6", getPasswordHash(SEED, 4, PASSWORD));
    }

    private String getPasswordHash(String seed, int iterations, String password) {
        return LaCaixaPasswordHash.hash(seed, iterations, password);
    }
}
