package se.tink.backend.aggregation.agents.utils.authentication.encap3.utils;

import static java.util.Base64.getUrlDecoder;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.storage.MockEncapStorage;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;

public class MockEncapMessageUtils extends BaseEncapMessageUtils {

    private static final String MORE_REQUESTS_TO_ENCAP_CLIENT_THAN_EXPECTED = "More requests to Encap client than expected";

    private int counter = 0;
    private int hexCounter = 0;
    private int randomKeyCounter = 0;
    private int emkCounter = 0;

    public MockEncapMessageUtils(
            EncapConfiguration configuration,
            MockEncapStorage storage,
            TinkHttpClient httpClient,
            DeviceProfile deviceProfile) {
        super(configuration, storage, httpClient, deviceProfile);
    }

    @Override
    protected byte[] prepareRandomKey(int size) {
        switch (counter++) {
            case 0:
                return getUrlDecoder().decode("jncyyLtjrU8xd7bwVKE8EUm6T4SjGB0yAJKQd_cDYFE=");
            case 1:
                return getUrlDecoder().decode("hlXer31K4ywd4-DQqIiFQzkZ8TSB7rhmLIXjTzH2Brw=");
            case 2:
                return getUrlDecoder().decode("tcMHFtQWbKBdX8LhRC-ArfU2DQMIjygDKrgkxARHhD0=");
            case 3:
                return getUrlDecoder().decode("40p2H3q1gK8oEUx1ynVFfDKb3bvwjfGrCZDo_lPuMxw=");
            case 4:
                return getUrlDecoder().decode("PVyGPjdg6lCEBuvN6rjROA==");
            case 5:
                return getUrlDecoder().decode("tg7mU8xzBLSG1a4RrkpzAQ==");
            case 6:
                return getUrlDecoder().decode("_oX0neCcyoEF1emIbZSlbw==");
            case 7:
                return getUrlDecoder().decode("P4OLhDp6szOP3JshZU4IRQ==");
            case 8:
                return getUrlDecoder().decode("8aJPMrNPV79IQZbgjDGGj2VFMWu6KGLOGaGL76r-qCk=");
            case 9:
                return getUrlDecoder().decode("MDmN_8KKYOY_GMyGbyuhuZ821GDIDrdbYY-uxFPxzvc=");
            default:
                throw new IllegalStateException(MORE_REQUESTS_TO_ENCAP_CLIENT_THAN_EXPECTED);
        }
    }

    @Override
    protected String generateRandomHex() {
        switch (hexCounter++) {
            case 0:
                return "E6703874";
            case 1:
                return "7580E4EB";
            case 2:
                return "83954748";
            default:
                throw new IllegalStateException(MORE_REQUESTS_TO_ENCAP_CLIENT_THAN_EXPECTED);
        }
    }

    @Override
    protected KeyPair generateEcKeyPair() {
        switch (randomKeyCounter++) {
            case 0:
                return new KeyPair(
                        getPublicKey(
                                "MFIwEAYHKoZIzj0CAQYFK4EEABoDPgAEAT5MVqTcP0I88BOWR3NYm6hGcwZVSH8jWzrdEYyiADA0dj3XYbaIP6r0Rmyqg0duc3h8lf8ru4DTBvhm"),
                        getPrivateKey(
                                "MIGGAgEAMBAGByqGSM49AgEGBSuBBAAaBG8wbQIBAQQdJbmItgGwcK+zUgmCA4+zYWspjc1Y9LuG1cqdlPugBwYFK4EEABqhQAM+AAQBPkxWpNw/QjzwE5ZHc1ibqEZzBlVIfyNbOt0RjKIAMDR2Pddhtog/qvRGbKqDR25zeHyV/yu7gNMG+GY="));
            case 1:
                return new KeyPair(
                        getPublicKey(
                                "MFIwEAYHKoZIzj0CAQYFK4EEABoDPgAEAeaMf7qx23kOJ7kBHoalJZzceIByLzdmpkv7e4wFARhBS0XJnnTnRTBchE0BXfKofINf+I9hXwIM+iir"),
                        getPrivateKey(
                                "MIGGAgEAMBAGByqGSM49AgEGBSuBBAAaBG8wbQIBAQQdPfv/pLqJnDKwTQTu/p2CWpkMeCOnhqhQo2KZ9u6gBwYFK4EEABqhQAM+AAQB5ox/urHbeQ4nuQEehqUlnNx4gHIvN2amS/t7jAUBGEFLRcmedOdFMFyETQFd8qh8g1/4j2FfAgz6KKs="));
            default:
                throw new IllegalStateException(MORE_REQUESTS_TO_ENCAP_CLIENT_THAN_EXPECTED);
        }
    }

