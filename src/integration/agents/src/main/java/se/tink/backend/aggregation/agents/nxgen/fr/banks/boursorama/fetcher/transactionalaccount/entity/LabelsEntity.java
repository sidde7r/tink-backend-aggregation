package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LabelsEntity {
    private String body;
    private String type;

    public String getBody() {
        return body;
    }

    public boolean isTransactionDescriptionLabel() {
        return type.equals(BoursoramaConstants.Transaction.TRANSACTION_DESCRIPTION_LABEL);
    }
}
