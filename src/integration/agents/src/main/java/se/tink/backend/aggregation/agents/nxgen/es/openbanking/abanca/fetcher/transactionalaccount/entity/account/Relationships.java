package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Relationships {

    private Movimientos movimientos;
    private Transferencias transferencias;
    private SimulacionesDeTransferencias simulacionesDeTransferencias;
}
