package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank;

public interface SwedbankBaseConfiguration {
    String getBIC();

    String getAuthenticationMethodId();

    String getBookingStatus();
}
