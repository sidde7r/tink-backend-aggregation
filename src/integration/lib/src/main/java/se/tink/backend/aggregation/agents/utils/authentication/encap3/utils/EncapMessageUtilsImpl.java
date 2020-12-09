package se.tink.backend.aggregation.agents.utils.authentication.encap3.utils;

import java.security.KeyPair;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.storage.EncapStorage;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;

public class EncapMessageUtilsImpl extends BaseEncapMessageUtils {

    public EncapMessageUtilsImpl(
            EncapConfiguration configuration,
            EncapStorage storage,
            TinkHttpClient httpClient,
            DeviceProfile deviceProfile) {
        super(configuration, storage, httpClient, deviceProfile);
    }

    @Override
    public byte[] prepareRandomKey(int size) {
        return RandomUtils.secureRandom(size);
    }

    @Override
    public String generateRandomHex() {
        return RandomUtils.generateRandomHexEncoded(4).toUpperCase();
    }

    @Override
    protected KeyPair generateEcKeyPair() {
        return EllipticCurve.generateKeyPair("sect233k1");
    }

    @Override
    protected String getEmk(byte[] randKey) {
        return EncapCryptoUtils.computeRsaEMK(configuration.getRsaPubKeyString(), randKey);
    }
}
