package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private Href authoriseTransaction;
    private Href selectAuthenticationMethod;

    public String getAuthoriseTransaction() {
        return authoriseTransaction.getHref();
    }

    public String getSelectAuthenticationMethod() {
        return selectAuthenticationMethod.getHref();
    }
}
