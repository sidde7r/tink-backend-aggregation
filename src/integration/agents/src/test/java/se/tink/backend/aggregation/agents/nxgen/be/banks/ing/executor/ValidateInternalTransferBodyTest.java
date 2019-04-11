package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc.ValidateInternalTransferBody;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.transfer.enums.MessageType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

public class ValidateInternalTransferBodyTest {

    @Test
    public void testShortMessageFormatting() {
        Amount amount = Amount.inEUR(2.0);
        String message = "Tink Test";

        Transfer transfer = createTransfer(amount, message);
        ValidateInternalTransferBody body =
                new ValidateInternalTransferBody(transfer, "fromAccount", "toAccount");

        Assertions.assertThat(body.get("amount").get(0)).isEqualTo("+000000000000002002");
        Assertions.assertThat(body.get("commLine1").get(0)).isEqualTo("Tink Test");
        Assertions.assertThat(body.get("commLine2").get(0)).isEqualTo("");
        Assertions.assertThat(body.get("commLine3").get(0)).isEqualTo("");
        Assertions.assertThat(body.get("commLine4").get(0)).isEqualTo("");
        Assertions.assertThat(body.get("memoDate").get(0)).isEqualTo("20180417");
    }

    @Test
    public void testAverageMessageFormatting() {
        Amount amount = Amount.inEUR(3.14);
        String message =
                "Tink test, this message is longer than the shortest one but still not of max length";

        Transfer transfer = createTransfer(amount, message);
        ValidateInternalTransferBody body =
                new ValidateInternalTransferBody(transfer, "fromAccount", "toAccount");

        Assertions.assertThat(body.get("amount").get(0)).isEqualTo("+000000000000003142");
        Assertions.assertThat(body.get("commLine1").get(0))
                .isEqualTo("Tink test, this message is longer t");
        Assertions.assertThat(body.get("commLine2").get(0))
                .isEqualTo("han the shortest one but still not ");
        Assertions.assertThat(body.get("commLine3").get(0)).isEqualTo("of max length");
        Assertions.assertThat(body.get("commLine4").get(0)).isEqualTo("");
        Assertions.assertThat(body.get("memoDate").get(0)).isEqualTo("20180417");
    }

    @Test
    public void testLongerThanMaxMessageLengthFormatting() {
        Amount amount = Amount.inEUR(2500.50);
        String message =
                "Tink test, this message is longer than the allowed message length of 140 chars, making sure that the message is divided and truncated appropriately";

        Transfer transfer = createTransfer(amount, message);
        ValidateInternalTransferBody body =
                new ValidateInternalTransferBody(transfer, "fromAccount", "toAccount");

        Assertions.assertThat(body.get("amount").get(0)).isEqualTo("+000000000002500502");
        Assertions.assertThat(body.get("commLine1").get(0))
                .isEqualTo("Tink test, this message is longer t");
        Assertions.assertThat(body.get("commLine2").get(0))
                .isEqualTo("han the allowed message length of 1");
        Assertions.assertThat(body.get("commLine3").get(0))
                .isEqualTo("40 chars, making sure that the mess");
        Assertions.assertThat(body.get("commLine4").get(0))
                .isEqualTo("age is divided and truncated approp");
        Assertions.assertThat(body.get("memoDate").get(0)).isEqualTo("20180417");
    }

    private Transfer createTransfer(Amount amount, String message) {
        Transfer t = new Transfer();
        t.setType(TransferType.BANK_TRANSFER);
        t.setAmount(amount);
        t.setDestinationMessage(message);
        t.setMessageType(MessageType.FREE_TEXT);
        t.setDueDate(DateUtils.parseDate("20180417"));

        return t;
    }
}
