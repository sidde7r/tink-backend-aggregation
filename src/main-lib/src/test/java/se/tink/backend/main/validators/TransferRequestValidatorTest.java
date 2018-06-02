package se.tink.backend.main.validators;

import java.io.IOException;
import java.util.Date;
import javax.ws.rs.WebApplicationException;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.Amount;
import se.tink.backend.core.enums.TransferType;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.main.validators.exception.AbstractTransferException;
import se.tink.backend.main.validators.exception.AbstractTransferException.LogMessage;
import se.tink.backend.main.validators.exception.InstantiationException;
import se.tink.backend.main.validators.exception.TransferNotFoundException;
import se.tink.backend.main.validators.exception.TransferValidationException;
import se.tink.backend.main.validators.exception.TransfersTemporaryDisabledException;
import se.tink.libraries.date.DateUtils;
import static org.mockito.Mockito.when;

public class TransferRequestValidatorTest extends AbstractValidatorTest {
    private TransferRequestValidator validator;

    @Before
    public void setup() throws InstantiationException {
        transferConfiguration = mockTransferConfiguration();
        transfer = createTransfer(NORDEA_SSN_IDENTIFIER);

        validator = new TransferRequestValidator(transferConfiguration);
    }

    @Test
    public void ensureExceptionIsThrown_whenTransfersConfiguration_isNull_whileInstantiatingValidatorWithThreeArguments() throws TransfersTemporaryDisabledException, TransferValidationException {
        expect("No TransfersConfiguration provided", InstantiationException.class);

        new TransferRequestValidator(null);
    }

    @Test
    public void ensureValidatorWithThreeArgumentsIsInstantiated_whenNoInjectedObjects_areNull() throws TransfersTemporaryDisabledException, TransferValidationException {
        new TransferRequestValidator(transferConfiguration);
    }

    @Test
    public void ensureValidateEnabled_throwsException_whenTransfersAreDisabled() 
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        when(transferConfiguration.isEnabled()).thenReturn(false);

        expect("Transfer service is temporarily disabled", TransfersTemporaryDisabledException.class);
        
