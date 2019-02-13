package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json;

import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class RequestEntity {
    private String name;
    private String url;
    private List<ParamEntity> params;

    public String getName() {
        return this.name;
    }

    public String getUrl() {
        return this.url;
    }

    public List<ParamEntity> getParams() {
        return this.params;
    }

    public boolean isGetAccounts() {
        return nameEquals(IngConstants.RequestNames.GET_ACCOUNTS);
    }

    public boolean isGetPendingPayments() {
        return nameEquals(IngConstants.RequestNames.GET_PENDING_PAYMENTS);
    }

    public boolean isCreditCards() {
        return nameEquals(IngConstants.RequestNames.CREDITCARD_LIST);
    }

    public boolean isCreditCardTransactions() {
        return nameEquals(IngConstants.RequestNames.CREDITCARD_TRANSACTIONS);
    }

    public boolean isTrustedBenficiariesRequest() {
        return nameEquals(IngConstants.RequestNames.GET_TRUSTED_BENEFICIARIES);
    }

    public boolean isValidateTransfer() {
        return nameEquals(IngConstants.RequestNames.VALIDATE_INTERNAL_TRANSFER);
    }

    public boolean isValidateTrustedTransfer() {
        return nameEquals(IngConstants.RequestNames.VALIDATE_TRUSTED_TRANSFER);
    }

    public boolean isValidateThirdPartyTransfer() {
        return nameEquals(IngConstants.RequestNames.VALIDATE_THIRD_PARTY_TRANSFER);
    }

    public boolean isExecuteTrustedTransfer() {
        return nameEquals(IngConstants.RequestNames.EXECUTE_TRUSTED_TRANSFER);
    }

    public boolean isExecuteThirdPartyTransfer() {
        return nameEquals(IngConstants.RequestNames.EXECUTE_THIRD_PARTY_TRANSFER);
    }

    private boolean nameEquals(String name) {
        return name.equalsIgnoreCase(this.name);
    }

    public URL asSSORequest() {
        return new URL(
                IngConstants.Urls.BASE_SSO_REQUEST + StringEscapeUtils.unescapeHtml(this.url));
    }
}
