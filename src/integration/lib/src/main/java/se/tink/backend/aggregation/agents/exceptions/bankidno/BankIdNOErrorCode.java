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
    BID_20A1("BID-20a1", BankIdNOError.INITIALIZATION_ERROR),
    BID_20B1("BID-20b1", BankIdNOError.MOBILE_BANK_ID_TIMEOUT_OR_REJECTED),
    BID_14A4("BID-14a4", BankIdNOError.BANK_ID_APP_BLOCKED),
    BID_14B1("BID-14b1", BankIdNOError.BANK_ID_APP_TIMEOUT),
    BID_14B3("BID-14b3", BankIdNOError.BANK_ID_APP_REJECTED);

    private final String code;
    private final BankIdNOError error;
}
