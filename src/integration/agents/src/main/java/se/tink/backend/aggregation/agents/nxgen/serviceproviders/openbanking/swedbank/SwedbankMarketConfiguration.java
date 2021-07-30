package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank;

public interface SwedbankMarketConfiguration {
    String getBIC();

    String getAuthenticationMethodId();

    String getBookingStatus();
}
