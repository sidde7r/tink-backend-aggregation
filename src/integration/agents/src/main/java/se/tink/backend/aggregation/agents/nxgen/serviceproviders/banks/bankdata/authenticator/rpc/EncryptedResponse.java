package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.Decryptor;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EncryptedResponse {

    private String data;

    public String getData() {
        return data;
    }

    public <T> T decrypt(Decryptor decryptor, Class<T> type) {
        return SerializationUtils.deserializeFromBytes(decryptor.decrypt(data), type);
    }

    public String decrypt(Decryptor decryptor) {
        return new String(decryptor.decrypt(data));
    }
}
