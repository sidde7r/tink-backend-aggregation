package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.system.rpc.Transaction;

public final class InformativeAction implements Action {
    private static final Logger logger = LoggerFactory.getLogger(InformativeAction.class);

    @Override
    public void onPass(final AisData aisData, final String ruleIdentifier) {
        logger.info(
                "Validator result:\n[PASS] {}", ruleIdentifier);
    }

    @Override
    public void onFail(final AisData aisData, final String ruleIdentifier, final String message) {
        logger.warn(
                "Validator result:\n[FAIL] {}: {} for AIS data instance: {}",
                ruleIdentifier,
                message,
                aisData);
    }

    @Override
    public void onPass(final Account account, final String ruleIdentifier) {
        logger.info("Validator result:\n[PASS] {}", ruleIdentifier);
    }

    @Override
    public void onFail(final Account account, final String ruleIdentifier, final String message) {
        logger.warn(
                "Validator result:\n[FAIL] {}: {} for account: {}",
                ruleIdentifier,
                message,
                account);
    }

    @Override
    public void onPass(final Transaction transaction, final String ruleIdentifier) {
        logger.info("Validator result:\n[PASS] {}", ruleIdentifier);
    }

    @Override
    public void onFail(final Transaction transaction, final String ruleIdentifier, final String message) {
        logger.warn(
                "Validator result:\n[FAIL] {}: {} for transaction: {}",
                ruleIdentifier,
                message,
                transaction);
    }
}
