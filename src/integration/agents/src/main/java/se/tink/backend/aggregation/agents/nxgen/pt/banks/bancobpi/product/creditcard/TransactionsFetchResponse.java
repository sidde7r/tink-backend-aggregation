package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionsFetchResponse {

    private List<Transaction> transactions = new LinkedList<>();
    private String bankFetchingUUID;
    private boolean lastPage;

    TransactionsFetchResponse(String rawResponse, CreditCardAccount account)
            throws RequestException {
        parseResponse(rawResponse, account);
    }

    private void parseResponse(String rawJsonResponse, CreditCardAccount account)
            throws RequestException {
        try {
            JSONObject data = new JSONObject(rawJsonResponse).getJSONObject("data");
            handleFetchingError(data);
            parsePagination(data.getJSONObject("PaginacaoOut"));
            parseTransactions(data.getJSONObject("ListaMovimentos").getJSONArray("List"), account);
        } catch (JSONException ex) {
            throw new RequestException(
                    "Credit card transactions fetching unexpected response format");
        }
    }

    private void handleFetchingError(JSONObject data) throws JSONException, RequestException {
        JSONArray errors =
                data.getJSONObject("TransactionStatus")
                        .getJSONObject("TransactionErrors")
                        .getJSONArray("List");
        if (errors.length() > 0) {
            throw new RequestException(
                    "Bank side error occured during fetching credit card transactions");
        }
    }

    private void parsePagination(JSONObject pagination) throws JSONException {
        bankFetchingUUID = pagination.getString("uuid");
        lastPage = pagination.getBoolean("lastPage");
    }

    private void parseTransactions(JSONArray transactionsArray, CreditCardAccount account)
            throws JSONException {
        for (int i = 0; i < transactionsArray.length(); i++) {
            JSONObject jsonTransaction = transactionsArray.getJSONObject(i);
            ExactCurrencyAmount transactionAmount =
                    ExactCurrencyAmount.of(jsonTransaction.getString("MontanteTransaccao"), "EUR");
            transactions.add(
                    CreditCardTransaction.builder()
                            .setAmount(transactionAmount)
                            .setCreditCardAccountNumber(
                                    account != null ? account.getAccountNumber() : null)
                            .setDate(LocalDate.parse(jsonTransaction.getString("DataTransaccao")))
                            .setDescription(jsonTransaction.getString("DescricaoTransaccao"))
                            .build());
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getBankFetchingUUID() {
        return bankFetchingUUID;
    }

    public boolean isLastPage() {
        return lastPage;
    }
}
