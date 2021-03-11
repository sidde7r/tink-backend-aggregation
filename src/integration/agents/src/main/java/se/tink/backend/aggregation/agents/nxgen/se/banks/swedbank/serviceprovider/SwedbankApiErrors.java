package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorCode;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankApiErrors {
    private static boolean isSecurityTokenInvalidFormat(HttpResponseException hre) {
        // This method expects an response with the following characteristics:
        // - Http status: 400
        // - Http body: `ErrorResponse` with error field of "RESPONSE"

        HttpResponse httpResponse = hre.getResponse();
        if (httpResponse.getStatus() != HttpStatus.SC_BAD_REQUEST) {
            return false;
        }

        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        // The field which contains the error is called RESPONSE. If this field exists the security
        // token is invalid.
        return errorResponse.hasErrorField(SwedbankBaseConstants.ErrorField.RESPONSE);
    }

    private static boolean isSecurityTokenTooOld(HttpResponseException hre) {
        // This method expects an response with the following characteristics:
        // - Http status: 405
        // - Http body: `ErrorResponse` with error field of "NOT_ALLOWED"

        HttpResponse httpResponse = hre.getResponse();
        if (httpResponse.getStatus() != HttpStatus.SC_METHOD_NOT_ALLOWED) {
            return false;
        }

        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        return errorResponse.hasErrorCode(SwedbankBaseConstants.ErrorCode.NOT_ALLOWED);
    }

    static boolean isLoginSecurityTokenInvalid(HttpResponseException hre) {
        // This method expects an response with the following characteristics:
        // - Http status: 401
        // - Http body: `ErrorResponse` with error field of "LOGIN_FAILED"

        HttpResponse httpResponse = hre.getResponse();
        if (httpResponse.getStatus() != HttpStatus.SC_UNAUTHORIZED) {
            return false;
        }

        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        return errorResponse.hasErrorCode(SwedbankBaseConstants.ErrorCode.LOGIN_FAILED);
    }

    private static boolean isAuthorizationSecurityTokenInvalid(HttpResponseException hre) {
        // This method expects an response with the following characteristics:
        // - Http status: 401
        // - Http body: `ErrorResponse` with:
        //   * error code of "AUTHORIZATION_FAILED"
        //   * error message does not contain "Appen behöver uppdateras."

        HttpResponse httpResponse = hre.getResponse();
        if (httpResponse.getStatus() != HttpStatus.SC_UNAUTHORIZED) {
            return false;
        }

        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        return errorResponse.hasErrorCode(SwedbankBaseConstants.ErrorCode.AUTHORIZATION_FAILED)
                && !errorResponse.getAllErrors().contains(ErrorMessage.APP_NEEDS_UPDATE);
    }

    public static boolean isAppTooOld(HttpResponseException hre) {
        // This method expects an response with the following characteristics:
        // - Http status: 401
        // - Http body: `ErrorResponse` with:
        //   * error code of "AUTHORIZATION_FAILED"
        //   * error message contains "Appen behöver uppdateras."

        HttpResponse httpResponse = hre.getResponse();
        if (httpResponse.getStatus() != HttpStatus.SC_UNAUTHORIZED) {
            return false;
        }

        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        return errorResponse.hasErrorCode(SwedbankBaseConstants.ErrorCode.AUTHORIZATION_FAILED)
                && errorResponse.getAllErrors().contains(ErrorMessage.APP_NEEDS_UPDATE);
    }

    public static void handleTokenErrors(HttpResponseException hre)
            throws SupplementalInfoException {
        if (isSecurityTokenInvalidFormat(hre)) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(hre);
        }
        if (isSecurityTokenTooOld(hre)) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(hre);
        }
        if (isAuthorizationSecurityTokenInvalid(hre)) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(hre);
        }
    }

    public static boolean isUserNotACustomer(HttpResponseException hre) {
        // This method expects an response with the following characteristics:
        // - Http status: 404
        // - Http body: `ErrorResponse` with `general` error code of "NOT_FOUND"

        HttpResponse httpResponse = hre.getResponse();
        if (httpResponse.getStatus() != HttpStatus.SC_NOT_FOUND) {
            return false;
        }

        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        return errorResponse.hasErrorCode(SwedbankBaseConstants.ErrorCode.NOT_FOUND);
    }

    public static boolean isAccountNumberInvalid(HttpResponseException hre) {
        // This method expects an response with the following characteristics:
        // - Http status: 400
        // - Http body: `ErrorResponse` with error field of "RECIPIENT_NUMBER"

        HttpResponse httpResponse = hre.getResponse();
        if (httpResponse.getStatus() != HttpStatus.SC_BAD_REQUEST) {
            return false;
        }

        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        return errorResponse.hasErrorField(SwedbankBaseConstants.ErrorField.RECIPIENT_NUMBER);
    }

    public static boolean isTransferAlreadyExists(HttpResponseException hre) {
        // This method expects an response with the following characteristics:
        // - Http status: 400
        // - Http body: `ErrorResponse` with error code of "TRANSFER_ALREADY_EXISTS"

        HttpResponse httpResponse = hre.getResponse();
        if (httpResponse.getStatus() != HttpStatus.SC_BAD_REQUEST) {
            return false;
        }

        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        return errorResponse.hasErrorCode(ErrorCode.TRANSFER_ALREADY_EXISTS)
                || errorResponse.hasErrorCode(ErrorCode.PAYMENT_ALREADY_EXISTS);
    }

    public static boolean isSessionTerminated(HttpResponseException hre) {
        // This method expects an response with the following characteristics:
        // - Http status: 401
        // - Http body: `ErrorResponse` with error field of "STRONGER_AUTHENTICATION_NEEDED"
        // - Not for an identification request

        if (hre.getRequest().getURI().getPath().contains(Url.IDENTIFICATION)) {
            return false;
        }

        HttpResponse httpResponse = hre.getResponse();
        if (httpResponse.getStatus() != HttpStatus.SC_UNAUTHORIZED) {
            return false;
        }

        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        return errorResponse.hasErrorCode(
                SwedbankBaseConstants.ErrorCode.STRONGER_AUTHENTICATION_NEEDED);
    }
}
