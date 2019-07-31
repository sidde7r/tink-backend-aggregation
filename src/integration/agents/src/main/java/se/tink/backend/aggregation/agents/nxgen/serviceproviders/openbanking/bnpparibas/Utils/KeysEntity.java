package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.Utils;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KeysEntity {

    public final String kty;
    public final String kid;
    public final List<String> x5c;

    public KeysEntity(String kty, String kid, List<String> x5c) {
        this.kty = kty;
        this.kid = kid;
        this.x5c = x5c;
    }
}
