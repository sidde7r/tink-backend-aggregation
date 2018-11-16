package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.common.utils.Pair;
import se.tink.backend.common.utils.Triplet;
import se.tink.backend.system.rpc.Transaction;

/** Keeps track of method calls. Useful for testing. */
public final class SilentAction implements Action {
    private final List<Pair<AisData, String>> onPassAisData = new ArrayList<>();
    private final List<Triplet<AisData, String, String>> onFailAisData = new ArrayList<>();
    private final List<Pair<Account, String>> onPassAccount = new ArrayList<>();
    private final List<Triplet<Account, String, String>> onFailAccount = new ArrayList<>();
    private final List<Pair<Transaction, String>> onPassTransaction = new ArrayList<>();
    private final List<Triplet<Transaction, String, String>> onFailTransaction = new ArrayList<>();

    @Override
    public void onPass(final AisData aisData, final String ruleIdentifier) {
        onPassAisData.add(new Pair<>(aisData, ruleIdentifier));
    }

    @Override
    public void onFail(final AisData aisData, final String ruleIdentifier, final String message) {
        onFailAisData.add(new Triplet<>(aisData, ruleIdentifier, message));
    }

    @Override
    public void onPass(final Account account, final String ruleIdentifier) {
        onPassAccount.add(new Pair<>(account, ruleIdentifier));
    }

    @Override
    public void onFail(final Account account, final String ruleIdentifier, final String message) {
        onFailAccount.add(new Triplet<>(account, ruleIdentifier, message));
    }

    @Override
    public void onPass(final Transaction transaction, final String ruleIdentifier) {
        onPassTransaction.add(new Pair<>(transaction, ruleIdentifier));
    }

    @Override
    public void onFail(final Transaction transaction, final String ruleIdentifier, final String message) {
        onFailTransaction.add(new Triplet<>(transaction, ruleIdentifier, message));
    }

    public List<Pair<AisData, String>> getOnPassAisData() {
        return onPassAisData;
    }

    public List<Triplet<AisData, String, String>> getOnFailAisData() {
        return onFailAisData;
    }

    public List<Pair<Account, String>> getOnPassAccount() {
        return onPassAccount;
    }

    public List<Triplet<Account, String, String>> getOnFailAccount() {
        return onFailAccount;
    }

    public List<Pair<Transaction, String>> getOnPassTransaction() {
        return onPassTransaction;
    }

    public List<Triplet<Transaction, String, String>> getOnFailTransaction() {
        return onFailTransaction;
    }

    public List<String> messages() {
        final List<String> msgs = new ArrayList<>();
        for (final Pair<AisData, String> pair : getOnPassAisData()) {
            msgs.add(String.format("[PASS] %s", pair.second));
        }
        for (final Triplet<AisData, String, String> pair : getOnFailAisData()) {
            msgs.add(String.format("[FAIL] %s", pair.second));
        }
        for (final Pair<Account, String> pair : getOnPassAccount()) {
            msgs.add(String.format("[PASS] %s", pair.second));
        }
        for (final Triplet<Account, String, String> pair : getOnFailAccount()) {
            msgs.add(String.format("[FAIL] %s", pair.second));
        }
        for (final Pair<Transaction, String> pair : getOnPassTransaction()) {
            msgs.add(String.format("[PASS] %s", pair.second));
        }
        for (final Triplet<Transaction, String, String> pair : getOnFailTransaction()) {
            msgs.add(String.format("[FAIL] %s", pair.second));
        }
        return msgs;
    }
}
