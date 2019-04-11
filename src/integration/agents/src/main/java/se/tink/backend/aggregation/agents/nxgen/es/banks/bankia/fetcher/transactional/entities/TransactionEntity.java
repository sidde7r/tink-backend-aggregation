package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {
    @JsonProperty("fechaMovimiento")
    private DateEntity transactionDate;

    @JsonProperty("fechaValor")
    private DateEntity valueDate;

    @JsonProperty("importe")
    private AmountEntity amount;

    @JsonProperty("oficina")
    private String office;

    @JsonProperty("conceptoMovimiento")
    private TransactionDescriptionEntity transactionDescription;

    @JsonProperty("saldoPosterior")
    private AmountEntity subsequentBalance;

    @JsonProperty("referencias")
    private List<TransactionReferenceEntity> references;

    @JsonProperty("indicadorTransferencia")
    private String transferIndicator;

    @JsonProperty("indicadorDevolucionRecibo")
    private String returnIndicatorReceipt;

    @JsonProperty("indicadorDevolucionReciboManyana")
    private String returnIndicatorManyanaReceipt;

    @JsonProperty("numSecuencial")
    private int sequentialNum;

    @JsonProperty("indicadorMovimientoCorrespondencia")
    private String flagMovementCorrespondence;

    @JsonProperty("codigoMovimiento")
    private String movementCode;

    @JsonProperty("esFinanciable")
    private boolean itIsFinandable;

    @JsonProperty("tipoFinanciacion")
    private String financingType;

    @JsonProperty("referenciaJusticicante2")
    private String referenceJusticicante2;

    @JsonProperty("referenciaJusticicante4")
    private String referenceJusticicante4;

    @JsonProperty("mostrarDetFechaMovimiento")
    private boolean showDetDateMovement;

    @JsonProperty("mostrarDetFechaValor")
    private boolean showDetDateValue;

    @JsonProperty("mostrarDetNumOficina")
    private boolean showDetNumOffice;

    @JsonProperty("mostrarDetIdentAcreedor")
    private boolean showDetIdentCreditor;

    @JsonProperty("mostrarDetRefOperacion")
    private boolean showDetRefOperation;

    @JsonProperty("mostrarDetConcepto")
    private boolean showDetConcept;

    @JsonProperty("mostrarDetOrdenante")
    private boolean showDetSorting;

    @JsonProperty("mostrarDetBeneficiario")
    private boolean showDetBeneficiary;

    @JsonProperty("mostrarDetNumTarjeta")
    private boolean showDetNumCard;

    @JsonProperty("mostrarDetIdentComercio")
    private boolean showDetIdentComercio;

    @JsonProperty("mostrarDetNombreComercio")
    private boolean showDetNameTrade;

    @JsonProperty("mostrarDetUbicacionTerminal")
    private boolean showDetTerminalLocation;

    @JsonProperty("mostrarDetVerEnMapa")
    private boolean showDetSeeOnMap;

    @JsonProperty("beneficiarioOEmisor")
    private String beneficiaryoIssuer;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(amount.toTinkAmount())
                .setDate(transactionDate.toJavaLangDate())
                .setDescription(transactionDescription.getDescriptionConcept())
                .build();
    }
}
