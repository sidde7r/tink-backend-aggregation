package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.DateFormats.DD_MM_YYYY;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.COLLAPSIBLE_SECTION_TYPE;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.CONTRACT;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.CURRENCY_EN;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.CURRENCY_PT;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.INITIAL_BALANCE_EN;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.INITIAL_BALANCE_PT;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.INITIAL_DATE_EN;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.INITIAL_DATE_PT;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.PRODUCT_NAME_EN;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.PRODUCT_NAME_PT;
import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.SECTION_TYPE;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic.DetailLineEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.GetLoanDetailsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.LoanBodyDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.loan.LoanDetailsHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LoanAccountMapper {

    public static LoanAccount mapToTinkAccount(NovoBancoApiClient.LoanAggregatedData loanData) {
        String loanCurrency = getCurrency(loanData);
        LoanModule loanModule =
                LoanModule.builder()
                        .withType(LoanDetails.Type.MORTGAGE)
                        .withBalance(
                                ExactCurrencyAmount.of(
                                        getCurrentBalance(loanData, loanCurrency), loanCurrency))
                        .withInterestRate(getInterestRate(loanData))
                        .setInitialBalance(
                                ExactCurrencyAmount.of(
                                        getInitialBalance(loanData, loanCurrency), loanCurrency))
                        .setInitialDate(getInitialDate(loanData))
                        .setLoanNumber(loanData.getLoanContractId())
                        .build();

        AccountDetailsEntity accountDetails = loanData.getAccountDetails();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(loanData.getLoanContractId())
                        .withAccountNumber(accountDetails.getId())
                        .withAccountName(accountDetails.getDesc())
                        .addIdentifier(
                                AccountIdentifierProvider.getAccountIdentifier(
                                        accountDetails.getIban(), accountDetails.getId()))
                        .setProductName(getProductName(loanData))
                        .build();

        return LoanAccount.nxBuilder().withLoanDetails(loanModule).withId(idModule).build();
    }

    private static double getInterestRate(NovoBancoApiClient.LoanAggregatedData loanData) {
        String tanAsString =
                Optional.of(loanData)
                        .map(NovoBancoApiClient.LoanAggregatedData::getLoanDetails)
                        .map(GetLoanDetailsResponse::getBody)
                        .map(GetLoanDetailsBodyEntity::getLoanDetails)
                        .map(LoanBodyDetailsEntity::getHeader)
                        .map(LoanDetailsHeaderEntity::getInterestRate)
                        .orElseThrow(
                                () -> new IllegalStateException("Could not find: Interest Rate"));
        tanAsString = tanAsString.replaceAll("%", "").replace(',', '.');
        return Math.abs(Double.parseDouble(tanAsString));
    }

    private static String getCurrency(NovoBancoApiClient.LoanAggregatedData loanData) {
        return getPropertyValue(loanData, CURRENCY_PT, CURRENCY_EN);
    }

    private static String getProductName(NovoBancoApiClient.LoanAggregatedData loanData) {
        return getPropertyValue(loanData, PRODUCT_NAME_PT, PRODUCT_NAME_EN);
    }

    private static LocalDate getInitialDate(NovoBancoApiClient.LoanAggregatedData loanData) {
        String startDateAsString = getPropertyValue(loanData, INITIAL_DATE_PT, INITIAL_DATE_EN);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MM_YYYY);
        return LocalDate.parse(startDateAsString, formatter);
    }

    private static double getInitialBalance(
            NovoBancoApiClient.LoanAggregatedData loanData, String currency) {
        String initialBalanceAsString =
                getPropertyValue(loanData, INITIAL_BALANCE_PT, INITIAL_BALANCE_EN);
        return parseValue(initialBalanceAsString.replaceAll(currency, ""));
    }

    private static double getCurrentBalance(
            NovoBancoApiClient.LoanAggregatedData loanData, String currency) {
        String currentBalanceAsString =
                Optional.of(loanData)
                        .map(NovoBancoApiClient.LoanAggregatedData::getLoanDetails)
                        .map(GetLoanDetailsResponse::getBody)
                        .map(GetLoanDetailsBodyEntity::getLoanDetails)
                        .map(LoanBodyDetailsEntity::getHeader)
                        .map(LoanDetailsHeaderEntity::getCurrentBalance)
                        .orElseThrow(
                                () -> new IllegalStateException("Could not find: Current Balance"));

        return parseValue(currentBalanceAsString.replaceAll(currency, ""));
    }

    private static double parseValue(String value) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "PT"));
        try {
            return nf.parse(value).doubleValue();
        } catch (ParseException e) {
            throw new IllegalStateException("Could not format value", e);
        }
    }

    private static String getPropertyValue(
            NovoBancoApiClient.LoanAggregatedData loanData, String property, String label) {

        return Optional.of(loanData)
                .map(NovoBancoApiClient.LoanAggregatedData::getLoanDetails)
                .map(GetLoanDetailsResponse::getBody)
                .map(GetLoanDetailsBodyEntity::getLoanDetails)
                .map(LoanBodyDetailsEntity::getLines)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(loanLine -> SECTION_TYPE.equals(loanLine.getType()))
                .findFirst()
                .map(DetailLineEntity::getLines)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(
                        loanLine ->
                                CONTRACT.equals(loanLine.getLabel())
                                        && COLLAPSIBLE_SECTION_TYPE.equals(loanLine.getType()))
                .findFirst()
                .map(DetailLineEntity::getLines)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(loanLine -> property.equals(loanLine.getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find: " + label))
                .getValue();
    }
}
