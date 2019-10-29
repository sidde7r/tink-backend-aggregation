package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.util.CurrencyMapper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderLoanAccountFetcher implements AccountFetcher<LoanAccount> {

    private final SantanderApiClient apiClient;
    private final CurrencyMapper currencyMapper;

    public SantanderLoanAccountFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
        this.currencyMapper = new CurrencyMapper();
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<Map<String, String>> businessData = apiClient.fetchLoans().getBusinessData();
        if(businessData == null){
            return Collections.emptyList();
        }
        return businessData.stream().map(this::toTinkAccount).collect(Collectors.toList());
    }

    private LoanAccount toTinkAccount(Map<String, String> loan) {
        String loanCurrency =
                currencyMapper.get(Integer.parseInt(loan.get("currency"))).getCurrencyCode();

        LocalDate startDate =
                LocalDate.parse(
                        loan.get("startDate"),
                        DateTimeFormatter.ofPattern(SantanderConstants.DATE_FORMAT));

        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(Type.DERIVE_FROM_NAME) // todo porownac z innymi kontami
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                new BigDecimal(loan.get("balance")), loanCurrency))
                                .withInterestRate(0) // not supported by mobile app
                                .setInitialBalance(
                                        ExactCurrencyAmount.of(loan.get("available"), loanCurrency))
                                .setInitialDate(startDate)
                                // .setNumMonthsBound()//liczba lat razy 12 miesiecy todo inny
                                // request
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loan.get("accountNumber"))
                                .withAccountNumber(loan.get("accountNumber"))
                                .withAccountName(loan.get("name"))
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                loan.get("accountNumber")))
                                .build())
                .build();
    }
}
