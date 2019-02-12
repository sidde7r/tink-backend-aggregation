
package se.tink.libraries.creditsafe.consumermonitoring.soap;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PortfolioInfoObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PortfolioInfoObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PortfolioName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="EmailSubject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IsStandard" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="UsesStdFilter" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Created" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Changed" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="LastEmailDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SendNoEmail" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Monitored" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="EmailAdressList" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}ArrayOfString" minOccurs="0"/>
 *         &lt;element name="FilterObject" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}ArrayOfAnyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortfolioInfoObject", propOrder = {
    "portfolioName",
    "emailSubject",
    "isStandard",
    "usesStdFilter",
    "created",
    "changed",
    "lastEmailDate",
    "sendNoEmail",
    "monitored",
    "emailAdressList",
    "filterObject"
})
public class PortfolioInfoObject {

    @XmlElement(name = "PortfolioName")
    protected String portfolioName;
    @XmlElement(name = "EmailSubject")
    protected String emailSubject;
    @XmlElement(name = "IsStandard")
    protected boolean isStandard;
    @XmlElement(name = "UsesStdFilter")
    protected boolean usesStdFilter;
    @XmlElement(name = "Created")
    protected String created;
    @XmlElement(name = "Changed")
    protected String changed;
    @XmlElement(name = "LastEmailDate")
    protected String lastEmailDate;
    @XmlElement(name = "SendNoEmail")
    protected boolean sendNoEmail;
    @XmlElement(name = "Monitored", required = true)
    protected BigDecimal monitored;
    @XmlElement(name = "EmailAdressList")
    protected ArrayOfString emailAdressList;
    @XmlElement(name = "FilterObject")
    protected ArrayOfAnyType filterObject;

    /**
     * Gets the value of the portfolioName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPortfolioName() {
        return portfolioName;
    }

    /**
     * Sets the value of the portfolioName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPortfolioName(String value) {
        this.portfolioName = value;
    }

    /**
     * Gets the value of the emailSubject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmailSubject() {
        return emailSubject;
    }

    /**
     * Sets the value of the emailSubject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmailSubject(String value) {
        this.emailSubject = value;
    }

    /**
     * Gets the value of the isStandard property.
     * 
     */
    public boolean isIsStandard() {
        return isStandard;
    }

    /**
     * Sets the value of the isStandard property.
     * 
     */
    public void setIsStandard(boolean value) {
        this.isStandard = value;
    }

    /**
     * Gets the value of the usesStdFilter property.
     * 
     */
    public boolean isUsesStdFilter() {
        return usesStdFilter;
    }

    /**
     * Sets the value of the usesStdFilter property.
     * 
     */
    public void setUsesStdFilter(boolean value) {
        this.usesStdFilter = value;
    }

    /**
     * Gets the value of the created property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreated() {
        return created;
    }

    /**
     * Sets the value of the created property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreated(String value) {
        this.created = value;
    }

    /**
     * Gets the value of the changed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChanged() {
        return changed;
    }

    /**
     * Sets the value of the changed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChanged(String value) {
        this.changed = value;
    }

    /**
     * Gets the value of the lastEmailDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastEmailDate() {
        return lastEmailDate;
    }

    /**
     * Sets the value of the lastEmailDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastEmailDate(String value) {
        this.lastEmailDate = value;
    }

    /**
     * Gets the value of the sendNoEmail property.
     * 
     */
    public boolean isSendNoEmail() {
        return sendNoEmail;
    }

    /**
     * Sets the value of the sendNoEmail property.
     * 
     */
    public void setSendNoEmail(boolean value) {
        this.sendNoEmail = value;
    }

    /**
     * Gets the value of the monitored property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMonitored() {
        return monitored;
    }

    /**
     * Sets the value of the monitored property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMonitored(BigDecimal value) {
        this.monitored = value;
    }

    /**
     * Gets the value of the emailAdressList property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getEmailAdressList() {
        return emailAdressList;
    }

    /**
     * Sets the value of the emailAdressList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setEmailAdressList(ArrayOfString value) {
        this.emailAdressList = value;
    }

    /**
     * Gets the value of the filterObject property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfAnyType }
     *     
     */
    public ArrayOfAnyType getFilterObject() {
        return filterObject;
    }

    /**
     * Sets the value of the filterObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfAnyType }
     *     
     */
    public void setFilterObject(ArrayOfAnyType value) {
        this.filterObject = value;
    }

}
