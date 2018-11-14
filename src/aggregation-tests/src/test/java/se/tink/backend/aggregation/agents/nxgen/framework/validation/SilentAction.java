package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.common.utils.Pair;
import se.tink.backend.common.utils.Triplet;

/** Keeps track of method calls. Useful for testing. */
public final class SilentAction implements Action {
    private final List<Pair<AisData, String>> onPassAisData = new ArrayList<>();
    private final List<Triplet<AisData, String, String>> onFailAisData = new ArrayList<>();
    private final List<Pair<Account, String>> onPassAccount = new ArrayList<>();
    private final List<Triplet<Account, String, String>> onFailAccount = new ArrayList<>();

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
}
