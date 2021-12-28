package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Getter
@Setter
@JsonObject
public class CajasurSessionState {

    @JsonIgnore private SessionStorage sessionStorage;

    private String loginResponse;

    private String globalPosition;

    private CajasurSessionState() {}

    private CajasurSessionState(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public static CajasurSessionState getInstance(SessionStorage sessionStorage) {
        return sessionStorage
                .get(CajasurSessionState.class.getSimpleName(), CajasurSessionState.class)
                .map(
                        ss -> {
                            ss.sessionStorage = sessionStorage;
                            return ss;
                        })
                .orElse(new CajasurSessionState(sessionStorage));
    }

    public void saveLoginResponse(String loginResponse) {
        this.loginResponse = loginResponse;
        save();
    }

    public void saveGlobalPosition(String globalPosition) {
        this.globalPosition = globalPosition;
        save();
    }

    private void save() {
        sessionStorage.put(CajasurSessionState.class.getSimpleName(), this);
    }
}
