package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PutFavouriteResult" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "",
        propOrder = {"putFavouriteResult"})
@XmlRootElement(name = "PutFavouriteResponse")
public class PutFavouriteResponse {

    @XmlElement(name = "PutFavouriteResult")
    protected boolean putFavouriteResult;

    /** Gets the value of the putFavouriteResult property. */
    public boolean isPutFavouriteResult() {
        return putFavouriteResult;
    }

    /** Sets the value of the putFavouriteResult property. */
    public void setPutFavouriteResult(boolean value) {
        this.putFavouriteResult = value;
    }
}
