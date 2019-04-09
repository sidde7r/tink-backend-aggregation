package se.tink.backend.aggregation.agents.creditcards.rikskortet.soap;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for EmployeeTransactionType.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <p>
 *
 * <pre>
 * &lt;simpleType name="EmployeeTransactionType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Activation"/>
 *     &lt;enumeration value="Balance"/>
 *     &lt;enumeration value="CashAdvance"/>
 *     &lt;enumeration value="Fee"/>
 *     &lt;enumeration value="MoneyBack"/>
 *     &lt;enumeration value="PinChange"/>
 *     &lt;enumeration value="Redemption"/>
 *     &lt;enumeration value="Refund"/>
 *     &lt;enumeration value="TopUp"/>
 *     &lt;enumeration value="BalanceTransferCredit"/>
 *     &lt;enumeration value="BalanceTransferDebit"/>
 *     &lt;enumeration value="Withdrawl"/>
 *     &lt;enumeration value="Deposit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "EmployeeTransactionType")
@XmlEnum
public enum EmployeeTransactionType {
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("Activation")
    ACTIVATION("Activation"),
    @XmlEnumValue("Balance")
    BALANCE("Balance"),
    @XmlEnumValue("CashAdvance")
    CASH_ADVANCE("CashAdvance"),
    @XmlEnumValue("Fee")
    FEE("Fee"),
    @XmlEnumValue("MoneyBack")
    MONEY_BACK("MoneyBack"),
    @XmlEnumValue("PinChange")
    PIN_CHANGE("PinChange"),
    @XmlEnumValue("Redemption")
    REDEMPTION("Redemption"),
    @XmlEnumValue("Refund")
    REFUND("Refund"),
    @XmlEnumValue("TopUp")
    TOP_UP("TopUp"),
    @XmlEnumValue("BalanceTransferCredit")
    BALANCE_TRANSFER_CREDIT("BalanceTransferCredit"),
    @XmlEnumValue("BalanceTransferDebit")
    BALANCE_TRANSFER_DEBIT("BalanceTransferDebit"),
    @XmlEnumValue("Withdrawl")
    WITHDRAWL("Withdrawl"),
    @XmlEnumValue("Deposit")
    DEPOSIT("Deposit");
    private final String value;

    EmployeeTransactionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EmployeeTransactionType fromValue(String v) {
        for (EmployeeTransactionType c : EmployeeTransactionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
