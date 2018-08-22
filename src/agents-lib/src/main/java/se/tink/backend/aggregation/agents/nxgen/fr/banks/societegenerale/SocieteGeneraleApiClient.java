package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc.AuthInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.rpc.LoginGridResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SocieteGeneraleApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;

    public SocieteGeneraleApiClient(TinkHttpClient client,
            PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    public LoginGridResponse getLoginGrid() {
        return client.request(SocieteGeneraleConstants.Url.SEC_VK_GEN_CRYPTO)
                .get(LoginGridResponse.class);
    }

    public AuthenticationResponse postAuthentication(String user_id, String cryptocvcs, String codsec) {

        String deviceId = getDeviceId();
        String token = getToken();

        AuthenticationRequest formBody = AuthenticationRequest.create(user_id, cryptocvcs, codsec, deviceId, token);

        return client.request(SocieteGeneraleConstants.Url.SEC_VK_AUTHENT)
                .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                .post(AuthenticationResponse.class);
    }

    public AuthInfoResponse getAuthInfo() {
        return client.request(SocieteGeneraleConstants.Url.GET_AUTH_INFO)
                .queryParam(SocieteGeneraleConstants.QueryParam.NIV_AUTHENT, "AUTHENTIFIE")
                .get(AuthInfoResponse.class);
    }

    public byte[] getLoginNumPadImage(String crypto) {
        return client.request(SocieteGeneraleConstants.Url.SEC_VK_GEN_UI)
                .queryParam(SocieteGeneraleConstants.QueryParam.MODE_CLAVIER, "0")
                .queryParam(SocieteGeneraleConstants.QueryParam.VK_VISUEL, "vk_widescreen")
                .queryParam(SocieteGeneraleConstants.QueryParam.CRYPTOGRAMME, crypto)
                .get(byte[].class);
    }

    private String getDeviceId() {
        return persistentStorage.get(SocieteGeneraleConstants.StorageKey.DEVICE_ID);
    }

    private String getToken() {
        String token = persistentStorage.get(SocieteGeneraleConstants.StorageKey.TOKEN);
        return token != null ? token : "";
    }

}
