package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.*;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NovoBancoLoanAccountFetcher implements AccountFetcher<LoanAccount> {
    private final NovoBancoApiClient apiClient;

    public NovoBancoLoanAccountFetcher(NovoBancoApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<NovoBancoApiClient.LoanAggregatedData> loansResponseData = apiClient.getLoanAccounts();
        return loansResponseData.stream().map(this::toTinkAccount).collect(Collectors.toList());
    }

    private LoanAccount toTinkAccount(NovoBancoApiClient.LoanAggregatedData loanData) {
        String loanCurrency = getCurrency(loanData);
        LoanModule loanModule = LoanModule.builder()
                .withType(LoanDetails.Type.MORTGAGE)
                .withBalance(
                        ExactCurrencyAmount.of(getCurrentBalance(loanData, loanCurrency), loanCurrency))
                .withInterestRate(getInterestRate(loanData))
                .setInitialBalance(
                        ExactCurrencyAmount.of(getInitialBalance(loanData, loanCurrency), loanCurrency))
                .setInitialDate(getInitialDate(loanData))
                .setLoanNumber(loanData.getLoanContractId())
                .build();

        IdModule idModule = IdModule.builder()
                .withUniqueIdentifier(loanData.getLoanContractId())
                .withAccountNumber(loanData.getAccountDetails().getId())
                .withAccountName(loanData.getAccountDetails().getDesc())
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.COUNTRY_SPECIFIC,
                                loanData.getLoanContractId()))
                .setProductName(getProductName(loanData))
                .build();

        return LoanAccount.nxBuilder()
                .withLoanDetails(loanModule)
                .withId(idModule)
                .build();
    }

    private double getInterestRate(NovoBancoApiClient.LoanAggregatedData loanData) {
        String tanAsString = Optional.of(loanData)
                .map(NovoBancoApiClient.LoanAggregatedData::getLoanDetails)
                .map(GetLoanDetailsResponse::getBody)
                .map(GetLoanDetailsBodyEntity::getLoanDetails).map(LoanBodyDetailsEntity::getHeader)
                .map(LoanDetailsHeaderEntity::getInterestRate)
                .orElseThrow(() -> new IllegalStateException("Could not find: Interest Rate"));
        tanAsString = tanAsString.replaceAll("%", "").replace(',','.');
        return Math.abs(Double.parseDouble(tanAsString));
    }

    private String getCurrency(NovoBancoApiClient.LoanAggregatedData loanData) {
        return getPropertyValue(loanData, "Moeda", "Currency");
    }

    private String getProductName(NovoBancoApiClient.LoanAggregatedData loanData) {
        return getPropertyValue(loanData, "Designação", "Product Name");
    }

    private LocalDate getInitialDate(NovoBancoApiClient.LoanAggregatedData loanData) {
        String startDateAsString = getPropertyValue(loanData, "Data Início do contrato", "Initial Date");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return LocalDate.parse(startDateAsString, formatter);
    }

    private double getInitialBalance(NovoBancoApiClient.LoanAggregatedData loanData, String currency) {
        String initialBalanceAsString =
                getPropertyValue(loanData, "Capital utilizado", "Initial Balance");
        return parseValue(initialBalanceAsString.replaceAll(currency, ""));
    }

    private double getCurrentBalance(NovoBancoApiClient.LoanAggregatedData loanData, String currency) {
        String currentBalanceAsString = Optional.of(loanData)
                .map(NovoBancoApiClient.LoanAggregatedData::getLoanDetails)
                .map(GetLoanDetailsResponse::getBody)
                .map(GetLoanDetailsBodyEntity::getLoanDetails).map(LoanBodyDetailsEntity::getHeader)
                .map(LoanDetailsHeaderEntity::getCurrentBalance)
                .orElseThrow(() -> new IllegalStateException("Could not find: Current Balance"));

        return parseValue(currentBalanceAsString.replaceAll(currency, ""));
    }

    private double parseValue(String value) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "PT"));
        try {
            return nf.parse(value).doubleValue();
        } catch (ParseException e) {
            throw new IllegalStateException("Could not format value", e);
        }
    }

    private String getPropertyValue(NovoBancoApiClient.LoanAggregatedData loanData, String property, String label) {
        final int SECTION_TYPE = 30;
        final int COLLAPSIBLE_SECTION_TYPE = 32;
        return Optional.of(loanData)
                .map(NovoBancoApiClient.LoanAggregatedData::getLoanDetails)
                .map(GetLoanDetailsResponse::getBody)
                .map(GetLoanDetailsBodyEntity::getLoanDetails)
                .map(LoanBodyDetailsEntity::getLines)
                .map(Collection::stream).orElse(Stream.empty())
                .filter(loanLine -> loanLine.getT() == SECTION_TYPE)
                .findFirst()
                .map(LoanLinesEntity::getLines)
                .map(Collection::stream).orElse(Stream.empty())
                .filter(loanLine -> "Contrato".equals(loanLine.getL()) && loanLine.getT() == COLLAPSIBLE_SECTION_TYPE)
                .findFirst()
                .map(LoanLinesEntity::getLines)
                .map(Collection::stream).orElse(Stream.empty())
                .filter(loanLine -> property.equals(loanLine.getL()))
                .findFirst().orElseThrow(() -> new IllegalStateException("Could not find: " + label))
                .getV();
    }
}
