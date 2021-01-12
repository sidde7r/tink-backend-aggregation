package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

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
                .map(agreementsListEntity -> agreementsListEntity.toTinkAccount(holderName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Collection<CreditCardAccount> getCreditCardAccounts() {
        return agreementsList.stream()
                .filter(AgreementsListEntity::isCard)
                .map(AgreementsListEntity::toTinkCreditCard)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
