package se.tink.backend.aggregation.agents.banks.nordea.v20.model.transfers;

import com.google.common.base.Strings;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.ProductEntity;

public class BankTransferRequest {
    private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.ENGLISH);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00", DECIMAL_FORMAT_SYMBOLS);

    private CreateTransferIn createTransferIn;

    public CreateTransferIn getCreateTransferIn() {
        return createTransferIn;
    }

    public void setCreateTransferIn(CreateTransferIn createTransferIn) {
        this.createTransferIn = createTransferIn;
    }

    public static BankTransferRequest createNonRecurringBankTransfer() {
        BankTransferRequest transferRequest = new BankTransferRequest();
        CreateTransferIn in = new CreateTransferIn();
        in.setToAccountBranchId("");
        in.setFromAccountBranchId("");
        in.setDueDateType("ExpressPayment");
        in.setDueDate(DateTime.now().toString("yyyy-MM-dd"));
        in.setToAccountIdNickname("");
        in.setRecurringNumberOfPayments("0");
        in.setRecurringFrequency("Once");
        in.setRecurringContinuously("false");

        transferRequest.setCreateTransferIn(in);

        return transferRequest;
    }

    /**
     * Nordea is very sensitive and expect the amount to be formatted with exactly two decimals. Sending the value
     * of 1.0 SEK will be equal to a transfer of 0.10 SEK since they expect two decimals.
     *
     * @param amount
     */
    public void setAmount(double amount) {
        createTransferIn.setAmount(DECIMAL_FORMAT.format(amount));
    }

    public void setSource(ProductEntity source) {
        createTransferIn.setFromAccountId(source.getInternalId());
        createTransferIn.setFromAccountProductTypeExtension((String) source.getProductType().get("$"));
        createTransferIn.setCurrency((String) source.getCurrency().get("$"));
    }

    public void setDestination(ProductEntity destination) {
        createTransferIn.setToAccountId(destination.getInternalId());
    }

    public void setMessage(String message) {
        createTransferIn.setMessage(Strings.nullToEmpty(message));
    }
}
