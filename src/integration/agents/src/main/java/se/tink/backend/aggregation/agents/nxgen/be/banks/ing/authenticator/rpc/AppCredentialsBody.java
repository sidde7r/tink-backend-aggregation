package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class AppCredentialsBody extends MultivaluedMapImpl {

    public AppCredentialsBody(byte[] encryptedQueryData) {
        add(IngConstants.Session.ValuePairs.METHOD.getKey(), IngConstants.Session.ValuePairs.METHOD.getValue());
        add(IngConstants.Session.ValuePairs.PROTOCOL_VERSION.getKey(),
                IngConstants.Session.ValuePairs.PROTOCOL_VERSION.getValue());
        add(IngConstants.Session.ValuePairs.PUBLIC_KEY_ID.getKey(),
                IngConstants.Session.ValuePairs.PUBLIC_KEY_ID.getValue());
        add(IngConstants.Session.ENC_QUERY_DATA, getUrlEncodedB64String(encryptedQueryData));
    }

    private String getUrlEncodedB64String(byte[] encryptedQueryData) {
        String urlEncodedB64 = Base64.getUrlEncoder().encodeToString(encryptedQueryData);
        return urlEncodedB64.replaceAll("=", "\\.");
    }
}
