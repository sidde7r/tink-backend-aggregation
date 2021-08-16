package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {
    private Href self;
    private Href status;
    private Href startAuthorisation;
    private Href scaRedirect;
    private Href balances;
    private Href transactions;
    private AccountEntity account;
    private Href selectAuthenticationMethod;
    private Href scaStatus;
}
