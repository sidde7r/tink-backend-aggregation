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
 * Java class for ProprietaryQuantity1 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ProprietaryQuantity1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Tp" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}Max35Text"/>
 *         &lt;element name="Qty" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}Max35Text"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "ProprietaryQuantity1",
        propOrder = {"tp", "qty"})
public class ProprietaryQuantity1 {

    @XmlElement(name = "Tp", required = true)
    protected String tp;

    @XmlElement(name = "Qty", required = true)
    protected String qty;

    /**
     * Gets the value of the tp property.
     *
     * @return possible object is {@link String }
     */
    public String getTp() {
        return tp;
    }

    /**
     * Sets the value of the tp property.
     *
     * @param value allowed object is {@link String }
     */
    public void setTp(String value) {
        this.tp = value;
    }

    /**
     * Gets the value of the qty property.
     *
     * @return possible object is {@link String }
     */
    public String getQty() {
        return qty;
    }

    /**
     * Sets the value of the qty property.
     *
     * @param value allowed object is {@link String }
     */
    public void setQty(String value) {
        this.qty = value;
    }
}
