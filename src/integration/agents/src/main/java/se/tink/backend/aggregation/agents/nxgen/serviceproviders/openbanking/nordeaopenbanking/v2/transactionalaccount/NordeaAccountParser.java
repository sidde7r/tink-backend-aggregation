package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities.AccountNumbersEntity;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;

public class NordeaAccountParser {

    public TransactionalAccount toTinkAccount(AccountEntity accountEntity) {
        String rawAccountNumber = getAccountNumber(accountEntity);
        return TransactionalAccount.builder(accountEntity.tinkAccountType(), rawAccountNumber)
                .setAccountNumber(rawAccountNumber)
                .setName(getName(accountEntity))
                .setBalance(accountEntity.getAccountBalance())
                .setBankIdentifier(accountEntity.getId())
                .addIdentifiers(getAccountIdentifiers(accountEntity))
                .putInTemporaryStorage(
                        NordeaBaseConstants.Storage.TRANSACTIONS,
                        getTransactionsLink(accountEntity))
                .build();
    }

    // defaults to IBAN
    protected String getAccountNumber(AccountEntity accountEntity) {
        return findAccountNumberByType(
                        accountEntity, NordeaBaseConstants.Account.ACCOUNT_NUMBER_IBAN)
                .orElseThrow(() -> new IllegalStateException("No account number found"));
    }

    // sometimes we get a transactions link, sometimes not
    private String getTransactionsLink(AccountEntity accountEntity) {
        return accountEntity
                .getLinks()
                .findLinkByName(NordeaBaseConstants.Link.TRANSACTIONS_LINK)
                .map(LinkEntity::getHref)
                .orElse(
                        NordeaBaseConstants.Url.getTransactionPathForAccount(
                                accountEntity.getId()));
    }

    protected Collection<AccountIdentifier> getAccountIdentifiers(AccountEntity accountEntity) {
        return Optional.ofNullable(accountEntity.getAccountNumbers())
                .orElseGet(Collections::emptyList).stream()
                .map(AccountNumbersEntity::toTinkIdentifier)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    protected String getName(AccountEntity accountEntity) {
        return noContent(accountEntity.getAccountName())
                ? accountEntity.getProduct()
                : accountEntity.getAccountName();
    }

    protected boolean noContent(String s) {
        return Strings.nullToEmpty(s).trim().isEmpty();
    }

    protected boolean hasContent(String s) {
        return !noContent(s);
    }

    protected Optional<String> findAccountNumberByType(
            AccountEntity accountEntity, final String accountNumberType) {

        return Optional.ofNullable(accountEntity.getAccountNumbers())
                .orElseGet(Collections::emptyList).stream()
                .filter(
                        accountNumberEntity ->
                                accountNumberType.equalsIgnoreCase(accountNumberEntity.getType())
                                        && hasContent(accountNumberEntity.getValue()))
                .map(AccountNumbersEntity::getValue)
                .findFirst();
    }
}
