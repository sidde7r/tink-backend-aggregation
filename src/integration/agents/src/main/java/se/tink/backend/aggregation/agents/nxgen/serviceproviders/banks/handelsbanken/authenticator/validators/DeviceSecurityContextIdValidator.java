package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.validators;

import com.google.common.base.Strings;
import java.util.Date;
import java.util.function.Supplier;
import org.joda.time.LocalDate;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public abstract class DeviceSecurityContextIdValidator
        extends HandelsbankenValidator<BaseResponse> {
    private final Credentials credentials;

    public DeviceSecurityContextIdValidator(Credentials credentials, BaseResponse response) {
        super(response);
        this.credentials = credentials;
    }

    protected void validate(Supplier<RuntimeException> fallback) throws SessionException {
        String code = getCode();
        if (!Strings.isNullOrEmpty(code)) {
            // 100 - "Temporary error" -> Which means, in this state, that the `Device Security
            // Context Id` was wrong.
            // The user must re-activate their credential.
            if (HandelsbankenConstants.AutoAuthentication.Validation
                    .DEVICE_SECURITY_CONTEXT_ID_INVALID
                    .equals(code)) {
                LocalDate dateOfLoginChange = new LocalDate(2017, 6, 20);
                Date credentialUpdatedDate = credentials.getUpdated();
                if (credentialUpdatedDate == null
                        || !credentialUpdatedDate.after(dateOfLoginChange.toDate())) {
                    throw HandelsbankenConstants.AutoAuthentication.UserError
                            .DEVICE_SECURITY_CONTEXT_ID_INVALID
                            .exception();
                }
                throw fallback.get();
            }
        }
    }
}
