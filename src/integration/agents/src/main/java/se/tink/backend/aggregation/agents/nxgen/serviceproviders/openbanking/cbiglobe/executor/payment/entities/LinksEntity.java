package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {

    // The link to the payment initiation resource created by this request. This link can be used to
    // retrieve the resource data
    private Href self;

    // The link to the payment initiation resource, which needs to be updated by a PSU password and
    // eventually the PSU identification if not delivered yet. This is used in case of the Embedded
    // or Decoupled SCA approach.
    private Href updatePsuAuthenticationRedirect;

    private Href updatePsuAuthentication;

    // In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to redirect
    // the PSU browser.
    private Href scaRedirect;

    // This is a link to a resource, where the TPP can select the applicable strong customer
    // authentication methods for the PSU, if there were several available authentication methods.
    // This link contained under exactly the same conditions as the data element
    // “authenticationMethods
    private Href selectAuthenticationMethod;

    // The link to the payment initiation resource, where the “Payment Authorisation Request” is
    // sent to. This is the link to the resource which will authorise the payment by checking the
    // SCA authentication data within the Embedded SCA approach.
    private Href authoriseTransaction;

    // The link that the TPP has to follow in order to restart the payment flow after that the PSU
    // has confirmed the payment.
    private Href feePaymentConfirmation;

    // In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the configuration
    // of the Authorisation Server can be retrieved. The configuration follows the OAuth 2.0
    // Authorisation Server Metadata specification.
    private Href scaOAuth;
}
