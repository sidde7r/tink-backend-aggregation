package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants;

public class ThirdPartyAppConstants {
    // This wait time is for the whole user authentication. Different banks have different
    // cumbersome authentication flows.
    // NOTE: This value is directly linked to the shutdown procedure of the aggregation-service.
    // Take *great* care when changing it, and validate the behaviour you want to achieve.
    public static final long WAIT_FOR_MINUTES = 9L;
}
