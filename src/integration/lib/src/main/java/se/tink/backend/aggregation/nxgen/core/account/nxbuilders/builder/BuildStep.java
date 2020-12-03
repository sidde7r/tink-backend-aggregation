package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.builder;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;
import se.tink.backend.aggregation.nxgen.core.account.Balance;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.enums.AccountFlag;

public interface BuildStep<A extends Account, B extends BuildStep<A, B>> {

    /**
     * Stores a value meant to represent an identifier of an account in the banks API. Typically
     * this is used to retrieve transactions for or more information about the account in later
     * steps.
     *
     * @param identifier The id of the account in the context of the banks API.
     * @return The next step of the builder.
     */
    B setApiIdentifier(@Nonnull String identifier);

    /**
     * Adds a name as a holder of the account. For shared accounts this method can be invoked
     * several times to add multiple holders. If the role of each holder can be specified use {@link
     * BuildStep#addHolders(Holder...)}
     *
     * @param holderName Name of the account holder.
     * @return The next step of the builder.
     */
    B addHolderName(@Nullable String holderName);

    /**
     * Adds multiple holders to the account. If this method is invoked several times all holders
     * specified in all calls will be added.
     *
     * @param holders list of holder entities to be added to the account
     * @return The next step of the builder.
     */
    B addHolders(@Nonnull List<Holder> holders);

    /**
     * Adds multiple holders to the account. If this method is invoked several times all holders
     * specified in all calls will be added.
     *
     * @param holders array of holder entities to be added to the account
     * @return The next step of the builder.
     */
    B addHolders(@Nonnull Holder... holders);

    /**
     * Set the type the account holder. {@link AccountHolderType}
     *
     * @param type type of the account holder.
     * @return The next step of the builder.
     */
    B setHolderType(@Nonnull AccountHolderType type);

    B addAccountFlags(@Nonnull AccountFlag... accountFlags);

    /**
     * Stores the value under the given key in temporary storage.
     *
     * @param key Key to store the value under.
     * @param value The value to be stored.
     * @return The final step of the builder.
     */
    <V> B putInTemporaryStorage(@Nonnull String key, @Nonnull V value);

    /**
     * Stores the value under the given key in the account payload.
     *
     * @param key Key to store the value under.
     * @param value The value to be stored.
     * @return The final step of the builder.
     */
    B putPayload(@Nonnull String key, @Nonnull String value);

    B setBankIdentifier(String number);

    // TODO: These should be made into a mandatory BuildStep:
    // TODO: https://tinkab.atlassian.net/browse/AGG-290
    B canWithdrawCash(AccountCapabilities.Answer canWithdrawCash);

    B canPlaceFunds(AccountCapabilities.Answer canPlaceFunds);

    B canExecuteExternalTransfer(AccountCapabilities.Answer canExecuteExternalTransfer);

    B canReceiveExternalTransfer(AccountCapabilities.Answer canReceiveExternalTransfer);

    B sourceInfo(AccountSourceInfo sourceInfo);

    B addBalances(@Nonnull List<Balance> balances);

    B addBalances(@Nonnull Balance... balances);
}
