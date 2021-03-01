package src.libraries.connectivity_errors;

import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorType;

public class ErrorHelper {
    public static ConnectivityError from(ConnectivityErrorType type) {
        return ConnectivityError.newBuilder().setType(type).build();
    }
}
