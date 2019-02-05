
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
 *         &lt;element name="DisableCardResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
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
    "disableCardResult"
})
@XmlRootElement(name = "DisableCardResponse")
public class DisableCardResponse {

    @XmlElement(name = "DisableCardResult")
    protected boolean disableCardResult;

    /**
     * Gets the value of the disableCardResult property.
     * 
     */
    public boolean isDisableCardResult() {
        return disableCardResult;
    }

    /**
     * Sets the value of the disableCardResult property.
     * 
     */
    public void setDisableCardResult(boolean value) {
        this.disableCardResult = value;
    }

}
