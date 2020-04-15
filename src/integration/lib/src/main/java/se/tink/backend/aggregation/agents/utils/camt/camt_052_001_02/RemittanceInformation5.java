//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2020.03.20 at 11:40:02 AM CET
//

package se.tink.backend.aggregation.agents.utils.camt.camt_052_001_02;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for RemittanceInformation5 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RemittanceInformation5">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Ustrd" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}Max140Text" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="Strd" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}StructuredRemittanceInformation7" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "RemittanceInformation5",
        propOrder = {"ustrd", "strd"})
public class RemittanceInformation5 {

    @XmlElement(name = "Ustrd")
    protected List<String> ustrd;

    @XmlElement(name = "Strd")
    protected List<StructuredRemittanceInformation7> strd;

    /**
     * Gets the value of the ustrd property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the ustrd property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getUstrd().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<String> getUstrd() {
        if (ustrd == null) {
            ustrd = new ArrayList<>();
        }
        return this.ustrd;
    }

    /**
     * Gets the value of the strd property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the strd property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getStrd().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link
     * StructuredRemittanceInformation7 }
     */
    public List<StructuredRemittanceInformation7> getStrd() {
        if (strd == null) {
            strd = new ArrayList<>();
        }
        return this.strd;
    }
}
