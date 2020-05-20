package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites.LoginDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends BaseResponse {
    private LoginDataEntity data;

    private String errorCode;

    public LoginDataEntity getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
