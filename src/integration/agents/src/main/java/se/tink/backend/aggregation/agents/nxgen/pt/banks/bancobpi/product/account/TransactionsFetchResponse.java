package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionsFetchResponse {

    private List<Transaction> transactions = new LinkedList<>();
    private String bankFetchingUUID;
    private boolean lastPage;

    TransactionsFetchResponse(String rawResponse) throws RequestException {
        parse(rawResponse);
    }

    private void parse(String jsonResponse) throws RequestException {
        try {
            JSONObject response = new JSONObject(jsonResponse).getJSONObject("data");
            JSONArray responseTransactionsArray =
                    response.getJSONObject("MovimentosConta").getJSONArray("List");
            for (int i = 0; i < responseTransactionsArray.length(); i++) {
                transactions.add(mapToTinkTransaction(responseTransactionsArray.getJSONObject(i)));
            }
            JSONObject pagination = response.getJSONObject("PaginacaoOut");
            bankFetchingUUID = pagination.getString("uuid");
            lastPage = pagination.getBoolean("lastPage");
        } catch (JSONException e) {
            throw new RequestException("Unexpected response format");
        }
    }

    private Transaction mapToTinkTransaction(JSONObject responseTransaction) throws JSONException {
        Transaction.Builder tb = new Transaction.Builder();
        tb.setDate(LocalDate.parse(responseTransaction.getString("dataMovimento")));
        tb.setDescription(responseTransaction.getString("descricao"));
        tb.setAmount(
                ExactCurrencyAmount.of(
                        responseTransaction.getString("valorMoedaConta"),
                        responseTransaction.getString("moedaOperacao")));
        return tb.build();
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
