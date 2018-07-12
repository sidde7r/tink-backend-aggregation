package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountStatementItem;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.OK;

public class GetAccountStatementItemsResponse {
    private Envelope envelope;

    public GetAccountStatementItemsResponse(Envelope envelope) {
        this.envelope = envelope;
    }

    public List<AccountStatementItem> getAccountStatementItemList() {
        final OK ok = envelope.getBody().getGetAccountStatementItemsResponseEntity().getOk();
        return Optional.ofNullable(ok.getAccountStatementItemList()).orElse(new ArrayList<>());
    }
}

