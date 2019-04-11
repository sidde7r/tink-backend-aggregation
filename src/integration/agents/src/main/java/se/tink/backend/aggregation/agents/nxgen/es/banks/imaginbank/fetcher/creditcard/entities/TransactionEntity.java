package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class TransactionEntity {
    private String id;
    private String idMovimiento;

    @JsonProperty("concepto")
    private String text;

    @JsonProperty("fecha")
    private DateEntity date;

    private DateEntity hora;

    @JsonProperty("importe")
    private BalanceEntity amount;

    private boolean estaLeido;
    private boolean tieneDocumento;
    private String idCategoriaMovimiento;
    private String categoriaMovimiento;
    private String numeroTarjetaMovimiento;
    private String tipoTarjetaMovimiento;
    private String fraccionable;
    private String accesoDetalleMovimiento;
    private boolean indicadorMasterPass;

    @JsonIgnore
    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(amount.negate())
                .setDate(date.toTinkDate())
                .setDescription(text)
                .build();
    }
}
