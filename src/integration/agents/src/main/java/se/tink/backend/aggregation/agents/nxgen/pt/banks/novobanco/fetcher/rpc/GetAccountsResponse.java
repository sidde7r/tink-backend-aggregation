package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseCodes;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.ContextEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.BodyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetAccountsResponse {
    private static final Logger logger = LoggerFactory.getLogger(GetAccountsResponse.class);

    @JsonProperty("Header")
    private HeaderEntity header;

    @JsonProperty("Body")
    private BodyEntity body;

    public HeaderEntity getHeader() {
        return header;
    }

    public BodyEntity getBody() {
        return body;
    }

    public boolean isSuccessful() {
        Integer resultCode = getResultCode();
        boolean isSuccessful =
                Optional.ofNullable(resultCode).map(code -> ResponseCodes.OK == code).orElse(false);
        if (!isSuccessful) {
            logger.warn("ObterLista Response ended up with failure code: " + resultCode);
        }

        return isSuccessful;
    }

    private Integer getResultCode() {
        return Optional.ofNullable(getHeader()).map(HeaderEntity::getResultCode).orElse(null);
    }

    public Collection<AccountDetailsEntity> getAccountDetailsEntities() {
        return Optional.of(getHeader())
                .map(HeaderEntity::getContext)
                .map(ContextEntity::getAccounts)
                .map(AccountsEntity::getList)
                .orElse(Collections.emptyList());
    }
}
