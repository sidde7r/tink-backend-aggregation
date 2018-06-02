package se.tink.backend.rpc;

import io.protostuff.Tag;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

import se.tink.backend.core.Credentials;

public class CredentialsListResponse {
    @Tag(1)
    @ApiModelProperty(name = "credentials", value="A list of credentials")
    private List<Credentials> credentials;

    public List<Credentials> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<Credentials> credentials) {
        this.credentials = credentials;
    }
}
