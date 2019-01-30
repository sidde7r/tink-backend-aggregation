package se.tink.backend.aggregation.agents.banks.nordea.v15.model.savings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.BankingServiceResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class CustodyAccountsResponse {

    private BankingServiceResponse bankingServiceResponse;

    private List<CustodyAccount> custodyAccounts = Lists.newArrayList();

    public List<CustodyAccount> getCustodyAccounts() {
        return custodyAccounts;
    }

    public void setCustodyAccounts(List<CustodyAccount> custodyAccounts) {
        this.custodyAccounts = custodyAccounts;
    }

    public String getErrorCode() {
        if (bankingServiceResponse == null || bankingServiceResponse.getErrorMessage() == null) {
            return null;
        }

        return (String) bankingServiceResponse.getErrorMessage().getErrorCode().get("$");
    }

    public boolean hasError() {
        return !Strings.isNullOrEmpty(getErrorCode());
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }
}
