
package se.tink.backend.aggregation.credit.safe.creditsafe.soap.consumermonitoring;

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
 *         &lt;element name="GetPortfolioEmailFiltersResult" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringResponse" minOccurs="0"/>
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
    "getPortfolioEmailFiltersResult"
})
@XmlRootElement(name = "GetPortfolioEmailFiltersResponse")
public class GetPortfolioEmailFiltersResponse {

    @XmlElement(name = "GetPortfolioEmailFiltersResult")
    protected MonitoringResponse getPortfolioEmailFiltersResult;

    /**
     * Gets the value of the getPortfolioEmailFiltersResult property.
     * 
     * @return
     *     possible object is
     *     {@link MonitoringResponse }
     *     
     */
    public MonitoringResponse getGetPortfolioEmailFiltersResult() {
        return getPortfolioEmailFiltersResult;
    }

    /**
     * Sets the value of the getPortfolioEmailFiltersResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonitoringResponse }
     *     
     */
    public void setGetPortfolioEmailFiltersResult(MonitoringResponse value) {
        this.getPortfolioEmailFiltersResult = value;
    }

}
