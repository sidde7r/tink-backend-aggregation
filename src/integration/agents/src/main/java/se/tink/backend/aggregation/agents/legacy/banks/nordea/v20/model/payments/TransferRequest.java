package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.google.common.base.Strings;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.ProductEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.beneficiaries.BeneficiaryEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.date.DateUtils;

public class TransferRequest {
    private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS =
            new DecimalFormatSymbols(Locale.ENGLISH);
    private static final DecimalFormat DECIMAL_FORMAT =
            new DecimalFormat("0.00", DECIMAL_FORMAT_SYMBOLS);

    private CreatePaymentIn createPaymentIn;

    public CreatePaymentIn getCreatePaymentIn() {
        return createPaymentIn;
    }

    public void setCreatePaymentIn(CreatePaymentIn createPaymentIn) {
        this.createPaymentIn = createPaymentIn;
    }

    private static TransferRequest createBaseTransferRequest() {
        TransferRequest transferRequest = new TransferRequest();
        CreatePaymentIn in = new CreatePaymentIn();
        in.setToAccountIdNickname("");
        in.setRecurringContinuously("false");
        in.setReference("");
        in.setPersonalNote("");
        in.setDueDateType("ExpressPayment");
        in.setBeneficiaryName("");
        in.setRecurringNumberOfPayments("0");
        in.setToBranchNumber("");
        in.setInvoicePaymentId("");
        in.setScannerUsed("NONE");
        in.setFromAccountBranchId("");
        in.setReceiptCode("NoReceipt");
        in.setEncryptedPaymentData("");
        in.setFromAccountProductTypeExtension("");
        in.setAddBeneficiary("false");
        in.setRecurringFrequency("Once");
        in.setDueDate(DateTime.now().toString("yyyy-MM-dd"));
        in.setBeneficiaryNickName("");
        in.setGiroNumber("");
        in.setChallenge("");
        in.setInvoicePaymentType("");
        in.setConfirmationCode("");
        in.setFromAccountIdNickname("");
        in.setStatusCode("Unconfirmed");
        in.setCategory("");
        in.setPotentialFraud("false");
        in.setStorePayment("No");

        transferRequest.setCreatePaymentIn(in);

        return transferRequest;
    }

    public static TransferRequest createNonRecurringBankTransferRequest() {
        TransferRequest transferRequest = createBaseTransferRequest();
        CreatePaymentIn in = transferRequest.getCreatePaymentIn();
        in.setPaymentSubType("ThirdParty");
        in.setPaymentSubTypeExtension("");

        return transferRequest;
    }

    public static TransferRequest createNonRecurringPaymentRequest() {
        TransferRequest transferRequest = createBaseTransferRequest();
        CreatePaymentIn in = transferRequest.getCreatePaymentIn();
        in.setPaymentSubType("Normal");

        return transferRequest;
    }

    public void setAmount(double amount) {
        createPaymentIn.setAmount(DECIMAL_FORMAT.format(amount));
    }

    public void setSource(ProductEntity source) {

        createPaymentIn.setFromAccountId(source.getInternalId());
        createPaymentIn.setCurrency((String) source.getCurrency().get("$"));
    }

    public void setDestination(BeneficiaryEntity destination) {

        createPaymentIn.setToAccountId(destination.getToAccountId());
        createPaymentIn.setBeneficiaryBankId(destination.getBeneficiaryBankId());
        createPaymentIn.setBeneficiaryName(destination.getBeneficiaryNickName());
        createPaymentIn.setBeneficiaryNickName(destination.getBeneficiaryNickName());
        createPaymentIn.setToAccountNumber(destination.getToAccountId());
    }

    public void setMessage(String message) {
        createPaymentIn.setMessageRow(Strings.nullToEmpty(message));
    }

    public void setDueDate(Date dueDate) {
        if (dueDate == null) {
            dueDate = DateUtils.getCurrentOrNextBusinessDay();
        }
        createPaymentIn.setDueDate(new DateTime(dueDate).toString("yyyy-MM-dd"));
    }

    public void setPaymentType(AccountIdentifier destination) {
        if (destination.is(Type.SE_BG)) {
            createPaymentIn.setPaymentSubTypeExtension("BGType");
        } else {
            createPaymentIn.setPaymentSubTypeExtension("PGType");
        }
    }
}
