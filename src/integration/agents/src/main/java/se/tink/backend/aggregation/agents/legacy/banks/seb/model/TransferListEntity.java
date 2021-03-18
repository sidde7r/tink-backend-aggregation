package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.util.Date;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class TransferListEntity {
    @JsonProperty("ROW_ID")
    public int RowId;

    @JsonProperty("USER_ID_NKL")
    public String UserIdNkl;

    @JsonProperty("ROLL_ID_NKL")
    public String RollIdNkl;

    @JsonProperty("LEV_SATT")
    public String LevSatt;

    @JsonProperty("TIME_STAMP_NKL")
    public String timeStampNkl;

    @JsonProperty("KONTO_NR")
    public String SourceAccountNumber;

    @JsonProperty("KK_TXT")
    public String InternalMessage;

    @JsonProperty("UPPDRAG_BEL")
    public Double Amount;

    @JsonProperty("MOTT_KONTO_NR")
    public String DestinationAccountNumber;

    @JsonIgnore
    public abstract String getTransferDateString();

    @JsonIgnore
    public abstract AccountIdentifierType getDestinationType();

    @JsonIgnore
    public Date getTransferDate() throws ParseException {
        return DateUtils.flattenTime(
                ThreadSafeDateFormat.FORMATTER_DAILY.parse(getTransferDateString()));
    }
}
