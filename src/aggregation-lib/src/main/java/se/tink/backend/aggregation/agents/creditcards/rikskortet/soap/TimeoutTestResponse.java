
package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

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
 *         &lt;element name="TimeoutTestResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "timeoutTestResult"
})
@XmlRootElement(name = "TimeoutTestResponse")
public class TimeoutTestResponse {

    @XmlElement(name = "TimeoutTestResult")
    protected boolean timeoutTestResult;

    /**
     * Gets the value of the timeoutTestResult property.
     * 
     */
    public boolean isTimeoutTestResult() {
        return timeoutTestResult;
    }

    /**
     * Sets the value of the timeoutTestResult property.
     * 
     */
    public void setTimeoutTestResult(boolean value) {
        this.timeoutTestResult = value;
    }

}
