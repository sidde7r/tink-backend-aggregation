package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAccountsContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class PinAuthenticationResponse extends LoginResponse {

    private BancoBpiAccountsContext bancoBpiAccountsContext;

    PinAuthenticationResponse(
            String rawJsonResponse,
            TinkHttpClient httpClient,
            BancoBpiAccountsContext bancoBpiAccountsContext)
            throws RequestException {
        super(rawJsonResponse, httpClient);
        this.bancoBpiAccountsContext = bancoBpiAccountsContext;
        extractTransactionalAccountBaseInfo();
    }

    private void extractTransactionalAccountBaseInfo() throws RequestException {
        try {
            JSONObject responseData = getResponse().getJSONObject("data");
            bancoBpiAccountsContext.setNip(responseData.getString("NIP"));
            JSONArray responseAccountList =
                    responseData.getJSONObject("rlContas").getJSONArray("List");
            for (int i = 0; i < responseAccountList.length(); i++) {
                JSONObject responseAccount = responseAccountList.getJSONObject(i);
                JSONObject responseContaSmall = responseAccount.getJSONObject("IS_ContaSmall");
                TransactionalAccountBaseInfo accountInfo = new TransactionalAccountBaseInfo();
                accountInfo.setAccountName(responseAccount.getString("ContaFormatada"));
                accountInfo.setClientId(responseAccount.getString("ClienteContaId"));
                accountInfo.setCurrency(responseAccount.getString("Moeda"));
                accountInfo.setInternalAccountId(responseContaSmall.getString("nuc"));
                accountInfo.setType(responseContaSmall.getString("tipo"));
                accountInfo.setOrder(responseContaSmall.getString("ordem"));
                bancoBpiAccountsContext.getAccountInfo().add(accountInfo);
            }
        } catch (JSONException e) {
            throw new RequestException("Response JSON doesn't have expected format");
        }
    }
}
