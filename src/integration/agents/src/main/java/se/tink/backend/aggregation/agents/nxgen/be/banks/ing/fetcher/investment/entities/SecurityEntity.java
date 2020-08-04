package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class SecurityEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String securityName;
    private String securityNumber;
    private AmountEntity securityCurrentPrice;
    private AmountEntity securityBalance;
    private String securityIsincode;
    private String securityType;
    private String securitySubType;
    private String securitySector;
    private String securityDate;

    public String getSecurityName() {
        return securityName;
    }

    public String getSecurityNumber() {
        return securityNumber;
    }

    public AmountEntity getSecurityCurrentPrice() {
        return securityCurrentPrice;
    }

    public AmountEntity getSecurityBalance() {
        return securityBalance;
    }

    public String getSecurityIsincode() {
        return securityIsincode;
    }

    public String getSecurityType() {
        return securityType;
    }

    public String getSecuritySubType() {
        return securitySubType;
    }

    public String getSecuritySector() {
        return securitySector;
    }

    public String getSecurityDate() {
        return securityDate;
    }

    public InstrumentModule toTinkInstrument() {

        return InstrumentModule.builder()
                .withType(toTinkInstrumentType())
                .withId(toTinkInstrumentId())
                .withMarketPrice(toMarketPrice())
                .withMarketValue(toMarketValue())
                .withAverageAcquisitionPrice(toAverageAcquisitionPrice())
                .withCurrency(securityCurrentPrice.getCurrency())
                .withQuantity(toQuantity())
                .withProfit(toProfit())
                .build();
    }

    private InstrumentIdModule toTinkInstrumentId() {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier(securityIsincode)
                .withName(Optional.ofNullable(securityName).orElse(""))
                .build();
    }

    private Double toMarketPrice() {
        return Optional.ofNullable(
                        IngHelper.parseBalanceStringToDouble((securityCurrentPrice.getAmount())))
                .orElse(0.0);
    }

    private Double toMarketValue() {
        return Optional.ofNullable(
                        IngHelper.parseBalanceStringToDouble((securityCurrentPrice.getAmount())))
                .orElse(0.0);
    }

    // Not stated from the API
    private Double toProfit() {
        return 0.0;
    }

    private Double toQuantity() {
        return Optional.ofNullable(IngHelper.parseQuantityStringToDouble((securityNumber)))
                .orElse(0.0);
    }

    private Double toAverageAcquisitionPrice() {
        Double aap = (toMarketValue() - toProfit()) / toQuantity();
        if (aap < 0) {
            return 0.0;
        }
        return aap;
    }

    private InstrumentType toTinkInstrumentType() {
        final String type = StringUtils.deleteWhitespace(securityType.toLowerCase());

        if (IngConstants.InstrumentTypes.STOCK.contains(type)) {
            return InstrumentType.STOCK;
        } else if (IngConstants.InstrumentTypes.FUND.contains(type)) {
            return InstrumentType.FUND;
        } else {
            logger.warn(
                    String.format(
                            "[%s] Unknown instrument type: [%s]",
                            IngConstants.Logs.UNKNOWN_INSTRUMENT_TYPE.toString(),
                            SerializationUtils.serializeToString(securityType)));
            return InstrumentType.OTHER;
        }
    }
}
