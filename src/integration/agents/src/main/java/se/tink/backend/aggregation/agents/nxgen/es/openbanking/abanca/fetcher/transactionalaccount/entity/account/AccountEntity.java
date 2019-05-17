package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public class AccountEntity {

    private String type;
    private String id;
    private AccountAttributesEntity attributes;
    private Relationships relationships;
    private AccountLinksEntity links;

    public TransactionalAccount toTinkAccount() {

        return CheckingAccount.builder()
                .setUniqueIdentifier(attributes.getAccountCode())
                .setAccountNumber(attributes.getAccountCode())
                .setBalance(attributes.getAvailableBalance().toTinkAmount())
                .setAlias(type)
                .addAccountIdentifier(new IbanIdentifier(attributes.getAccountCode()))
                .setApiIdentifier(id)
                .build();
    }
}
