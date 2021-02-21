package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.entities.PreciseAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "methodResult")
public class FundDetailsResponse {
    @JsonIgnore
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private InfoEntity info;

    @JsonProperty("titular")
    private String holder;

    @JsonProperty("descFondo")
    private String name;

    @JsonProperty("valorLiquidativoParticipac")
    private PreciseAmountEntity marketPrice;

    @JsonProperty("valorTotal")
    private AmountEntity marketValue;

    @JsonProperty("fechaValor")
    private String valueDate;

    @JsonProperty("numParticipac")
    private double quantity;

    @JsonProperty("descCuentaAsociada")
    private String associatedAccount;

    @JsonIgnore
    public LocalDate getValueDate() {
        if (Strings.isNullOrEmpty(valueDate)) {
            return null;
        }

        return LocalDate.parse(valueDate, DATE_FORMATTER);
    }

    public String getHolder() {
        return holder;
    }

    public List<Instrument> toTinkInstruments() {
        Instrument instrument = new Instrument();
        instrument.setCurrency(marketValue.getTinkAmount().getCurrencyCode());
        instrument.setMarketValue(marketValue.getTinkAmount().getDoubleValue());
        instrument.setPrice(marketPrice.getTinkAmount().getDoubleValue());
        instrument.setType(Instrument.Type.FUND);
        instrument.setQuantity(quantity);
        instrument.setName(name);
        instrument.setUniqueIdentifier(name);

        return Collections.singletonList(instrument);
    }
}
