package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import org.apache.commons.lang3.builder.ToStringBuilder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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

    public String getCodError() {
        return codError;
    }

    public String getDescError() {
        return descError;
    }
}
