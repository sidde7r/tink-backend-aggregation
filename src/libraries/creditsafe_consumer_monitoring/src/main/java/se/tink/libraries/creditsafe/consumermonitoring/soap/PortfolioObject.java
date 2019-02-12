
package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PortfolioObject complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PortfolioObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PortfolioName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="IsStandard" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortfolioObject", propOrder = {
    "portfolioName",
    "isStandard"
})
public class PortfolioObject {

    @XmlElement(name = "PortfolioName")
    protected String portfolioName;
    @XmlElement(name = "IsStandard")
    protected boolean isStandard;

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

}
