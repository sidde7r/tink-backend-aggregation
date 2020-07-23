package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
@Data
public class GetAccountsResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    private List<AccountEntity> accounts;

    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(this::isAccountWithResourceId)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public boolean areConsentsNecessary() {
        return CollectionUtils.isNotEmpty(accounts)
                && (isBeneficiaryConsentNecessary()
                        || isIdentityConsentNecessary()
                        || isConsentForAnyAccountNecessary());
    }

    public List<AccountIdEntity> getListOfNecessaryConsents() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(this::isAccountWithResourceId)
                .map(AccountEntity::getAccountId)
                .collect(Collectors.toList());
    }

    private boolean isAccountWithResourceId(AccountEntity accountEntity) {
        return StringUtils.isNotBlank(accountEntity.getResourceId());
    }

    private boolean isConsentForAnyAccountNecessary() {
        return accounts.stream().anyMatch(AccountEntity::areConsentsNecessary);
    }

    private boolean isIdentityConsentNecessary() {
        return links == null || !links.hasEndUserIdentity();
    }

    private boolean isBeneficiaryConsentNecessary() {
        return links == null || !links.hasBeneficiaries();
    }
}
