package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account;

import com.google.common.base.Strings;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISPA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.builder.IdBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.GermanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@Slf4j
@RequiredArgsConstructor
public class FinTsTransactionalAccountMapper {

    private final HisalBalance hisalBalance;

    public Optional<TransactionalAccount> toTinkAccount(
            FinTsAccountInformation accountInformation) {
        HIUPD basicInfo = accountInformation.getBasicInfo();
        HISAL balance = accountInformation.getBalance();
        HISPA.Detail details = accountInformation.getSepaDetails();

        if (balance == null) {
            log.warn("Could not properly map transactional account due to missing balance");
            return Optional.empty();
        }

        BalanceModule balanceModule = hisalBalance.calculate(balance);
        String iban = getIBAN(basicInfo, details);
        String uniqueIdentifier = getUniqueIdentifier(basicInfo, iban);
        TransactionalAccountType accountType =
                getTransactionalAccountType(accountInformation.getAccountType());
        IdBuildStep idBuildStep = getIdModule(basicInfo, uniqueIdentifier, iban, details);

        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withoutFlags()
                .withBalance(balanceModule)
                .withId(idBuildStep.build())
                .addHolderName(basicInfo.getFirstAccountHolder())
                .addHolderName(basicInfo.getSecondAccountHolder())
                .build();
    }

    private TransactionalAccountType getTransactionalAccountType(AccountTypes accountType) {
        return TransactionalAccountType.from(accountType).orElse(null);
    }

    private IdBuildStep getIdModule(
            HIUPD basicInfo, String uniqueIdentifier, String iban, HISPA.Detail details) {
        IdBuildStep result =
                IdModule.builder()
                        .withUniqueIdentifier(uniqueIdentifier)
                        .withAccountNumber(basicInfo.getAccountNumber())
                        .withAccountName(basicInfo.getProductName())
                        .addIdentifier(
                                new GermanIdentifier(
                                        basicInfo.getBlz(), basicInfo.getAccountNumber()))
                        .setProductName(basicInfo.getProductName());

        if (iban != null) {
            result = result.addIdentifier(getIbanIdentifier(iban, details));
        }
        return result;
    }

    private IbanIdentifier getIbanIdentifier(String iban, HISPA.Detail details) {
        String bic = details != null ? details.getBic() : null;
        return new IbanIdentifier(bic, iban);
    }

    private String getUniqueIdentifier(HIUPD basicInfo, String iban) {
        return iban != null ? iban : basicInfo.getAccountNumber();
    }

    private String getIBAN(HIUPD basicInfo, HISPA.Detail details) {
        return details != null && !Strings.isNullOrEmpty(details.getIban())
                ? details.getIban()
                : basicInfo.getIban();
    }
}
