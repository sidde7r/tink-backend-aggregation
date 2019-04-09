package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetTransactionsResult" type="{http://edenred.se/}Transaction" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"getTransactionsResult"})
@XmlRootElement(name = "GetTransactionsResponse")
public class GetTransactionsResponse {

    @XmlElement(name = "GetTransactionsResult")
    protected Transaction getTransactionsResult;

    /**
     * Gets the value of the getTransactionsResult property.
     *
     * @return possible object is {@link Transaction }
     */
    public Transaction getGetTransactionsResult() {
        return getTransactionsResult;
    }

    /**
     * Sets the value of the getTransactionsResult property.
     *
     * @param value allowed object is {@link Transaction }
     */
    public void setGetTransactionsResult(Transaction value) {
        this.getTransactionsResult = value;
    }
}
