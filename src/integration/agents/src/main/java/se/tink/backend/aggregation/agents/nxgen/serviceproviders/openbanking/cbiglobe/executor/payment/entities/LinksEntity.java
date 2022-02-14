package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LinksEntity {
    // The link to the payment initiation resource, which needs to be updated by a PSU password and
    // eventually the PSU identification if not delivered yet. This is used in case of the Embedded
    // or Decoupled SCA approach.
    private Href updatePsuAuthenticationRedirect;

    private Href updatePsuAuthentication;

    // In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to redirect
    // the PSU browser.
    private Href scaRedirect;
}
