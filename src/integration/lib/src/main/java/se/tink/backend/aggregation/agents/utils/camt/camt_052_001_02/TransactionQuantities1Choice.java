//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.03.20 at 11:40:02 AM CET
//

package se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for TransactionQuantities1Choice complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TransactionQuantities1Choice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="Qty" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}FinancialInstrumentQuantityChoice"/>
 *           &lt;element name="Prtry" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ProprietaryQuantity1"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "TransactionQuantities1Choice",
        propOrder = {"qty", "prtry"})
public class TransactionQuantities1Choice {

    @XmlElement(name = "Qty")
    protected FinancialInstrumentQuantityChoice qty;

    @XmlElement(name = "Prtry")
    protected ProprietaryQuantity1 prtry;

    /**
     * Gets the value of the qty property.
     *
     * @return possible object is {@link FinancialInstrumentQuantityChoice }
     */
    public FinancialInstrumentQuantityChoice getQty() {
        return qty;
    }

    /**
     * Sets the value of the qty property.
     *
     * @param value allowed object is {@link FinancialInstrumentQuantityChoice }
     */
    public void setQty(FinancialInstrumentQuantityChoice value) {
        this.qty = value;
    }

    /**
     * Gets the value of the prtry property.
     *
     * @return possible object is {@link ProprietaryQuantity1 }
     */
    public ProprietaryQuantity1 getPrtry() {
        return prtry;
    }

    /**
     * Sets the value of the prtry property.
     *
     * @param value allowed object is {@link ProprietaryQuantity1 }
     */
    public void setPrtry(ProprietaryQuantity1 value) {
        this.prtry = value;
    }
}
