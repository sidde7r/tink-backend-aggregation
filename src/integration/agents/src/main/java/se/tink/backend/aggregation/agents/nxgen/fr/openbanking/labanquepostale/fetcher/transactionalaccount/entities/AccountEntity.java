package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountIdentificationDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountLinksWithHrefEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountUsage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.CashAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
@Data
public class AccountEntity {

    private String resourceId;

    private String bicFi;

    private AccountIdentificationDto accountId;

    private String name;

    private AccountUsage usage;

    private CashAccountType cashAccountType;

    private List<BalanceBaseEntity> balances;

    private String linkedAccount;

    @JsonProperty("_links")
    private AccountLinksWithHrefEntity links;

    public String getTransactionLink() {
        return Optional.ofNullable(links)
                .map(AccountLinksWithHrefEntity::getTransactionLink)
                .orElse("");
    }

    public String getUniqueIdentifier() {
        return accountId.getIban();
    }

    public AccountIdentifier getIdentifier() {
        return new IbanIdentifier(accountId.getIban());
    }

    public String getAccountNumber() {
        return accountId.getIban();
    }
}
