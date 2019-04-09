package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for Transaction complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Transaction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Last" type="{http://edenred.se/}ArrayOfTransactionDetails" minOccurs="0"/>
 *         &lt;element name="Day" type="{http://edenred.se/}ArrayOfTransactionDetails" minOccurs="0"/>
 *         &lt;element name="Week" type="{http://edenred.se/}ArrayOfTransactionDetails" minOccurs="0"/>
 *         &lt;element name="Month" type="{http://edenred.se/}ArrayOfTransactionDetails" minOccurs="0"/>
 *         &lt;element name="Year" type="{http://edenred.se/}ArrayOfTransactionDetails" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "Transaction",
        propOrder = {"last", "day", "week", "month", "year"})
public class Transaction {

    @XmlElement(name = "Last")
    protected ArrayOfTransactionDetails last;

    @XmlElement(name = "Day")
    protected ArrayOfTransactionDetails day;

    @XmlElement(name = "Week")
    protected ArrayOfTransactionDetails week;

    @XmlElement(name = "Month")
    protected ArrayOfTransactionDetails month;

    @XmlElement(name = "Year")
    protected ArrayOfTransactionDetails year;

    /**
     * Gets the value of the last property.
     *
     * @return possible object is {@link ArrayOfTransactionDetails }
     */
    public ArrayOfTransactionDetails getLast() {
        return last;
    }

    /**
     * Sets the value of the last property.
     *
     * @param value allowed object is {@link ArrayOfTransactionDetails }
     */
    public void setLast(ArrayOfTransactionDetails value) {
        this.last = value;
    }

    /**
     * Gets the value of the day property.
     *
     * @return possible object is {@link ArrayOfTransactionDetails }
     */
    public ArrayOfTransactionDetails getDay() {
        return day;
    }

    /**
     * Sets the value of the day property.
     *
     * @param value allowed object is {@link ArrayOfTransactionDetails }
     */
    public void setDay(ArrayOfTransactionDetails value) {
        this.day = value;
    }

    /**
     * Gets the value of the week property.
     *
     * @return possible object is {@link ArrayOfTransactionDetails }
     */
    public ArrayOfTransactionDetails getWeek() {
        return week;
    }

    /**
     * Sets the value of the week property.
     *
     * @param value allowed object is {@link ArrayOfTransactionDetails }
     */
    public void setWeek(ArrayOfTransactionDetails value) {
        this.week = value;
    }

    /**
     * Gets the value of the month property.
     *
     * @return possible object is {@link ArrayOfTransactionDetails }
     */
    public ArrayOfTransactionDetails getMonth() {
        return month;
    }

    /**
     * Sets the value of the month property.
     *
     * @param value allowed object is {@link ArrayOfTransactionDetails }
     */
    public void setMonth(ArrayOfTransactionDetails value) {
        this.month = value;
    }

    /**
     * Gets the value of the year property.
     *
     * @return possible object is {@link ArrayOfTransactionDetails }
     */
    public ArrayOfTransactionDetails getYear() {
        return year;
    }

    /**
     * Sets the value of the year property.
     *
     * @param value allowed object is {@link ArrayOfTransactionDetails }
     */
    public void setYear(ArrayOfTransactionDetails value) {
        this.year = value;
    }
}
