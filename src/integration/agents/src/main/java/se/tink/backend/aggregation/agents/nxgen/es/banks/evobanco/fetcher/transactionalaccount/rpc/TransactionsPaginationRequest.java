package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.EeIConsultationMovementsPostponedViewEntity;

public class TransactionsPaginationRequest {
    @JsonProperty("EE_I_ConsultaMovimientosVistaAplazada")
    private EeIConsultationMovementsPostponedViewEntity eeIConsultationMovementsPostponedView;

    @JsonIgnore
    public TransactionsPaginationRequest(
            EeIConsultationMovementsPostponedViewEntity eeIConsultationMovementsPostponedView) {
        this.eeIConsultationMovementsPostponedView = eeIConsultationMovementsPostponedView;
    }
}
