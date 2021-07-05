package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    @Getter private Href self;
    @Getter private Href status;
    @Getter private Href startAuthorisation;
    @Getter private Href scaRedirect;
    @Getter private Href balances;
    @Getter private Href transactions;
    @Getter private AccountEntity account;
    @Getter private Href selectAuthenticationMethod;
    @Getter private Href scaStatus;
}
