package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.Transaction;

/** Throws an exception upon validation failure. */
public final class ThrowAction implements Action {
    @Override
    public void onPass(final AisData aisData, final String ruleIdentifier) {
        // noop
    }

    @Override
    public void onFail(final AisData aisData, final String ruleIdentifier, final String message) {
        throw new AssertionError(
                String.format(
                        "%s: %s for AIS data instance: %s", ruleIdentifier, message, aisData));
    }

    @Override
    public void onPass(final Account account, final String ruleIdentifier) {
        // noop
    }

    @Override
    public void onFail(final Account account, final String ruleIdentifier, final String message) {
        throw new AssertionError(
                String.format("%s: %s for account: %s", ruleIdentifier, message, account));
    }

    @Override
    public void onPass(final Transaction transaction, final String ruleIdentifier) {
        // noop
    }

    @Override
    public void onFail(
            final Transaction transaction, final String ruleIdentifier, final String message) {
        throw new AssertionError(
                String.format("%s: %s for transaction: %s", ruleIdentifier, message, transaction));
    }
}
