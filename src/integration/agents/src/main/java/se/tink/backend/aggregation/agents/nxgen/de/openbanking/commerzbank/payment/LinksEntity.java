package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import java.util.Map;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {

    private String scaOAuth;
    private String scaRedirect;
    private String scaStatus;
    private Map<String, String> status;
}
