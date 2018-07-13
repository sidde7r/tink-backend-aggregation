package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankDeserializer;

public class FinalizeAuthenticationRequest {
    @JsonIgnore
    private static final String BANK_ID_USER_ID = "\"\"";
    private final String userId;
    @JsonProperty("LogonPackage")
    private final String logonPackage;

    private FinalizeAuthenticationRequest(String logonPackage) {
        this(BANK_ID_USER_ID, logonPackage);
    }

    private FinalizeAuthenticationRequest(String userId, String logonPackage) {
        this.userId = userId;
        this.logonPackage = logonPackage;
    }

    public static FinalizeAuthenticationRequest createForBankId(String logonPackage) {
        return new FinalizeAuthenticationRequest(logonPackage);
    }

    public static FinalizeAuthenticationRequest createForServiceCode(String logonInfo) {
        LogonPackageEntity logonPackageEntity =
                DanskeBankDeserializer.convertStringToObject(logonInfo, LogonPackageEntity.class);

        return new FinalizeAuthenticationRequest(logonPackageEntity.getUserId(), logonPackageEntity.getLogonPackage());
    }

    public String getUserId() {
        return userId;
    }

    public String getLogonPackage() {
        return logonPackage;
    }
}
