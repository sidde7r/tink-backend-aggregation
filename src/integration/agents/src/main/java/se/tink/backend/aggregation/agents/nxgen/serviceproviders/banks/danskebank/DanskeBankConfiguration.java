package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface DanskeBankConfiguration extends ClientConfiguration {
    String getAppVersionHeader();

    String getClientId();

    String getClientSecret();

    String getAppCulture();

    String getAppName();

    String getAppReferer();

    String getAppVersion();

    String getBrand();

    String getLanguageCode();

    String getMarketCode();

    boolean shouldAddXAppCultureHeader();

    List<String> getCheckingAccountTypes();

    List<String> getSavingsAccountTypes();

    default AccountCapabilities.Answer canMakeDomesticTransfer(String productCode) {
        return AccountCapabilities.Answer.UNKNOWN;
    }

    default AccountCapabilities.Answer canReceiveDomesticTransfer(String productCode) {
        return AccountCapabilities.Answer.UNKNOWN;
    }

    default AccountCapabilities.Answer canPlaceFunds(String productCode) {
        return AccountCapabilities.Answer.UNKNOWN;
    }

    default AccountCapabilities.Answer canWithdrawFunds(String productCode) {
        return AccountCapabilities.Answer.UNKNOWN;
    }

    Map<String, Loan.Type> getLoanAccountTypes();

    String getStepUpTokenKey();

    String getDeviceSerialNumberKey();

    String getSecuritySystem();

    Optional<String> getBindDeviceSecuritySystem();
}