        validator.validate(transfer);
    }

    @Test
    public void ensureValidate_throwsBadRequest_whenTransfer_isNull()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        expect(TransferNotFoundException.MESSAGE, TransferNotFoundException.class);

        validator.validate(null);

    }

    @Test
    public void ensureValidateAmount_throwsException_whenAmountCurrency_isNull()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setAmount(new Amount(null, 150.00));

        expect(LogMessage.MISSING_CURRENCY);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmount_throwsException_whenAmountCurrency_isEmpty()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setAmount(new Amount("", 150.00));

        expect(LogMessage.MISSING_CURRENCY);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmount_throwsException_whenAmountValue_isNull()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setAmount(Amount.inSEK(null));

        expect(LogMessage.MISSING_AMOUNT);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmount_throwsException_whenAmountValue_isZero()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setAmount(Amount.inSEK(0.00));

        expect(LogMessage.MISSING_AMOUNT);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateAmount_throwsException_whenAmount_isNegativeOrZero()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setAmount(Amount.inSEK(-20.00));

        expect(LogMessage.NEGATIVE_AMOUNT);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_throwsException_whenDestinationIdentifier_isNull()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setDestination(null);

        expect(LogMessage.MISSING_DESTINATION);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_throwsException_whenDestinationIdentifier_isInvalid()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        String jsonTransfer = "{\"amount\":150.0,\"credentialsId\":null,\"currency\":\"SEK\",\"destinationMessage\":null,\"id\":\"2a4a3f01-038d-4587-9037-e41d7d58322e\",\"sourceMessage\":null,\"userId\":\"627fba23-f10a-4eb9-bb66-7be6f144151f\",\"type\":\"BANK_TRANSFER\",\"dueDate\":1469618176470,\"payloadSerialized\":null,\"destinationUri\":\"se-bg://0\",\"sourceUri\":\"se://6152135538858\"}";

        try {
            transfer = MAPPER.readValue(jsonTransfer, Transfer.class);

            expect(LogMessage.INVALID_DESTINATION);

            validator.validate(transfer);
        } catch (IOException e) {
            throw new AssertionError("Couldn't deserialize json transfer");
        }
    }

    @Test
    public void ensureValidateDestination_throwsException_whenDestinationType_isPG_andTransferType_isBankTransfer()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setDestination(BARNCANCERFONDEN_PG);

        expect(LogMessage.INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_throwsException_whenDestinationType_isBG_andTransferType_isBankTransfer()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setDestination(BARNCANCERFONDEN_BG);

        expect(LogMessage.INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_throwsException_whenDestinationType_isSE_andTransferType_isPayment()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setType(TransferType.PAYMENT);

        expect(LogMessage.INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_throwsException_whenDestinationType_isSE_andTransferType_isEInvoice()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setType(TransferType.EINVOICE);

        expect(LogMessage.INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_throwsException_whenDestinationType_isSHB_andTransferType_isPayment()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setType(TransferType.PAYMENT);
        transfer.setDestination(SHB_IDENTIFIER);

        expect(LogMessage.INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_throwsException_whenDestinationType_isSHB_andTransferType_isEInvoice()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setType(TransferType.EINVOICE);
        transfer.setDestination(SHB_IDENTIFIER);

        expect(LogMessage.INCOMPATIBLE_TRANSFER_AND_DESTINATION_TYPES);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_validationPasses_whenDestinationType_isSE_andTransferType_isBankTransfer()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestination_validationPasses_whenDestinationType_isSHB_andTransferType_isBankTransfer()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setDestination(SHB_IDENTIFIER);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateSource_throwsException_whenSourceIdentifier_isNull()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setSource(null);

        expect(LogMessage.MISSING_SOURCE);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateSource_throwsException_whenSourceIdentifier_isInvalid()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        String jsonTransfer = "{\"amount\":150.0,\"credentialsId\":null,\"currency\":\"SEK\",\"destinationMessage\":null,\"id\":\"2a4a3f01-038d-4587-9037-e41d7d58322e\",\"sourceMessage\":null,\"userId\":\"627fba23-f10a-4eb9-bb66-7be6f144151f\",\"type\":\"PAYMENT\",\"dueDate\":1469618176470,\"payloadSerialized\":null,\"destinationUri\":\"se-bg://9020900\",\"sourceUri\":\"se://0\"}";

        try {
            transfer = MAPPER.readValue(jsonTransfer, Transfer.class);

            expect(LogMessage.INVALID_SOURCE);

            validator.validate(transfer);
        } catch (IOException e) {
            throw new AssertionError("Couldn't deserialize json transfer");
        }
    }

    @Test
    public void ensureValidateDueDate_whenDueDate_isNull()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setType(TransferType.PAYMENT);
        transfer.setDestination(BARNCANCERFONDEN_BG);
        transfer.setDueDate(null);
        transfer.setDestinationMessage("message");

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDueDate_throwsException_whenDueDate_isBeforeToday()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        Date dueDate = DateUtils.addDays(new Date(),
                -10);
        transfer.setDueDate(dueDate);

        expect(LogMessage.PAYMENT_DATE_BEFORE_TODAY);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDueDate_throwsException_whenPaymentDueDate_isHoliday()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setDestination(PG_IDENTIFIER);
        transfer.setType(TransferType.PAYMENT);
        transfer.setDueDate(getNextHoliday())
        ;

        expect(LogMessage.PAYMENT_DATE_NOT_BUSINESS_DAY);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDueDate_validationPasses_whenTransferDueDate_isHoliday()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setDueDate(getNextHoliday())
        ;

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDueDate_validationPasses_whenTransferDueDate_isBusinessDay()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setDueDate(DateUtils.getCurrentOrNextBusinessDay())
        ;

        validator.validate(transfer);
    }

    @Test
    public void ensureValidateDestinationMessage_throwsException_whenEInvoiceDestinationMessage_isNull()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        transfer.setType(TransferType.EINVOICE);
        transfer.setDestination(BARNCANCERFONDEN_BG);
        transfer.setDestinationMessage(null);

        expect(LogMessage.MISSING_PAYMENT_DESTINATION_MESSAGE);

        validator.validate(transfer);
    }

    @Test
    public void ensureValidationPasses_whenPayment_isValid()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        Transfer payment = createPayment(BARNCANCERFONDEN_BG);

        validator.validate(payment);
    }

    @Test
    public void ensureValidationPasses_whenEInvoice_isValid()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        Transfer eInvoice = createEInvoice(BARNCANCERFONDEN_BG);

        validator.validate(eInvoice);
    }

    @Test
    public void ensureValidationPasses_whenBankTransfer_isValid()
            throws TransferNotFoundException, TransfersTemporaryDisabledException, TransferValidationException {
        Transfer transfer = createTransfer(NORDEA_SSN_IDENTIFIER);

        validator.validate(transfer);
    }

    public String getExpectedMessage(AbstractTransferException.LogMessage message) {
        return String.format("Transfer validation failed ( %s )", message.get());
    }

    private Date getNextHoliday() {
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek();

        if (dayOfWeek == DateTimeConstants.SATURDAY || dayOfWeek == DateTimeConstants.SUNDAY) {
            return today.toDate();
        } else {
            int daysToSaturday = DateTimeConstants.SATURDAY - dayOfWeek;
            return today.plusDays(daysToSaturday).toDate();
        }
    }
}
