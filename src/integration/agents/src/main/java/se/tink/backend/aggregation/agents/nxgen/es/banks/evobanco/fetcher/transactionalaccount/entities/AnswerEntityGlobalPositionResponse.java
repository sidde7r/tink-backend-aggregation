package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonObject
public class AnswerEntityGlobalPositionResponse {
    @JsonProperty("ListaAcuerdos")
    private List<AgreementsListEntity> agreementsList;

    @JsonProperty("numeroRegistros")
    private String registersNumber;

    public List<AgreementsListEntity> getAgreementsList() {
        return agreementsList;
    }

    public Collection<TransactionalAccount> getTransactionalAccounts(String holderName) {
        return agreementsList.stream()
                .filter(AgreementsListEntity::isAccount)
                .map(agreementsListEntity -> agreementsListEntity.toTinkAccount(holderName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
