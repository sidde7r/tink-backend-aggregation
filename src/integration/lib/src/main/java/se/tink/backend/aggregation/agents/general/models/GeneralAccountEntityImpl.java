package se.tink.backend.aggregation.agents.general.models;

import java.util.Optional;
import org.assertj.core.util.Preconditions;
import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.account.AccountIdentifier;

public class GeneralAccountEntityImpl implements GeneralAccountEntity {
    private static final String EMPTY_BANK_NAME = "";

    private final String bankName;
    private final String accountName;
    private final AccountIdentifier accountIdentifier;

    private GeneralAccountEntityImpl(String bankName, String accountName,
            AccountIdentifier accountIdentifier) {
        this.bankName = bankName;
        this.accountName = accountName;
        this.accountIdentifier = accountIdentifier;
    }

    public static Optional<GeneralAccountEntityImpl> createFromCoreAccount(Account coreAccount) {

        Preconditions.checkNotNull(coreAccount, "Account cannot be null.");

        Optional<AccountIdentifier> accountIdentifier = coreAccount.getIdentifiers().stream().findFirst();
        if (!accountIdentifier.isPresent()) {
            return Optional.empty();
        }

        String accountName = coreAccount.getName();
        return Optional.of(new GeneralAccountEntityImpl(EMPTY_BANK_NAME, accountName, accountIdentifier.get()));
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return accountIdentifier;
    }

    @Override
    public String generalGetBank() {
        return bankName;
    }

    @Override
    public String generalGetName() {
        return accountName;
    }
}
