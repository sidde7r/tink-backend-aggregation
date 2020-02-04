package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.common.RequestException;

public class ProductDetailsResponse {

    private DateTimeFormatter DAY_MONTH_YEAR_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private LocalDate initialDate;
    private LocalDate finalDate;
    private BigDecimal initialBalance;
    private String owner;

    ProductDetailsResponse(String rawResponse) throws RequestException {
        parseResponse(rawResponse);
    }

    private void parseResponse(String rawJsonResponse) throws RequestException {
        try {
            JSONObject details =
                    new JSONObject(rawJsonResponse)
                            .getJSONObject("data")
                            .getJSONObject("DetalheProduto");
            owner = details.getString("Nome");
            initialBalance =
                    new BigDecimal(
                            details.getJSONObject("Credito")
                                    .getJSONObject("IS_Credito")
                                    .getString("ValorLimite"));
            initialDate =
                    LocalDate.parse(
                            details.getJSONObject("Operacao")
                                    .getJSONObject("IS_POSIOperacao")
                                    .getString("DataInicio"));
            finalDate =
                    LocalDate.parse(
                            details.getJSONObject("Operacao")
                                    .getJSONObject("IS_POSIOperacao")
                                    .getString("DataFim"),
                            DAY_MONTH_YEAR_DATE_FORMAT);
        } catch (JSONException ex) {
            throw new RequestException("Unexpected products details response format");
        }
    }

    public LocalDate getInitialDate() {
        return initialDate;
    }

    public LocalDate getFinalDate() {
        return finalDate;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public String getOwner() {
        return owner;
    }
}
