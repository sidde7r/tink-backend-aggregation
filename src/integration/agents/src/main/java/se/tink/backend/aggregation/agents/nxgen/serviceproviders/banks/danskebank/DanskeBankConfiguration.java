package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

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

    default List<String> getDepotCashBalanceAccounts() {
        return Collections.emptyList();
    }

    default AccountCapabilities.Answer canExecuteExternalTransfer(String productCode) {
        return AccountCapabilities.Answer.UNINITIALIZED;
    }

    default AccountCapabilities.Answer canReceiveExternalTransfer(String productCode) {
        return AccountCapabilities.Answer.UNINITIALIZED;
    }

    default AccountCapabilities.Answer canPlaceFunds(String productCode) {
        return AccountCapabilities.Answer.UNINITIALIZED;
    }

    default AccountCapabilities.Answer canWithdrawCash(String productCode) {
        return AccountCapabilities.Answer.UNINITIALIZED;
    }

    Map<String, LoanDetails.Type> getLoanAccountTypes();

    String getStepUpTokenKey();

    String getDeviceSerialNumberKey();

    String getSecuritySystem();

    Optional<String> getBindDeviceSecuritySystem();
}
