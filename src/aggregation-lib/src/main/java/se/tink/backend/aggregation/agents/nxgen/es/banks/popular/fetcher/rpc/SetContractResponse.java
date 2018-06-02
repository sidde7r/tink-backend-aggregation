package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SetContractResponse {
    private String result;
    private String codError;
    private String descError;

    public boolean isSuccess() {
        return BancoPopularConstants.Fetcher.OK.equalsIgnoreCase(result);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("result", result)
                .append("codError", codError)
                .append("descError", descError)
                .toString();
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCodError() {
        return codError;
    }

    public void setCodError(String codError) {
        this.codError = codError;
    }

    public String getDescError() {
        return descError;
    }

    public void setDescError(String descError) {
        this.descError = descError;
    }
}
