package se.tink.backend.aggregation.nxgen.core.account;

import static se.tink.backend.aggregation.nxgen.core.account.Account.BANK_IDENTIFIER_KEY;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.BuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithBalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.enums.AccountFlag;

public abstract class AccountBuilder<A extends Account, B extends BuildStep<A, B>>
        implements WithIdStep<B>, WithBalanceStep<B>, BuildStep<A, B> {

    private IdModule idModule;
    private BalanceModule balanceModule;
    private String apiIdentifier;
    private final List<HolderName> holderNames = new ArrayList<>();
    private final List<AccountFlag> accountFlags = new ArrayList<>();
    private final TemporaryStorage temporaryStorage = new TemporaryStorage();

    protected abstract B buildStep();

    @Override
    public WithBalanceStep<B> withId(IdModule id) {
        Preconditions.checkNotNull(id, "Id Module must not be null.");
        this.idModule = id;
        return this;
    }

    @Override
    public B withBalance(BalanceModule balance) {
        Preconditions.checkNotNull(balance, "Balance Module must not be null.");
        this.balanceModule = balance;
        return buildStep();
    }

    @Override
    public B setApiIdentifier(@Nonnull String identifier) {
        this.apiIdentifier = identifier;
        return buildStep();
    }

    @Override
    public B addHolderName(@Nonnull String holderName) {
        // Preconditions.checkNotNull(holderName, "Holder name must not be null.");
        holderNames.add(new HolderName(holderName));
        return buildStep();
    }

    @Override
    public B addAccountFlags(@Nonnull AccountFlag... accountFlags) {
        Preconditions.checkNotNull(accountFlags, "Flag list must not be null.");
        this.accountFlags.addAll(Arrays.asList(accountFlags));
        return buildStep();
    }

    @Override
    public <V> B putInTemporaryStorage(@Nonnull String key, @Nonnull V value) {
        temporaryStorage.put(key, value);
        return buildStep();
    }

    public B setBankIdentifier(String bankIdentifier) {
        temporaryStorage.put(BANK_IDENTIFIER_KEY, bankIdentifier);
        return buildStep();
    }

    private String getBankIdentifier() {
        return temporaryStorage.get(BANK_IDENTIFIER_KEY);
    }

    public IdModule getIdModule() {
        return idModule;
    }

    public BalanceModule getBalanceModule() {
        return balanceModule;
    }

    public String getApiIdentifier() {
        return apiIdentifier;
    }

    public List<HolderName> getHolderNames() {
        return holderNames;
    }

    public List<AccountFlag> getAccountFlags() {
        return accountFlags;
    }

    public TemporaryStorage getTransientStorage() {
        return temporaryStorage;
    }
}
