package se.tink.backend.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "abnamro_customers")
@IdClass(AbnAmroCustomerPk.class)
public class AbnAmroCustomer {
    
    @Id
    @Column(name="`accountnumber`")
    private long accountNumber;
    @Id
    @Column(name="`cardnumber`")
    private long cardNumber;
    private String comment;
    
    public long getAccountNumber() {
        return accountNumber;
    }

    public long getCardNumber() {
        return cardNumber;
    }
    
    public String getComment() {
        return comment;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setCardNumber(long cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
}
