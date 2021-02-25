package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.authenticator.entities.AvailableService;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class LoginResponse {
    private List<AvailableService> availableServicesList;
    private String branch;
    private String bugurooId;
    private String entity;
    private String hmacCodePerson;
    private String hmacCodeSesion;
    private String language;
    private String lastAccessDate;
    private String maiaId;
    private int maxIdleTime;
    private String name;
    private boolean passMustBeChanged;
    private String personNumber;
    private boolean remoteSessionClosed;
    private String style;
    private boolean verisecEnabled;
}
