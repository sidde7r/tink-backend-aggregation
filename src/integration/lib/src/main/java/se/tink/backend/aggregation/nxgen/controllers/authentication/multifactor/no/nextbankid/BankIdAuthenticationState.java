package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import lombok.Getter;
import lombok.Setter;

/**
 * Storage for values used throughout whole BankID controller to avoid passing them repeatedly in
 * method parameters.
 */
@Getter
@Setter
public class BankIdAuthenticationState {

    private BankIdIframeFirstWindow firstIframeWindow;
}
