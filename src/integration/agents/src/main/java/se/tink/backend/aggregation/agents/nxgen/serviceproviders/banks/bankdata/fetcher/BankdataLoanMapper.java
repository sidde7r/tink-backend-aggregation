package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataMapperUtils.getAccountNumberToDisplay;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity.ACCOUNT_NUMBER_TEMP_STORAGE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity.REGISTRATION_NUMBER_TEMP_STORAGE_KEY;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankdataLoanMapper {

    public static List<LoanAccount> getLoanAccounts(GetAccountsResponse getAccountsResponse) {
        return getAccountsResponse.getAccounts().stream()
                .filter(BankdataLoanMapper::isLoanAccount)
                .map(BankdataLoanMapper::toLoanAccount)
                .collect(Collectors.toList());
    }

    private static boolean isLoanAccount(BankdataAccountEntity entity) {
        return entity.getAccountType() == AccountTypes.LOAN;
    }

    public static LoanAccount toLoanAccount(BankdataAccountEntity entity) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(LoanDetails.Type.DERIVE_FROM_NAME)
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                entity.getBalance(), entity.getCurrencyCode()))
                                .withInterestRate(0) // not supported by the app
                                .setInitialBalance(
                                        ExactCurrencyAmount.of(
                                                entity.getDrawingRight(), entity.getCurrencyCode()))
                                .setLoanNumber(entity.getAccountNo())
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(entity.getIban())
                                .withAccountNumber(
                                        getAccountNumberToDisplay(
                                                entity.getRegNo(), entity.getAccountNo()))
                                .withAccountName(entity.getName())
                                .addIdentifiers(
                                        new IbanIdentifier(entity.getBicSwift(), entity.getIban()),
                                        new BbanIdentifier(entity.getIban().substring(4)))
                                .setProductName(entity.getName())
                                .build())
                .addHolderName(entity.getAccountOwner())
                .putInTemporaryStorage(REGISTRATION_NUMBER_TEMP_STORAGE_KEY, entity.getRegNo())
                .putInTemporaryStorage(ACCOUNT_NUMBER_TEMP_STORAGE_KEY, entity.getAccountNo())
                .build();
    }
}
