
package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MonitoredObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MonitoredObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Pnr" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ConsumerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="CustomerNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FreeText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PortfolioName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Changes" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}ArrayOfAnyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MonitoredObject", propOrder = {
    "pnr",
    "consumerName",
    "customerNo",
    "freeText",
    "portfolioName",
    "changes"
})
public class MonitoredObject {

    @XmlElement(name = "Pnr")
    protected String pnr;
    @XmlElement(name = "ConsumerName")
    protected String consumerName;
    @XmlElement(name = "CustomerNo")
    protected String customerNo;
    @XmlElement(name = "FreeText")
    protected String freeText;
    @XmlElement(name = "PortfolioName")
    protected String portfolioName;
    @XmlElement(name = "Changes")
    protected ArrayOfAnyType changes;

    /**
     * Gets the value of the pnr property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPnr() {
        return pnr;
    }

    /**
     * Sets the value of the pnr property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPnr(String value) {
        this.pnr = value;
    }

    /**
     * Gets the value of the consumerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConsumerName() {
        return consumerName;
    }

    /**
     * Sets the value of the consumerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConsumerName(String value) {
        this.consumerName = value;
    }

    /**
     * Gets the value of the customerNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerNo() {
        return customerNo;
    }

    /**
     * Sets the value of the customerNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerNo(String value) {
        this.customerNo = value;
    }

    /**
     * Gets the value of the freeText property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFreeText() {
        return freeText;
    }

    /**
     * Sets the value of the freeText property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFreeText(String value) {
        this.freeText = value;
    }

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
     * Gets the value of the changes property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfAnyType }
     *     
     */
    public ArrayOfAnyType getChanges() {
        return changes;
    }

    /**
     * Sets the value of the changes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfAnyType }
     *     
     */
    public void setChanges(ArrayOfAnyType value) {
        this.changes = value;
    }

}
