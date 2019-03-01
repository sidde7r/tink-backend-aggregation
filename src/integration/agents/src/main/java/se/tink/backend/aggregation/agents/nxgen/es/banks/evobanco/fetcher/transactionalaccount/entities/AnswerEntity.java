package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class AnswerEntity {
    @JsonProperty("ListaAcuerdos")
    private List<AgreementsListEntity> agreementsList;

    @JsonProperty("numeroRegistros")
    private String registersNumber;
}
