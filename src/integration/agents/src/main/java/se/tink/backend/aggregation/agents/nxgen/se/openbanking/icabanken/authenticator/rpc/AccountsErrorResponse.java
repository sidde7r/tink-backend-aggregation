package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsErrorResponse {
    private String title;
    private String detail;
    private String code;

    public boolean isOldKycInfoError() {
        return IcaBankenConstants.ErrorTypes.RESOURCE_BLOCKED.equalsIgnoreCase(code)
                && IcaBankenConstants.ErrorMessages.OLD_KYC_INFO.equalsIgnoreCase(detail);
    }

    public boolean isNoAccountInformation() {
        return IcaBankenConstants.ErrorTypes.RESOURCE_UNKNOWN.equalsIgnoreCase(code)
                && IcaBankenConstants.ErrorMessages.NO_ACCOUNT_INFO.equalsIgnoreCase(detail);
    }
}
