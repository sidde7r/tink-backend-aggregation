
package se.tink.backend.aggregation.agents.fraud.creditsafe.soap.consumermonitoring;

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
 *         &lt;element name="UpdatePortfolioResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
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
    "updatePortfolioResult"
})
@XmlRootElement(name = "UpdatePortfolioResponse")
public class UpdatePortfolioResponse {

    @XmlElement(name = "UpdatePortfolioResult")
    protected MonitoringResponse updatePortfolioResult;

    /**
     * Gets the value of the updatePortfolioResult property.
     * 
     * @return
     *     possible object is
     *     {@link MonitoringResponse }
     *     
     */
    public MonitoringResponse getUpdatePortfolioResult() {
        return updatePortfolioResult;
    }

    /**
     * Sets the value of the updatePortfolioResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonitoringResponse }
     *     
     */
    public void setUpdatePortfolioResult(MonitoringResponse value) {
        this.updatePortfolioResult = value;
    }

}
