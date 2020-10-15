package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto;

import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DerivedKeyOutput {

    private final SecretKeySpec encryptionKey;

    private final SecretKeySpec signingKey;
}
