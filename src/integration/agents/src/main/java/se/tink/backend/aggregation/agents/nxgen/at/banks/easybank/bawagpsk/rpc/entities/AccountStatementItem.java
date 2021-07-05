package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.entities;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.time.LocalDate;
import java.util.List;

public class AccountStatementItem {
    private int position;
    private AmountEntity amountEntity;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private List<String> textLines;

    @XmlElement(name = "Position")
    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @XmlElement(name = "Amount")
    public void setAmountEntity(AmountEntity amountEntity) {
        this.amountEntity = amountEntity;
    }

    public AmountEntity getAmountEntity() {
        return amountEntity;
    }

    @XmlElement(name = "BookingDate")
    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    @XmlElement(name = "ValueDate")
    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    @XmlElementWrapper(name = "TextLines")
    @XmlElement(name = "Text")
    public void setTextLines(List<String> textLines) {
        this.textLines = textLines;
    }

    public List<String> getTextLines() {
        return textLines;
    }
}
