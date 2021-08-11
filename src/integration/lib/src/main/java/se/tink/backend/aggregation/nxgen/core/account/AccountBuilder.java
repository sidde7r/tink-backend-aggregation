package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.BuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder.WithIdStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.enums.AccountFlag;

public abstract class AccountBuilder<A extends Account, B extends BuildStep<A, B>>
        implements WithIdStep<B>, BuildStep<A, B> {

    private IdModule idModule;
    private String apiIdentifier;
    private final List<Party> parties = new ArrayList<>();
    protected final List<AccountFlag> accountFlags = new ArrayList<>();
    private final TemporaryStorage temporaryStorage = new TemporaryStorage();
    protected Map<String, String> payload = new HashMap<>();
    private AccountCapabilities capabilities = AccountCapabilities.createDefault();
    private AccountSourceInfo sourceInfo;
    private AccountHolderType holderType;
    private final List<Balance> balances = new ArrayList<>();

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
        if (!Strings.isNullOrEmpty(holderName)) {
            parties.add(new Party(holderName, Party.Role.HOLDER));
        }
        return buildStep();
    }

    @Override
    public B addParties(@Nullable Party... parties) {
        addParties(Arrays.asList(parties));
        return buildStep();
    }

    @Override
    public B addParties(@Nullable List<Party> parties) {
        if (parties == null) {
            return buildStep();
        }
        // When removed writes to account information service should parties be used as term
        // not only in the agents but also in aggregation-service request to aggregation controller
        parties.stream()
                .filter(
                        party ->
                                party != null && party.getName() != null && party.getRole() != null)
                .forEach(party -> this.parties.add(party));
        return buildStep();
    }

    @Override
    public B setHolderType(@Nonnull AccountHolderType holderType) {
        Preconditions.checkNotNull(holderType, "holder type must not be null.");
        this.holderType = holderType;
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

    @Override
    public B canWithdrawCash(AccountCapabilities.Answer canWithdrawCash) {
        this.capabilities.setCanWithdrawCash(canWithdrawCash);
        return buildStep();
    }

    @Override
    public B canPlaceFunds(AccountCapabilities.Answer canPlaceFunds) {
        this.capabilities.setCanPlaceFunds(canPlaceFunds);
        return buildStep();
    }

    @Override
    public B canExecuteExternalTransfer(AccountCapabilities.Answer canExecuteExternalTransfer) {
        this.capabilities.setCanExecuteExternalTransfer(canExecuteExternalTransfer);
        return buildStep();
    }

    @Override
    public B canReceiveExternalTransfer(AccountCapabilities.Answer canReceiveExternalTransfer) {
        this.capabilities.setCanReceiveExternalTransfer(canReceiveExternalTransfer);
        return buildStep();
    }

    @Override
    public B sourceInfo(AccountSourceInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
        return buildStep();
    }

    @Override
    public B addBalances(@Nonnull List<Balance> balances) {
        Preconditions.checkNotNull(balances, "balances list must not be null.");

        this.balances.addAll(balances);
        return buildStep();
    }

    @Override
    public B addBalances(@Nonnull Balance... balances) {
        Preconditions.checkNotNull(balances, "balances Array must not be null.");
        return this.addBalances(Arrays.asList(balances));
    }

    IdModule getIdModule() {
        return idModule;
    }

    String getApiIdentifier() {
        return apiIdentifier;
    }

    List<Party> getParties() {
        return ImmutableList.copyOf(parties);
    }

    AccountHolderType getHolderType() {
        return holderType;
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

    public AccountCapabilities getCapabilities() {
        return capabilities;
    }

    public AccountSourceInfo getSourceInfo() {
        return sourceInfo;
    }

    public List<Balance> getBalances() {
        return balances;
    }
}
