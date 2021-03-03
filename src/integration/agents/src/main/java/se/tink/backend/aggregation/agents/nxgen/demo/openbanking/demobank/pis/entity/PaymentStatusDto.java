package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.entity;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymentStatusDto {
    RCVD("Received"), /* Payment initiation has been received. */
    ACTC(
            "AcceptedTechnicalValidation"), /* Authentication and syntactical and semantical validation are successful. */
    ACSC(
            "AcceptedSettlementCompleted"), /* Settlement on the debtor's account has been completed. */
    RJCT("Rejected"),
    CANC("Cancelled"); /* Payment initiation has been cancelled before execution. */

    private final String fullName;

    public static PaymentStatusDto createFromFullName(String fullName) {
        return Arrays.stream(PaymentStatusDto.values())
                .filter(status -> status.getFullName().equals(fullName))
                .findAny()
                .orElseThrow(
                        () -> new IllegalArgumentException("Unknown payment status: " + fullName));
    }
}
