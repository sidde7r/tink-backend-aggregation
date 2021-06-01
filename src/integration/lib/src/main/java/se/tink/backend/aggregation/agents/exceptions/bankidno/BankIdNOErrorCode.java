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
    C325("C325", BankIdNOError.MOBILE_BANK_ID_TIMEOUT_OR_REJECTED),
    BID_14A4("BID-14a4", BankIdNOError.THIRD_PARTY_APP_BLOCKED),
    BID_14B1("BID-14b1", BankIdNOError.THIRD_PARTY_APP_TIMEOUT),
    BID_14B3("BID-14b3", BankIdNOError.THIRD_PARTY_APP_REJECTED),
    /*
    From our tests it seems that this error happens when OTP has some non-digits characters or is longer than 8.
    This should be verified in ITE-1369.
     */
    BID_1437("BID-1437", BankIdNOError.INVALID_ONE_TIME_CODE_FORMAT);

    private final String code;
    private final BankIdNOError error;
}
