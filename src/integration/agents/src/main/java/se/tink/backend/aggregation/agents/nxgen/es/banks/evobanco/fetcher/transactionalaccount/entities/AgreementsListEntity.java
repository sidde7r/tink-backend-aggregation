package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementsListEntity {
    @JsonProperty("saldoLimite")
    private String balanceLimit;

    @JsonProperty("relacionAcuerdoPersona")
    private String relationshipAgreementPerson;

    private String sitIrregular;

    @JsonProperty("tipoCuenta")
    private String accountType;

    @JsonProperty("aliasBE")
    private String aliasbe;

    @JsonProperty("saldoNoDispuesto")
    private String balanceNoUsed;

    @JsonProperty("campoLineaGrupo")
    private String fieldLineGroup;

    @JsonProperty("codigoMoneda")
    private String currencyCode;

    @JsonProperty("saldoDeudaImpg")
    private String debtBalanceImpg;

    @JsonProperty("acuerdo")
    private String agreement;

    @JsonProperty("numFavoritas")
    private String numFavorites;

    @JsonProperty("acuerdoRelacionado")
    private String relatedAgreement;

    @JsonProperty("saldoNoVencido")
    private String unspentBalance;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("DatosTarjeta")
    private CardDataEntity cardData;
}
