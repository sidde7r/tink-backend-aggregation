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
 * Java class for GenericOrganisationIdentification1 complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GenericOrganisationIdentification1">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Id" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}Max35Text"/>
 *         &lt;element name="SchmeNm" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}OrganisationIdentificationSchemeName1Choice" minOccurs="0"/>
 *         &lt;element name="Issr" type="{urn:iso:std:iso:20022:tech:xsd:camt.052.001.02}Max35Text" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "GenericOrganisationIdentification1",
        propOrder = {"id", "schmeNm", "issr"})
public class GenericOrganisationIdentification1 {

    @XmlElement(name = "Id", required = true)
    protected String id;

    @XmlElement(name = "SchmeNm")
    protected OrganisationIdentificationSchemeName1Choice schmeNm;

    @XmlElement(name = "Issr")
    protected String issr;

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the schmeNm property.
     *
     * @return possible object is {@link OrganisationIdentificationSchemeName1Choice }
     */
    public OrganisationIdentificationSchemeName1Choice getSchmeNm() {
        return schmeNm;
    }

    /**
     * Sets the value of the schmeNm property.
     *
     * @param value allowed object is {@link OrganisationIdentificationSchemeName1Choice }
     */
    public void setSchmeNm(OrganisationIdentificationSchemeName1Choice value) {
        this.schmeNm = value;
    }

    /**
     * Gets the value of the issr property.
     *
     * @return possible object is {@link String }
     */
    public String getIssr() {
        return issr;
    }

    /**
     * Sets the value of the issr property.
     *
     * @param value allowed object is {@link String }
     */
    public void setIssr(String value) {
        this.issr = value;
    }
}
