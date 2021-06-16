package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.core.account.AccountHolderType;

@Slf4j
public enum StarlingAccountHolderType {
    INDIVIDUAL(AccountHolderType.PERSONAL),
    JOINT(AccountHolderType.PERSONAL),
    SOLE_TRADER(AccountHolderType.BUSINESS),
    BUSINESS(AccountHolderType.BUSINESS),
    BANKING_AS_A_SERVICE(AccountHolderType.BUSINESS),
    UNKNOWN(AccountHolderType.UNKNOWN);

    private final AccountHolderType accountHolderType;

    StarlingAccountHolderType(AccountHolderType accountHolderType) {
        this.accountHolderType = accountHolderType;
    }

    @JsonCreator
    private static StarlingAccountHolderType fromString(String key) {
        try {
            return Optional.ofNullable(key)
                    .map(String::toUpperCase)
                    .map(StarlingAccountHolderType::valueOf)
                    .orElse(UNKNOWN);
        } catch (IllegalArgumentException e) {
            log.warn("`{}` is unmapped account holder type", key);
            return UNKNOWN;
        }
    }

    public AccountHolderType toTinkAccountHolderType() {
        return this.accountHolderType;
    }
}
