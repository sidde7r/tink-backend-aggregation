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
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Java class for TransactionDates2 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TransactionDates2">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AccptncDtTm" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ISODateTime" minOccurs="0"/>
 *         &lt;element name="TradActvtyCtrctlSttlmDt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ISODate" minOccurs="0"/>
 *         &lt;element name="TradDt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ISODate" minOccurs="0"/>
 *         &lt;element name="IntrBkSttlmDt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ISODate" minOccurs="0"/>
 *         &lt;element name="StartDt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ISODate" minOccurs="0"/>
 *         &lt;element name="EndDt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ISODate" minOccurs="0"/>
 *         &lt;element name="TxDtTm" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ISODateTime" minOccurs="0"/>
 *         &lt;element name="Prtry" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ProprietaryDate2" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "TransactionDates2",
        propOrder = {
            "accptncDtTm",
            "tradActvtyCtrctlSttlmDt",
            "tradDt",
            "intrBkSttlmDt",
            "startDt",
            "endDt",
            "txDtTm",
            "prtry"
        })
public class TransactionDates2 {

    @XmlElement(name = "AccptncDtTm")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar accptncDtTm;

    @XmlElement(name = "TradActvtyCtrctlSttlmDt")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar tradActvtyCtrctlSttlmDt;

    @XmlElement(name = "TradDt")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar tradDt;

    @XmlElement(name = "IntrBkSttlmDt")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar intrBkSttlmDt;

    @XmlElement(name = "StartDt")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar startDt;

    @XmlElement(name = "EndDt")
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar endDt;

    @XmlElement(name = "TxDtTm")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar txDtTm;

    @XmlElement(name = "Prtry")
    protected List<ProprietaryDate2> prtry;

    /**
     * Gets the value of the accptncDtTm property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getAccptncDtTm() {
        return accptncDtTm;
    }

    /**
     * Sets the value of the accptncDtTm property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setAccptncDtTm(XMLGregorianCalendar value) {
        this.accptncDtTm = value;
    }

    /**
     * Gets the value of the tradActvtyCtrctlSttlmDt property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getTradActvtyCtrctlSttlmDt() {
        return tradActvtyCtrctlSttlmDt;
    }

    /**
     * Sets the value of the tradActvtyCtrctlSttlmDt property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setTradActvtyCtrctlSttlmDt(XMLGregorianCalendar value) {
        this.tradActvtyCtrctlSttlmDt = value;
    }

    /**
     * Gets the value of the tradDt property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getTradDt() {
        return tradDt;
    }

    /**
     * Sets the value of the tradDt property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setTradDt(XMLGregorianCalendar value) {
        this.tradDt = value;
    }

    /**
     * Gets the value of the intrBkSttlmDt property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getIntrBkSttlmDt() {
        return intrBkSttlmDt;
    }

    /**
     * Sets the value of the intrBkSttlmDt property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setIntrBkSttlmDt(XMLGregorianCalendar value) {
        this.intrBkSttlmDt = value;
    }

    /**
     * Gets the value of the startDt property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getStartDt() {
        return startDt;
    }

    /**
     * Sets the value of the startDt property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setStartDt(XMLGregorianCalendar value) {
        this.startDt = value;
    }

    /**
     * Gets the value of the endDt property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getEndDt() {
        return endDt;
    }

    /**
     * Sets the value of the endDt property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setEndDt(XMLGregorianCalendar value) {
        this.endDt = value;
    }

    /**
     * Gets the value of the txDtTm property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getTxDtTm() {
        return txDtTm;
    }

    /**
     * Sets the value of the txDtTm property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setTxDtTm(XMLGregorianCalendar value) {
        this.txDtTm = value;
    }

    /**
     * Gets the value of the prtry property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the prtry property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getPrtry().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link ProprietaryDate2 }
     */
    public List<ProprietaryDate2> getPrtry() {
        if (prtry == null) {
            prtry = new ArrayList<>();
        }
        return this.prtry;
    }
}
