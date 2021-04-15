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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.CashAccountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
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
                .filter(this::isCheckingAccounts)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Collection<CreditCardAccount> toTinkCreditCards() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(this::isAccountWithResourceId)
                .filter(this::isCreditCards)
                .map(AccountEntity::convertToCreditCards)
                .collect(Collectors.toList());
    }

    public List<AccountIdEntity> getAccountsListForNecessaryConsents() {
        if (CollectionUtils.isNotEmpty(accounts)
                && (isBeneficiaryConsentNecessary() || isIdentityConsentNecessary())) {
            return accounts.stream().map(AccountEntity::getAccountId).collect(Collectors.toList());
        }
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(AccountEntity::areConsentsNecessary)
                .map(AccountEntity::getAccountId)
                .collect(Collectors.toList());
    }

    private boolean isAccountWithResourceId(AccountEntity accountEntity) {
        return StringUtils.isNotBlank(accountEntity.getResourceId());
    }

    private boolean isIdentityConsentNecessary() {
        return links == null || !links.hasEndUserIdentity();
    }

    private boolean isBeneficiaryConsentNecessary() {
        return links == null || !links.hasBeneficiaries();
    }

    public boolean isCreditCards(AccountEntity accountEntity) {
        return CashAccountTypeEntity.CARD == accountEntity.getCashAccountType();
    }

    public boolean isCheckingAccounts(AccountEntity accountEntity) {
        return CashAccountTypeEntity.CACC == accountEntity.getCashAccountType();
    }
}
