package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContract;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchTransactionsRequest {
    @JsonProperty("numIntContrato")
    private String contractNumber;

    @JsonProperty("cccBanco")
    private String bank;

    @JsonProperty("cccSucursal")
    private String office;

    @JsonProperty("cccDigitos")
    private String digits;

    @JsonProperty("cccSubcuenta")
    private String subAccount;

    @JsonProperty("cccCuenta")
    private String account;

    @JsonProperty("cccDigitosCtrol")
    private String checkDigits;

    @JsonProperty("fechaDesde")
    private String fetchFrom;

    @JsonProperty("fechaHasta")
    private String fetchTo;

    @JsonProperty("tipoMovEcrmvto2")
    private String movementType = BancoPopularConstants.Fetcher.MOVEMENT_TYPE;

    private int concepEcrmvto2 =
            BancoPopularConstants.Fetcher.CONCEP_ECRMVTO_2; // no clue what to translate to

    private FetchTransactionsRequest(
            BancoPopularContract contract,
            TransactionalAccount account,
            Date fromDate,
            Date toDate) {
        String accountNumber = extractAccountNumber(account.getAccountNumber());

        this.contractNumber = account.getBankIdentifier();
        this.bank = intToZeroFilledString(contract.getBank());
        this.office = intToZeroFilledString(contract.getOffice());
        this.digits = accountNumber.substring(0, 2);
        this.subAccount = accountNumber.substring(2, 5);
        this.account = accountNumber.substring(5, 10);
        this.checkDigits = accountNumber.substring(accountNumber.length() - 2);
        this.fetchFrom = formatDate(fromDate);
        this.fetchTo = formatDate(toDate);
        this.movementType = BancoPopularConstants.Fetcher.MOVEMENT_TYPE;
        this.concepEcrmvto2 = BancoPopularConstants.Fetcher.CONCEP_ECRMVTO_2;
    }

    public static FetchTransactionsRequest build(
            BancoPopularContract contract,
            TransactionalAccount account,
            Date fromDate,
            Date toDate) {

        return new FetchTransactionsRequest(contract, account, fromDate, toDate);
    }

    // account number is set to iban, so just use the 12 last characters in the number
    private String extractAccountNumber(String accountNumber) {
        String normalizedAccountNumber = accountNumber.replaceAll(" ", "");
        if (normalizedAccountNumber.length() > 12) {
            normalizedAccountNumber =
                    normalizedAccountNumber.substring(normalizedAccountNumber.length() - 12);
        }

        return normalizedAccountNumber;
    }

    private String intToZeroFilledString(int i) {
        return String.format("%04d", i);
    }

    private String formatDate(Date aDate) {
        LocalDate date = new java.sql.Date(aDate.getTime()).toLocalDate();
        return date.format(DateTimeFormatter.ISO_DATE);
    }
}
