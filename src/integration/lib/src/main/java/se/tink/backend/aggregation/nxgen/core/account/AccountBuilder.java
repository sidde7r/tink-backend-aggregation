package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.BuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.account.enums.AccountFlag;

public abstract class AccountBuilder<A extends Account, B extends BuildStep<A, B>>
        implements WithIdStep<B>, BuildStep<A, B> {

    private IdModule idModule;
    private String apiIdentifier;
    private final List<HolderName> holderNames = new ArrayList<>();
    protected final List<AccountFlag> accountFlags = new ArrayList<>();
    private final TemporaryStorage temporaryStorage = new TemporaryStorage();
    protected Map<String, String> payload = new HashMap<>();

    protected abstract B buildStep();

    @Override
    public B withId(@Nonnull IdModule id) {
        Preconditions.checkNotNull(id, "Id Module must not be null.");
        this.idModule = id;
        return buildStep();
    }

    @Override
    public B setApiIdentifier(@Nonnull String identifier) {
        Preconditions.checkNotNull(identifier, "API Identifier name must not be null.");
        this.apiIdentifier = identifier;
        return buildStep();
    }

    @Override
    public B addHolderName(@Nullable String holderName) {
        if (holderName != null) {
            holderNames.add(new HolderName(holderName));
        }

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

    @Override
    public B setBankIdentifier(String bankIdentifier) {
        temporaryStorage.put(Account.BANK_IDENTIFIER_KEY, bankIdentifier);
        return buildStep();
    }

    IdModule getIdModule() {
        return idModule;
    }

    String getApiIdentifier() {
        return apiIdentifier;
    }

    List<HolderName> getHolderNames() {
        return ImmutableList.copyOf(holderNames);
    }

    List<AccountFlag> getAccountFlags() {
        return ImmutableList.copyOf(accountFlags);
    }

    TemporaryStorage getTransientStorage() {
        return temporaryStorage;
    }

    public B putPayload(@Nonnull String key, @Nonnull String value) {
        payload.put(key, value);
        return buildStep();
    }

    public Map<String, String> getPayload() {
        return payload;
    }
}
