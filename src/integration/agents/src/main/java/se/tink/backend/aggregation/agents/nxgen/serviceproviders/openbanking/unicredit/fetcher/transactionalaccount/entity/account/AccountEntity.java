package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.entity.account;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountEntity {

    private String resourceId;
    private String iban;
    private String currency;
    private String product;
    private String cashAccountType;
    private String name;
    private String ownerName;
}
