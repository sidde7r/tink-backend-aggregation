package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.transactionalaccount.RuralviaTransactionalAccountFetcher.parseAmount;

import java.util.Optional;
import java.util.regex.Pattern;
import lombok.Data;
import org.jsoup.nodes.Element;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@Data
public class AccountEntity {

    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(?<value>[\\+\\-]?[0-9\\.,]+)(?<currency>€|EUROS|\\$|\\w{3})?");

    private String accountAlias;

    private String accountNumber;

    private String balance;

    private String currency;

    private Element form;

    public AccountEntity() {
        this.setCurrency("EUR");
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        final AccountIdentifier accountIdentifier = getAccountIdentifier();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withFlags()
                .withBalance(BalanceModule.of(parseAmount(getBalance(), getCurrency())))
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
