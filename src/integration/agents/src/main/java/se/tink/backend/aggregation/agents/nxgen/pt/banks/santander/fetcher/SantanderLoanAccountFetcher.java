package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Loan.ACCOUNT_NUMBER;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Loan.AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Loan.BALANCE;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Loan.CURRENCY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Loan.LOAN_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Loan.NAME;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Loan.START_DATE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.client.SantanderApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SantanderLoanAccountFetcher implements AccountFetcher<LoanAccount> {

    private static final Logger log = LoggerFactory.getLogger(SantanderLoanAccountFetcher.class);

    private static final GenericTypeMapper<Type, String> loanTypes =
            GenericTypeMapper.<Type, String>genericBuilder()
                    .put(Type.MORTGAGE, "096HG0") // Habitação
                    .put(Type.MORTGAGE, "096HG2") // Empréstimo da Casa
                    .put(Type.CREDIT, "096MF0") // Habitação Multifunções
                    .build();

    private final SantanderApiClient apiClient;
    private final CurrencyMapper currencyMapper;

    public SantanderLoanAccountFetcher(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
        this.currencyMapper = new CurrencyMapper();
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return Optional.ofNullable(
                        (List<Map<String, String>>) apiClient.fetchLoans().getBusinessData())
                .orElse(Collections.emptyList()).stream()
                .map(this::toTinkAccount)
                .collect(Collectors.toList());
    }

    private LoanAccount toTinkAccount(Map<String, String> loan) {
        String loanCurrency =
                currencyMapper.get(Integer.parseInt(loan.get(CURRENCY))).getCurrencyCode();

        LocalDate startDate =
                LocalDate.parse(loan.get(START_DATE), SantanderConstants.DATE_FORMATTER);

        String loanProductName = loan.get(NAME);
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(mapLoanType(loan.get(LOAN_TYPE), loanProductName))
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                new BigDecimal(loan.get(BALANCE)), loanCurrency))
                                .withInterestRate(0) // not supported by mobile app
                                .setInitialBalance(
                                        ExactCurrencyAmount.of(loan.get(AVAILABLE), loanCurrency))
                                .setInitialDate(startDate)
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loan.get(ACCOUNT_NUMBER))
                                .withAccountNumber(loan.get(ACCOUNT_NUMBER))
                                .withAccountName(loanProductName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.IBAN,
                                                loan.get(ACCOUNT_NUMBER)))
                                .build())
                .build();
    }

    private Type mapLoanType(String loanProductType, String loanProductName) {
        Optional<Type> type = loanTypes.translate(loanProductType);

        if (type.isPresent()) {
            return type.get();
        } else {
            log.warn(
                    "Unknown loan product type: {} with name {}. Loan will be mapped as OTHER.",
                    loanProductType,
                    loanProductName);
            return Type.OTHER;
        }
    }
}
