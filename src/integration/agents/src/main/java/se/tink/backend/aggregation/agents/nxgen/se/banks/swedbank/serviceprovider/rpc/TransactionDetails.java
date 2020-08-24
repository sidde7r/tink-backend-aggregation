package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionDetails {
    public final String transactionReference;
    public final String ownReference;

    public TransactionDetails(String transactionReference, String ownReference) {
        this.transactionReference =
                !Strings.isNullOrEmpty(transactionReference)
                        ? transactionReference
                        : StringUtils.EMPTY;
        this.ownReference = !Strings.isNullOrEmpty(ownReference) ? ownReference : StringUtils.EMPTY;
    }
}
