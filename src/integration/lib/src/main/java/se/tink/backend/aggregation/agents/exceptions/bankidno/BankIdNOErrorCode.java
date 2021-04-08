package se.tink.backend.aggregation.agents.exceptions.bankidno;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * These are the error codes that we expect to see on a screen when something goes wrong. The full
 * list of all possible errors can be found on BankID confluence page:
 * https://confluence.bankidnorge.no/confluence/kiev-open/bankid-error-codes.
 */
@Getter
@RequiredArgsConstructor
public enum BankIdNOErrorCode {
    BID_20A1("BID-20a1", BankIdNOError.INITIALIZATION_ERROR);

    private final String code;
    private final BankIdNOError error;
}
