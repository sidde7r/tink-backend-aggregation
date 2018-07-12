package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "LoginRequestEntity")
public class LoginRequestEntity {
    private DisposerContext DisposerContextObject;
    private Context ContextObject;
    private Authentication AuthenticationObject;
    private String isIncludeLoginImage;

    public DisposerContext getDisposerContext() {
        return DisposerContextObject;
    }

    public Context getContext() {
        return ContextObject;
    }

    public Authentication getAuthentication() {
        return AuthenticationObject;
    }

    public String getIsIncludeLoginImage() {
        return isIncludeLoginImage;
    }

    @XmlElement(name = "DisposerContext")
    public void setDisposerContext(DisposerContext DisposerContextObject) {
        this.DisposerContextObject = DisposerContextObject;
    }

    @XmlElement(name = "Context")
    public void setContext(Context ContextObject) {
        this.ContextObject = ContextObject;
    }

    @XmlElement(name = "Authentication")
    public void setAuthentication(Authentication AuthenticationObject) {
        this.AuthenticationObject = AuthenticationObject;
    }

    @XmlElement(name = "isIncludeLoginImage")
    public void setIsIncludeLoginImage(String isIncludeLoginImage) {
        this.isIncludeLoginImage = isIncludeLoginImage;
    }
}
