//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.03.20 at 11:40:02 AM CET
//

package se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Java class for Document complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Document">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BkToCstmrAcctRpt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BankToCustomerAccountReportV02"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "Document",
        propOrder = {"bkToCstmrAcctRpt"})
public class Document {

    @XmlElement(name = "BkToCstmrAcctRpt", required = true)
    protected BankToCustomerAccountReportV02 bkToCstmrAcctRpt;

    /**
     * Gets the value of the bkToCstmrAcctRpt property.
     *
     * @return possible object is {@link BankToCustomerAccountReportV02 }
     */
    public BankToCustomerAccountReportV02 getBkToCstmrAcctRpt() {
        return bkToCstmrAcctRpt;
    }

    /**
     * Sets the value of the bkToCstmrAcctRpt property.
     *
     * @param value allowed object is {@link BankToCustomerAccountReportV02 }
     */
    public void setBkToCstmrAcctRpt(BankToCustomerAccountReportV02 value) {
        this.bkToCstmrAcctRpt = value;
    }
}
