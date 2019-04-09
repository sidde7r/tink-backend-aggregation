package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for ArrayOfTransactionDetails complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ArrayOfTransactionDetails">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TransactionDetails" type="{http://edenred.se/}TransactionDetails" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ArrayOfTransactionDetails",
        propOrder = {"transactionDetails"})
public class ArrayOfTransactionDetails {

    @XmlElement(name = "TransactionDetails", nillable = true)
    protected List<TransactionDetails> transactionDetails;

    /**
     * Gets the value of the transactionDetails property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the transactionDetails property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getTransactionDetails().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link TransactionDetails }
     */
    public List<TransactionDetails> getTransactionDetails() {
        if (transactionDetails == null) {
            transactionDetails = new ArrayList<TransactionDetails>();
        }
        return this.transactionDetails;
    }
}
