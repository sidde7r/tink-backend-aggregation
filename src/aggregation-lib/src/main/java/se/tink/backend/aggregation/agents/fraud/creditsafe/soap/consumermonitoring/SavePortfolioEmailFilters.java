
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
 *         &lt;element name="mon_req" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}MonitoringRequest" minOccurs="0"/>
 *         &lt;element name="filters" type="{https://webservice.creditsafe.se/ConsumerMonitoring/}ArrayOfEmailFilterObject" minOccurs="0"/>
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
    "monReq",
    "filters"
})
@XmlRootElement(name = "SavePortfolioEmailFilters")
public class SavePortfolioEmailFilters {

    @XmlElement(name = "mon_req")
    protected MonitoringRequest monReq;
    protected ArrayOfEmailFilterObject filters;

    /**
     * Gets the value of the monReq property.
     * 
     * @return
     *     possible object is
     *     {@link MonitoringRequest }
     *     
     */
    public MonitoringRequest getMonReq() {
        return monReq;
    }

    /**
     * Sets the value of the monReq property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonitoringRequest }
     *     
     */
    public void setMonReq(MonitoringRequest value) {
        this.monReq = value;
    }

    /**
     * Gets the value of the filters property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfEmailFilterObject }
     *     
     */
    public ArrayOfEmailFilterObject getFilters() {
        return filters;
    }

    /**
     * Sets the value of the filters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfEmailFilterObject }
     *     
     */
    public void setFilters(ArrayOfEmailFilterObject value) {
        this.filters = value;
    }

}
