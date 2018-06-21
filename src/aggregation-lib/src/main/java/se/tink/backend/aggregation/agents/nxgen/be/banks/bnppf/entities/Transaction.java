package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import com.google.common.base.Strings;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.BnpPfConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Transaction {
    private String accountId;
    private String transactionId;
    private String transactionReference;
    private Amount amount;
    private String creditDebitIndicator;
    private String status;
    private String bookingDateTime;
    private String valueDateTime;
    private String transactionInformation;
    private ServicerInfo servicer;
    private TransactionContextDetail transactionContextDetail;
    private BankTransactionCode bankTransactionCode;
    private MerchantDetails merchantDetails;
}
