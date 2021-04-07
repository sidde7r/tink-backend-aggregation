package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * These are the error codes that we expect to see on a screen when something goes wrong. This list
 * should grow each time we find a new meaningful error code that is worth handling.
 */
@Getter
@RequiredArgsConstructor
public enum BankIdNOErrorCode {
    BID_20A1("BID-20a1", BankIdNOError.INITIALIZATION_ERROR);

    private final String code;
    private final BankIdNOError error;
}
