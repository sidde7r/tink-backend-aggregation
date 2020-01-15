package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

public class ConfigurationProvider {

    private static final String BASE_URL = "https://m.ingdirect.it";

    public String getBaseUrl() {
        return BASE_URL;
    }

    public boolean useRsaWithPadding() {
        return true;
    }

    public RSAPublicKey getRsaExternalPublicKey() {
        return RSA.getPubKeyFromBytes(
                Base64.getDecoder()
                        .decode(
                                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0F/ZDFTFT7f41U/0WbeN"
                                        + "HBzYkhy3FSnD1QtrVDfzYshpnEvu6bHaWeQF7x44gxumQ5uIT0W21NUhMwWbWYef"
                                        + "2GwG4V+5Wr+4O8dkL5Gl/ey6jsYfVOq4WPA6NN2ansPNN/tQ4wQvd4WypQp/UtlS"
                                        + "koThzVmEWs4LHe6BN6aPBdUfwUEaesADIHWlubHgeSO0otHW9IMEQgyK7FtrulmU"
                                        + "V9NrbL3QUVf7RlG7jJdAg07sM5F95xvY19tXcw5b8C33doYniNuVb2xy8/h1LQqK"
                                        + "Uto7XVd94pMQlImZs74DjINvKtb0cgRq9owa89DkMiyszbJgtjwKB6gc7cD4hypJ"
                                        + "QQIDAQAB"));
    }
}
