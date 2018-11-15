package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import se.tink.backend.aggregation.rpc.Account;

/** Action to take if validation fails. */
public interface Action {
    /**
     * Executes an action when a piece of AIS data satisfies a rule.
     *
     * @param aisData The piece of AIS data which satisfied a rule
     * @param ruleIdentifier A human-readable identifier for the rule that the account satisfied
     */
    void onPass(AisData aisData, String ruleIdentifier);

    /**
     * Executes an action when a piece of AIS data does not satisfy a rule.
     *
     * @param aisData The piece of AIS data which did not satisfy a rule
     * @param ruleIdentifier A human-readable identifier for the rule that the AIS data did not
     *     satisfy
     * @param message A human-readable message explaining why the rule failed in the context of this
     *     AIS data
     */
    void onFail(AisData aisData, String ruleIdentifier, String message);

    /**
     * Executes an action when an account satisfies a rule.
     *
     * @param account The account which satisfied a rule
     * @param ruleIdentifier A human-readable identifier for the rule that the account satisfied
     */
    void onPass(Account account, String ruleIdentifier);

    /**
     * Executes an action when an account does not satisfy a rule.
     *
     * @param account The account which did not satisfy a rule
     * @param ruleIdentifier A human-readable identifier for the rule that the account did not
     *     satisfy
     * @param message A human-readable message explaining why the rule failed in the context of this
     *     account
     */
    void onFail(Account account, String ruleIdentifier, String message);
}
