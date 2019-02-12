
package se.tink.libraries.creditsafe.consumermonitoring.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetPortfolioInformationResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getPortfolioInformationResult"
})
@XmlRootElement(name = "GetPortfolioInformationResponse")
public class GetPortfolioInformationResponse {

    @XmlElement(name = "GetPortfolioInformationResult")
    protected MonitoringResponse getPortfolioInformationResult;

    /**
     * Gets the value of the getPortfolioInformationResult property.
     * 
     * @return
     *     possible object is
     *     {@link MonitoringResponse }
     *     
     */
    public MonitoringResponse getGetPortfolioInformationResult() {
        return getPortfolioInformationResult;
    }

    /**
     * Sets the value of the getPortfolioInformationResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonitoringResponse }
     *     
     */
    public void setGetPortfolioInformationResult(MonitoringResponse value) {
        this.getPortfolioInformationResult = value;
    }

}
