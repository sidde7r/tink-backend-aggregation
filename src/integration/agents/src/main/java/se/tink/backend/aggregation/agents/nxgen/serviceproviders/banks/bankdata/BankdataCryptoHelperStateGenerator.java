package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.Crypto.IV_SIZE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.Crypto.RSA_KEY_SIZE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants.Crypto.SESSION_KEY_SIZE;

import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.libraries.uuid.UUIDUtils;

@RequiredArgsConstructor
public class BankdataCryptoHelperStateGenerator {

    public BankdataCryptoHelperState generate() {
        return new BankdataCryptoHelperState(
                UUIDUtils.generateUUID(),
                RSA.generateKeyPair(RSA_KEY_SIZE),
                randomBytes(SESSION_KEY_SIZE),
                randomBytes(IV_SIZE));
    }

    private static byte[] randomBytes(int size) {
        byte[] randomBytes = new byte[size];
        new SecureRandom().nextBytes(randomBytes);
        return randomBytes;
    }
}
