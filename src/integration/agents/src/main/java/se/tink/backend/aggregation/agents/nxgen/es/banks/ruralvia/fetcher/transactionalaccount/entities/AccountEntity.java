package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.entities;

import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.RuralviaUtils;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@Getter
@Builder
public class AccountEntity {

    private String accountAlias;
    private String accountNumber;
    private String balance;
    @Builder.Default private String currency = "EUR";
    private Element form;

    public Optional<TransactionalAccount> toTinkAccount() {
        final AccountIdentifier accountIdentifier = getAccountIdentifier();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(
                        BalanceModule.of(RuralviaUtils.parseAmount(getBalance(), getCurrency())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountIdentifier.getIdentifier())
                                .withAccountNumber(accountIdentifier.getIdentifier())
                                .withAccountName(getAccountAlias())
                                .addIdentifier(accountIdentifier)
                                .build())
                .build();
    }

    protected AccountIdentifier getAccountIdentifier() {
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifierType.IBAN, getAccountNumber());
        identifier.setName(getAccountAlias());
        if (!identifier.isValid()) {
            throw new IllegalStateException("Found invalid account IBAN.");
        }
        return identifier;
    }
}
