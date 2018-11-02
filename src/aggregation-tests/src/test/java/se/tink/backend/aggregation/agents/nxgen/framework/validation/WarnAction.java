package se.tink.backend.aggregation.agents.nxgen.framework.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WarnAction implements Action {
    private static final Logger logger = LoggerFactory.getLogger(WarnAction.class);

    @Override
    public void onPass(final AisData aisData, final String ruleIdentifier) {
        logger.info(
                "[PASS] {} for AIS data instance: {}",
                ruleIdentifier,
                aisData);
    }

    @Override
    public void onFail(final AisData aisData, final String ruleIdentifier, final String message) {
        logger.warn(
                "[FAIL] {}: {} for AIS data instance: {}",
                ruleIdentifier,
                message,
                aisData);
    }
}