    private PublicKey getPublicKey(String base64EncodedKey) {
        byte[] publicKeyData = EncodingUtils.decodeBase64String(base64EncodedKey);
        return EllipticCurve.convertPEMtoPublicKey(publicKeyData);
    }

    private PrivateKey getPrivateKey(String base64EncodedKey) {
        byte[] privateKeyData = EncodingUtils.decodeBase64String(base64EncodedKey);
        return EllipticCurve.convertPEMtoPrivateKey(privateKeyData);
    }

    @Override
    protected String getEmk(byte[] randKey) {
        switch (emkCounter++) {
            case 0:
                return "MpzhEJHH7lUOZ3eb89a9jdJ5v6R9rFhwyXae3k/KMulmvzLhd69u6C/9tA0alK8CQf6FrTxPM6jSjjm5NASqmezcmsbRyAnsstaR+9dnx6fN33t1QM6TPoHErSFnfKjSdwWG1cEELi9RJR4ckP1yw4U75eu410a/oqpBSZaT7rBv75kihI/oudJSlRcxeBDcOEIgp24UbshYFkQD+r4fiBTnq6JJ3tCF3IXDZQQsZxyigvePpo2Bg680bj1ypgBFOfOzmBo9K4IbDxpAUWPEKhcJq4HZ+9ZqhJ/jDtp/ueC/ZFG/vlC9k0Hl0m0umR3/OMMq8Nulb4WF+Z2hE4npBQ==";
            case 1:
                return "GtGqQvidag0Pgk6Rj0no1dI0UcK7kQSPafCD69t+4mITNrjFpNqPsBDeQnwY9luGKoRj15L4qydyQYRacvZVUU+g+t783d0YNfbRUV188kfrVj6nvB7jQSFjsDwVr3v2NPizqjY6T8NmimZVxcPI2y2SaJb4C+qWItpOhLUP3+Ys2kYPBDyx2cyQje5f2yaugbyNUlYTNuTNizZVV14/bHdUn0wlk4FaXqmWd4zSjJOCRQjI0i2kG9/XWFcsN+TPS/ywaOfXPBGiPewM34cUDR7i9AS9snulIr/jLy5jlxmeu3r3Com2Rvz0BL8U1W/V+WtooJ0oihQ7XF5FpzGYyA==";
            case 2:
                return "ftSwMeOtT83X/1NgRk1z+jNdUm1mbv/DIU0oKzXE0/hbiG6xY6Z11Kdtpb0eGIO6jUY+Fm1owfWaFR16Vp2pU8e7BF7pFf2vhrhea9tSExPT9sshc3DQdZO6NUUwQY4AZd5/35tsbcGd/OXQk+H6w0lYfOcIltH6XkPteuYjZ7xq2XVOEOd7ylv4o6zpKKQ1s0kfrXK98CAOaGdcbf+Kpo5TtajdZkU7jNtxlaRwHZP+CINIcSkw+4seQ0zEyaeJ4/IaAXiDjwf0J+1KmDD3BS06UJNWjpdIXhphNElX69/hkvEqf+Jie6JP03vm+Dl6zzku5YWi97zMuMugoeEjMg==";
            default:
                throw new IllegalStateException(MORE_REQUESTS_TO_ENCAP_CLIENT_THAN_EXPECTED);
        }
    }
}
