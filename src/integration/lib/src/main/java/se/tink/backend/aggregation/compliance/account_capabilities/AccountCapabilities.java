package se.tink.backend.aggregation.compliance.account_capabilities;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AccountCapabilities {
    public enum Answer {
        YES,
        NO,
        UNKNOWN
    }

    @Builder.Default private Answer canWithdrawFunds = Answer.UNKNOWN;
    @Builder.Default private Answer canPlaceFunds = Answer.UNKNOWN;
    @Builder.Default private Answer canMakeAndReceiveTransfer = Answer.UNKNOWN;
}
