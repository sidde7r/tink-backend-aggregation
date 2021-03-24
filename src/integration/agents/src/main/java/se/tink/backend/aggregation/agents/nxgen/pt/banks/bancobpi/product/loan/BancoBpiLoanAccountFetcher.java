package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.loan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.BancoBpiProductType;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoBpiLoanAccountFetcher implements AccountFetcher<LoanAccount> {

    private BancoBpiClientApi clientApi;

    public BancoBpiLoanAccountFetcher(BancoBpiClientApi clientApi) {
        this.clientApi = clientApi;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            List<BancoBpiProductData> loans =
                    clientApi.getProductsByType(BancoBpiProductType.getLoanProductTypes());
            return mapToLoanAccount(loans);
        } catch (RequestException e) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(e.getMessage());
        }
    }

    private List<LoanAccount> mapToLoanAccount(List<BancoBpiProductData> loans) {
        List<LoanAccount> loanAccounts = new ArrayList<>(loans.size());
        for (BancoBpiProductData loan : loans) {
            loanAccounts.add(
                    LoanAccount.nxBuilder()
                            .withLoanDetails(
                                    LoanModule.builder()
                                            .withType(mapToLoanType(loan))
                                            .withBalance(
                                                    ExactCurrencyAmount.of(
                                                            loan.getBalance(),
                                                            loan.getCurrencyCode()))
                                            .withInterestRate(0) // not supported by mobile app
                                            .setInitialBalance(
                                                    ExactCurrencyAmount.of(
                                                            loan.getInitialBalance(),
                                                            loan.getCurrencyCode()))
                                            .setInitialDate(loan.getInitialDate())
                                            .build())
                            .withId(
                                    IdModule.builder()
                                            .withUniqueIdentifier(loan.getNumber())
                                            .withAccountNumber(loan.getNumber())
                                            .withAccountName(loan.getName())
                                            .addIdentifier(
                                                    AccountIdentifier.create(
                                                            AccountIdentifierType.PT_BPI,
                                                            loan.getNumber()))
                                            .build())
                            .build());
        }
        return loanAccounts;
    }

    private LoanDetails.Type mapToLoanType(BancoBpiProductData data) {
        return BancoBpiProductType.getByCode(data.getCodeAlfa()).getDomainLoanType();
    }
}
