package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmationInfoEntity {
    private String id;
    private String created;
    private String type;
    private String state;

    // For reference:
    // `authenticationType` is null - cannot define it!
    private Object authenticationTypes;
    private String expirationDate;
    private List<Object> authentications;
    // `ownTransfers` is null - cannot define it!
    // `payments` is null - cannot define it!
    // `fundSubscribes` is null - cannot define it!
    // `fundExchanges` is null - cannot define it!
    // `fundRedeems` is null - cannot define it!
    // `messageSends` is null - cannot define it!
    // `transactionContent` is null - cannot define it!

    public String getId() {
        return id;
    }
}
