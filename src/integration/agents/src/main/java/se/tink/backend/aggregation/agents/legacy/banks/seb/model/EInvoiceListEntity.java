package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.text.ParseException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.banks.seb.SEBAgentUtils;
import se.tink.backend.aggregation.agents.banks.seb.SebAccountIdentifierFormatter;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoiceListEntity implements MatchableTransferRequestEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final SebAccountIdentifierFormatter ACCOUNT_IDENTIFIER_FORMATTER =
            new SebAccountIdentifierFormatter();

    /**
     * Helper to filter away null valued list entity (SEB always sends one empty entity with no
     * values at all that is useless)
     */
    public static final Predicate<EInvoiceListEntity> IS_EMPTY =
            eInvoiceListEntity ->
                    eInvoiceListEntity == null
                            || Strings.isNullOrEmpty(eInvoiceListEntity.customerId);

    /** Converts entity to a pending eInvoice tink transfer */
    public static final Function<EInvoiceListEntity, Transfer> TO_TRANSFER =
            eInvoiceListEntity -> {
                Transfer transfer = new Transfer();
                transfer.setType(TransferType.EINVOICE);

                try {
                    transfer.setDueDate(eInvoiceListEntity.getOriginalDueDate());
                } catch (ParseException parseException) {
                    logger.error(
                            String.format(
                                    "Could not parse originalDueDate: %s",
                                    eInvoiceListEntity.originalDueDate),
                            parseException);
                    return null;
                }

                transfer.setAmount(eInvoiceListEntity.getOriginalAmount());
                transfer.setDestination(eInvoiceListEntity.getDestination());
                transfer.setDestinationMessage(eInvoiceListEntity.getDestinationMessage());
                RemittanceInformation remittanceInformation = new RemittanceInformation();
                remittanceInformation.setValue(eInvoiceListEntity.getDestinationMessage());
                transfer.setRemittanceInformation(remittanceInformation);
                transfer.setSource(eInvoiceListEntity.getSource());
                transfer.setSourceMessage(eInvoiceListEntity.getSourceMessage());

                transfer.addPayload(
                        TransferPayloadType.PROVIDER_UNIQUE_ID,
                        eInvoiceListEntity.getProviderUniqueId());

                return transfer;
            };

    /** Useful properties */
    @JsonProperty("SEB_KUND_NR")
    public String customerId;

    @JsonProperty("REFERENS")
    public String reference;

    @JsonProperty("TIMESTAMP")
    public String timestamp;

    @JsonDouble
    @JsonProperty("BELOPP")
    public Double amount;

    @JsonDouble
    @JsonProperty("BELOPP_URSPR")
    public Double originalAmount;

    @JsonProperty("FF_DATUM")
    public String dueDate;

    @JsonProperty("FF_DATUM_URSPR")
    public String originalDueDate;

    @JsonProperty("VALUTAKOD")
    public String currencyCode;

    @JsonProperty("BEL_ANDR_KOD")
    public String changeCode;

    @JsonProperty("E_GIROTYP")
    public String eGiroType;

    @JsonProperty("E_GIROFAKTTYP")
    public String eGiroInvoiceType;

    @JsonProperty("KONTO_NR")
    public String sourceAccount;

    @JsonProperty("NAMN")
    public String recipientName;

    @JsonProperty("FAKTURA_ID")
    public String invoiceId;

    @JsonProperty("E_REFERENSTEXT")
    public String sourceAccountReferenceText;

    @JsonProperty("UTSTALLARE_ID")
    public String recipientId;

    @JsonProperty("GIRO_TYP")
    public String giroType;

    @JsonProperty("GIRO_NR")
    public String giroNumber;

    @JsonProperty("STAT")
    public String state;

    /** Unknown properties - Don't know meaning of them (or unused) */
    @JsonProperty("ROW_ID")
    public Integer ROW_ID;

    @JsonProperty("BETALAR_NR")
    public String BETALAR_NR;

    @JsonProperty("BG_NR")
    public String BG_NR;

    @JsonProperty("FAKT_SPEC_URL")
    public String FAKT_SPEC_URL;

    @JsonProperty("KORT_NAMN")
    public String KORT_NAMN;

    @JsonProperty("KTOSLAG_TXT")
    public String KTOSLAG_TXT;

    @JsonProperty("KTOBEN_TXT")
    public String KTOBEN_TXT;

    @JsonProperty("BOKF_SALDO")
    public Double BOKF_SALDO;

    @JsonProperty("DISP_BEL")
    public Double DISP_BEL;

    @JsonProperty("KREDBEL")
    public Double KREDBEL;

    @JsonProperty("KHAV")
    public String KHAV;

    @JsonProperty("REF_ANV_KOD")
    public String REF_ANV_KOD;

    @JsonProperty("REF_TIDIGARE")
    public String REF_TIDIGARE;

    @JsonProperty("KONTO_NR_EA")
    public String KONTO_NR_EA;

    @JsonProperty("E_KATEGORI")
    public String E_KATEGORI;

    @JsonProperty("BELOPP_VAT")
    public Double BELOPP_VAT;

    @JsonProperty("PROCENT_VAT")
    public String PROCENT_VAT;

    @JsonProperty("TRANS_KOD_E")
    public String TRANS_KOD_E;

    @JsonProperty("BILJETT_TYP")
    public String BILJETT_TYP;

    @JsonProperty("FU_IDENT")
    public String FU_IDENT; // Looks to be possible unique identifier in some way?

    @JsonProperty("STATUS_EGIRO2")
    public String STATUS_EGIRO2;

    @JsonProperty("UPPDRAG_KOD_ANTAL")
    public Integer UPPDRAG_KOD_ANTAL;

    @JsonProperty("ENCRYPTED_TICKET")
    public String ENCRYPTED_TICKET;

    private Object modifiedDueDate;

    @JsonIgnore
    public ExactCurrencyAmount getCurrentAmount() {
        return ExactCurrencyAmount.inSEK(amount);
    }

    @JsonIgnore
    public ExactCurrencyAmount getOriginalAmount() {
        return ExactCurrencyAmount.inSEK(originalAmount);
    }

    @JsonIgnore
    public AccountIdentifier getDestination() {
        if (Objects.equal(giroType, "BG")) {
            AccountIdentifier bankGiroIdenfier = new BankGiroIdentifier(giroNumber);
            bankGiroIdenfier.setName(recipientName);
            return bankGiroIdenfier;
        } else if (Objects.equal(giroType, "PG")) {
            AccountIdentifier plusGiroIdentifier = new PlusGiroIdentifier(giroNumber);
            plusGiroIdentifier.setName(recipientName);
            return plusGiroIdentifier;
        } else {
            logger.error(
                    String.format(
                            "Unknown destination type for giroNumber (giroType=%s and giroNumber=%s)",
                            giroType, giroNumber));
            return new NonValidIdentifier(giroNumber);
        }
    }

    @JsonIgnore
    public String getDestinationMessage() {
        return reference;
    }

    /**
     * The original dueDate on the received invoice Parse exception should never occur, but log
     * where used just in case
     */
    @JsonIgnore
    public Date getOriginalDueDate() throws ParseException {
        return DateUtils.flattenTime(ThreadSafeDateFormat.FORMATTER_DAILY.parse(originalDueDate));
    }

    /**
     * The user set dueDate (or if it was modified somehow) Parse exception should never occur (it
     * seems it always is set to the originalDueDate by default), but log where used just in case
     */
    @JsonIgnore
    public Date getCurrentDueDate() throws ParseException {
        return DateUtils.flattenTime(ThreadSafeDateFormat.FORMATTER_DAILY.parse(dueDate));
    }

    /**
     * SEB assigns eInvoices to default accounts (this might be changeable, but for now take what
     * they give us)
     */
    @JsonIgnore
    public SwedishIdentifier getSource() {
        return new SwedishIdentifier(sourceAccount);
    }

    /**
     * @return For now just human readable format of the recipient shortened to the 25 char that SEB
     *     gives us
     */
    @JsonIgnore
    public String getSourceMessage() {
        return StringUtils.formatHuman(recipientName);
    }

    @JsonIgnore
    public String getProviderUniqueId() {
        return recipientId + "|" + invoiceId;
    }

    @JsonIgnore
    public void setCurrentDueDate(Date dueDate) {
        this.dueDate = ThreadSafeDateFormat.FORMATTER_DAILY.format(dueDate);
    }

    @JsonIgnore
    public State getState() {
        return State.fromString(state);
    }

    @JsonIgnore
    public void setState(State state) {
        this.state = state.getState();
    }

    @JsonIgnore
    public void setSource(AccountIdentifier source) {
        this.sourceAccount = source.getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER);
    }

    @Override
    @JsonIgnore
    public boolean matches(TransferListEntity transferListEntity) {
        // Source and destination accounts cannot be null
        if (this.giroNumber == null
                || this.sourceAccount == null
                || transferListEntity.SourceAccountNumber == null
                || transferListEntity.DestinationAccountNumber == null) {
            return false;
        }

        AccountIdentifier.Type expectedType;
        if (Objects.equal(giroType.trim(), "BG")) {
            expectedType = AccountIdentifier.Type.SE_BG;
        } else if (Objects.equal(giroType.trim(), "PG")) {
            expectedType = AccountIdentifier.Type.SE_PG;
        } else {
            logger.error("Unexpected account type found: " + giroType);
            return false;
        }

        // Accounts and the amount must match
        Date currentDueDate;
        Date transferEntityDate;
        try {
            currentDueDate = Preconditions.checkNotNull(getCurrentDueDate());
            transferEntityDate = Preconditions.checkNotNull(transferListEntity.getTransferDate());
        } catch (ParseException parseException) {
            logger.error("Could not parse date when matching transfer dates", parseException);
            return false;
        } catch (NullPointerException npe) {
            logger.error("Date is unexpectedly null", npe);
            return false;
        }

        return Objects.equal(expectedType, transferListEntity.getDestinationType())
                && SEBAgentUtils.trimmedDashAgnosticEquals(
                        this.giroNumber, transferListEntity.DestinationAccountNumber)
                && SEBAgentUtils.trimmedDashAgnosticEquals(
                        this.sourceAccount, transferListEntity.SourceAccountNumber)
                && Objects.equal(currentDueDate, transferEntityDate)
                && Math.abs(getCurrentAmount().getDoubleValue() - transferListEntity.Amount) < 0.01;
    }

    /**
     * When moving an eInvoice between the initial state (when eInvoices arrive to the customer) and
     * the outbox (before signing the eInvoice) the state is a flag that's needed for the movement
     * to occur.
     */
    public enum State {
        UNKNOWN(null),
        INITIAL("1"),
        OUTBOX("2"); // Guess there can be more states, but these are the ones we know for now and
        // most importantly the "2" state is the needed for adding to outbox. If "1"
        // is set nothing happens.

        private final String state;

        State(String state) {
            this.state = state;
        }

        public String getState() {
            return this.state;
        }

        public static State fromString(String STAT) {
            for (State state : values()) {
                if (Objects.equal(state.getState(), STAT)) {
                    return state;
                }
            }

            return UNKNOWN;
        }
    }
}
