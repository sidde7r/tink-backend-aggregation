package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.validators;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;

public class BankIdValidator {
    public static void validate(String bankId) {
        Preconditions.checkState(
                HandelsbankenConstants.BANKID_PATTERN
                        .matcher(Preconditions.checkNotNull(bankId))
                        .matches(),
                "Unexpected account.bankid '%s'. Reformatted?",
                bankId);
    }
}
