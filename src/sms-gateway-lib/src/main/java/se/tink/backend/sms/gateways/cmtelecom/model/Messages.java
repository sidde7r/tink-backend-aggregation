package se.tink.backend.sms.gateways.cmtelecom.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Messages {
    private Authentication authentication;

    public List<Msg> getMsg() {
        return msg;
    }

    public void setMsg(List<Msg> msg) {
        this.msg = msg;
    }

    @JsonProperty("msg")
    private List<Msg> msg = null;

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }
}
