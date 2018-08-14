package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.LoginDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.NumpadDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.NumpadRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc.NumpadResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.storage.BnpParibasPersistentStorage;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BnpParibasApiClient {
    private final TinkHttpClient client;

    public BnpParibasApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public NumpadDataEntity getNumpadParams() {
        NumpadRequest formBody = NumpadRequest.create();

        NumpadResponse response = client.request(BnpParibasConstants.Urls.NUMPAD)
                .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                .post(NumpadResponse.class);

        response.assertReturnCodeOk();

        return response.getData();
    }

    public LoginDataEntity login(String username, String gridId, String passwordIndices,
            BnpParibasPersistentStorage bnpParibasPersistentStorage) {
        LoginRequest formBody = LoginRequest.create(username, gridId, passwordIndices, bnpParibasPersistentStorage);

        LoginResponse response = client.request(BnpParibasConstants.Urls.LOGIN)
                .body(formBody, MediaType.APPLICATION_FORM_URLENCODED)
                .post(LoginResponse.class);

        response.assertReturnCodeOk();

        return response.getData();
    }

    public void keepAlive() {
        BaseResponse response = client.request(BnpParibasConstants.Urls.KEEP_ALIVE)
                .get(BaseResponse.class);

        response.assertReturnCodeOk();
    }
}
