package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginID {

    private String device = IngConstants.Default.MOBILE_PHONE;
    private String birthday;
    private LoginDocument loginDocument = new LoginDocument();

    public LoginID(String document, String birthday) {
        loginDocument.setDocument(document);
        this.birthday = birthday;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setLoginDocument(LoginDocument loginDocument) {
        this.loginDocument = loginDocument;
    }

    public String getBirthday() {
        return birthday;
    }

    public LoginDocument getLoginDocument() {
        return loginDocument;
    }
}
