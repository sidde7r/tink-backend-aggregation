package se.tink.backend.aggregation.agents.utils.authentication.encap3.storage;

import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class EncapStorage extends BaseEncapStorage {

    public EncapStorage(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    protected String generateRandomBase64Encoded(int randomLength) {
        return EncodingUtils.encodeAsBase64String(RandomUtils.secureRandom(randomLength));
    }

    protected String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }
}
