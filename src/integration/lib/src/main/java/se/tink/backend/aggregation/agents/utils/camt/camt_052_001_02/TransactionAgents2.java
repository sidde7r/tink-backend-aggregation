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
import java.util.ArrayList;
import java.util.List;

/**
 * Java class for TransactionAgents2 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TransactionAgents2">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DbtrAgt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BranchAndFinancialInstitutionIdentification4" minOccurs="0"/>
 *         &lt;element name="CdtrAgt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BranchAndFinancialInstitutionIdentification4" minOccurs="0"/>
 *         &lt;element name="IntrmyAgt1" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BranchAndFinancialInstitutionIdentification4" minOccurs="0"/>
 *         &lt;element name="IntrmyAgt2" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BranchAndFinancialInstitutionIdentification4" minOccurs="0"/>
 *         &lt;element name="IntrmyAgt3" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BranchAndFinancialInstitutionIdentification4" minOccurs="0"/>
 *         &lt;element name="RcvgAgt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BranchAndFinancialInstitutionIdentification4" minOccurs="0"/>
 *         &lt;element name="DlvrgAgt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BranchAndFinancialInstitutionIdentification4" minOccurs="0"/>
 *         &lt;element name="IssgAgt" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BranchAndFinancialInstitutionIdentification4" minOccurs="0"/>
 *         &lt;element name="SttlmPlc" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}BranchAndFinancialInstitutionIdentification4" minOccurs="0"/>
 *         &lt;element name="Prtry" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}ProprietaryAgent2" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "TransactionAgents2",
        propOrder = {
            "dbtrAgt",
            "cdtrAgt",
            "intrmyAgt1",
            "intrmyAgt2",
            "intrmyAgt3",
            "rcvgAgt",
            "dlvrgAgt",
            "issgAgt",
            "sttlmPlc",
            "prtry"
        })
public class TransactionAgents2 {

    @XmlElement(name = "DbtrAgt")
    protected BranchAndFinancialInstitutionIdentification4 dbtrAgt;

    @XmlElement(name = "CdtrAgt")
    protected BranchAndFinancialInstitutionIdentification4 cdtrAgt;

    @XmlElement(name = "IntrmyAgt1")
    protected BranchAndFinancialInstitutionIdentification4 intrmyAgt1;

    @XmlElement(name = "IntrmyAgt2")
    protected BranchAndFinancialInstitutionIdentification4 intrmyAgt2;

    @XmlElement(name = "IntrmyAgt3")
    protected BranchAndFinancialInstitutionIdentification4 intrmyAgt3;

    @XmlElement(name = "RcvgAgt")
    protected BranchAndFinancialInstitutionIdentification4 rcvgAgt;

    @XmlElement(name = "DlvrgAgt")
    protected BranchAndFinancialInstitutionIdentification4 dlvrgAgt;

    @XmlElement(name = "IssgAgt")
    protected BranchAndFinancialInstitutionIdentification4 issgAgt;

    @XmlElement(name = "SttlmPlc")
    protected BranchAndFinancialInstitutionIdentification4 sttlmPlc;

    @XmlElement(name = "Prtry")
    protected List<ProprietaryAgent2> prtry;

    /**
     * Gets the value of the dbtrAgt property.
     *
     * @return possible object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public BranchAndFinancialInstitutionIdentification4 getDbtrAgt() {
        return dbtrAgt;
    }

    /**
     * Sets the value of the dbtrAgt property.
     *
     * @param value allowed object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public void setDbtrAgt(BranchAndFinancialInstitutionIdentification4 value) {
        this.dbtrAgt = value;
    }

    /**
     * Gets the value of the cdtrAgt property.
     *
     * @return possible object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public BranchAndFinancialInstitutionIdentification4 getCdtrAgt() {
        return cdtrAgt;
    }

    /**
     * Sets the value of the cdtrAgt property.
     *
     * @param value allowed object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public void setCdtrAgt(BranchAndFinancialInstitutionIdentification4 value) {
        this.cdtrAgt = value;
    }

    /**
     * Gets the value of the intrmyAgt1 property.
     *
     * @return possible object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public BranchAndFinancialInstitutionIdentification4 getIntrmyAgt1() {
        return intrmyAgt1;
    }

    /**
     * Sets the value of the intrmyAgt1 property.
     *
     * @param value allowed object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public void setIntrmyAgt1(BranchAndFinancialInstitutionIdentification4 value) {
        this.intrmyAgt1 = value;
    }

    /**
     * Gets the value of the intrmyAgt2 property.
     *
     * @return possible object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public BranchAndFinancialInstitutionIdentification4 getIntrmyAgt2() {
        return intrmyAgt2;
    }

    /**
     * Sets the value of the intrmyAgt2 property.
     *
     * @param value allowed object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public void setIntrmyAgt2(BranchAndFinancialInstitutionIdentification4 value) {
        this.intrmyAgt2 = value;
    }

    /**
     * Gets the value of the intrmyAgt3 property.
     *
     * @return possible object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public BranchAndFinancialInstitutionIdentification4 getIntrmyAgt3() {
        return intrmyAgt3;
    }

    /**
     * Sets the value of the intrmyAgt3 property.
     *
     * @param value allowed object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public void setIntrmyAgt3(BranchAndFinancialInstitutionIdentification4 value) {
        this.intrmyAgt3 = value;
    }

    /**
     * Gets the value of the rcvgAgt property.
     *
     * @return possible object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public BranchAndFinancialInstitutionIdentification4 getRcvgAgt() {
        return rcvgAgt;
    }

    /**
     * Sets the value of the rcvgAgt property.
     *
     * @param value allowed object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public void setRcvgAgt(BranchAndFinancialInstitutionIdentification4 value) {
        this.rcvgAgt = value;
    }

    /**
     * Gets the value of the dlvrgAgt property.
     *
     * @return possible object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public BranchAndFinancialInstitutionIdentification4 getDlvrgAgt() {
        return dlvrgAgt;
    }

    /**
     * Sets the value of the dlvrgAgt property.
     *
     * @param value allowed object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public void setDlvrgAgt(BranchAndFinancialInstitutionIdentification4 value) {
        this.dlvrgAgt = value;
    }

    /**
     * Gets the value of the issgAgt property.
     *
     * @return possible object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public BranchAndFinancialInstitutionIdentification4 getIssgAgt() {
        return issgAgt;
    }

    /**
     * Sets the value of the issgAgt property.
     *
     * @param value allowed object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public void setIssgAgt(BranchAndFinancialInstitutionIdentification4 value) {
        this.issgAgt = value;
    }

    /**
     * Gets the value of the sttlmPlc property.
     *
     * @return possible object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public BranchAndFinancialInstitutionIdentification4 getSttlmPlc() {
        return sttlmPlc;
    }

    /**
     * Sets the value of the sttlmPlc property.
     *
     * @param value allowed object is {@link BranchAndFinancialInstitutionIdentification4 }
     */
    public void setSttlmPlc(BranchAndFinancialInstitutionIdentification4 value) {
        this.sttlmPlc = value;
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
     * <p>Objects of the following type(s) are allowed in the list {@link ProprietaryAgent2 }
     */
    public List<ProprietaryAgent2> getPrtry() {
        if (prtry == null) {
            prtry = new ArrayList<>();
        }
        return this.prtry;
    }
}
