package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.BancoPopularConstants;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class AccountEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountEntity.class);

    @JsonProperty("numIntContrato")
    private int contractNumber;
    private String iban;
    @JsonProperty("banco")
    private String bank;
    @JsonProperty("sucursal")
    private String branchOffice;
    @JsonProperty("idExternaContrato")
    private String accountNumber;
    private long fecSald;
    @JsonDouble
    @JsonProperty("posicion")
    private double position;
    @JsonProperty("signoPosicion")
    private String positionSign;
    @JsonProperty("monedaPosicion")
    private String currencyPosition;
    @JsonDouble
    @JsonProperty("posicion2")
    private double position2;
    @JsonProperty("signoPosicion2")
    private String position2Sign;
    @JsonProperty("monedaPosicion2")
    private String currencyPosition2;
    @JsonDouble
    @JsonProperty("posicion3")
    private double position3;
    @JsonProperty("signoPosicion3")
    private String position3Sign;
    @JsonProperty("monedaPosicion3")
    private String currencyPosition3;
    @JsonDouble
    @JsonProperty("posicion4")
    private double position4;
    @JsonProperty("signoPosicion4")
    private String position4Sign;
    @JsonProperty("monedaPosicion4")
    private String currencyPosition4;
    @JsonProperty("tipoContrato")
    private String contractType;
    @JsonProperty("marca")
    private int brand;
    @JsonProperty("modalidad")
    private int modality;
    @JsonProperty("producto")
    private int product;
    @JsonProperty("situacion")
    private int situation;
    @JsonProperty("activacion")
    private int activation;
    @JsonProperty("numIntPrimerTitular")
    private int numIntPrimaryHolder;
    @JsonProperty("nomTitContrato")
    private String holderName;
    @JsonProperty("numIntSegundoTitular")
    private int inAnIntSecondHolder;
    @JsonProperty("nomTitContrato2")
    private String nameTitContrato2;
    @JsonProperty("numIntTercerTitular")
    private int numIntThirdTenant;
    @JsonProperty("nomTitContrato3")
    private String nameTitContrato3;
    @JsonProperty("numIntCuartoTitular")
    private int numIntFourthHolder;
    @JsonProperty("nomTitContrato4")
    private String nameTitContrato4;
    private int indicatrans1;
    @JsonProperty("indicapago")
    private int indicatespay;
    @JsonProperty("indica1")
    private int indicates1;
    private int indica2;
    private int indica3;
    private String alias;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {

        Optional<AccountTypes> type = BancoPopularConstants.ProductCode.translate(product);

        if (!type.isPresent()) {
            LOGGER.info("{} Unknown product code for: {}", BancoPopularConstants.Tags.UNKNOWN_PRODUCT_CODE,
                    SerializationUtils.serializeToString(this));
        }

        return TransactionalAccount
                .builder(type.orElse(AccountTypes.OTHER), formatAccountNumber().toLowerCase(), getTinkBalance())
                .setAccountNumber(formatAccountNumber())
                .setName(contractType)
                .setBankIdentifier(Integer.toString(contractNumber))
                .build();
    }

    // Format iban spanish style
    @JsonIgnore
    private String formatAccountNumber() {
        String externalId = accountNumber.replaceAll(" ", "");
        if (externalId.length() == 12) {
            return String.format("%s %s %s %s %s %s",
                    iban, bank, branchOffice,
                    externalId.substring(0, 4),
                    externalId.substring(4, 8),
                    externalId.substring(8));
        }

        return accountNumber;
    }

    @JsonIgnore
    private Amount getTinkBalance() {
        if (Strings.isNullOrEmpty(currencyPosition)) {
            return Amount.inEUR(position);
        }

        return new Amount(currencyPosition, position);
    }

    public int getContractNumber() {
        return contractNumber;
    }

    public String getIban() {
        return iban;
    }

    public String getBank() {
        return bank;
    }

    public String getBranchOffice() {
        return branchOffice;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public long getFecSald() {
        return fecSald;
    }

    public double getPosition() {
        return position;
    }

    public String getPositionSign() {
        return positionSign;
    }

    public String getCurrencyPosition() {
        return currencyPosition;
    }

    public double getPosition2() {
        return position2;
    }

    public String getPosition2Sign() {
        return position2Sign;
    }

    public String getCurrencyPosition2() {
        return currencyPosition2;
    }

    public double getPosition3() {
        return position3;
    }

    public String getPosition3Sign() {
        return position3Sign;
    }

    public String getCurrencyPosition3() {
        return currencyPosition3;
    }

    public double getPosition4() {
        return position4;
    }

    public String getPosition4Sign() {
        return position4Sign;
    }

    public String getCurrencyPosition4() {
        return currencyPosition4;
    }

    public String getContractType() {
        return contractType;
    }

    public int getBrand() {
        return brand;
    }

    public int getModality() {
        return modality;
    }

    public int getProduct() {
        return product;
    }

    public int getSituation() {
        return situation;
    }

    public int getActivation() {
        return activation;
    }

    public int getNumIntPrimaryHolder() {
        return numIntPrimaryHolder;
    }

    public String getHolderName() {
        return holderName;
    }

    public int getInAnIntSecondHolder() {
        return inAnIntSecondHolder;
    }

    public String getNameTitContrato2() {
        return nameTitContrato2;
    }

    public int getNumIntThirdTenant() {
        return numIntThirdTenant;
    }

    public String getNameTitContrato3() {
        return nameTitContrato3;
    }

    public int getNumIntFourthHolder() {
        return numIntFourthHolder;
    }

    public String getNameTitContrato4() {
        return nameTitContrato4;
    }

    public int getIndicatrans1() {
        return indicatrans1;
    }

    public int getIndicatespay() {
        return indicatespay;
    }

    public int getIndicates1() {
        return indicates1;
    }

    public int getIndica2() {
        return indica2;
    }

    public int getIndica3() {
        return indica3;
    }

    public String getAlias() {
        return alias;
    }
}
