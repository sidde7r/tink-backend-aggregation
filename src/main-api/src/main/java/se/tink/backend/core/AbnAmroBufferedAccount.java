package se.tink.backend.core;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "abnamro_buffered_accounts")
@IdClass(AbnAmroBufferedAccountPk.class)
public class AbnAmroBufferedAccount {
    
    @Id
    @Column(name="`accountnumber`")
    private long accountNumber;
    private boolean complete;
    @Id
    @Column(name="`credentialsid`")
    private String credentialsId;
    private int transactionCount;
    
    public long getAccountNumber() {
        return accountNumber;
    }
    
    public String getCredentialsId() {
        return credentialsId;
    }
    
    public int getTransactionCount() {
        return transactionCount;
    }
    
    public boolean isComplete() {
        return complete;
    }
    
    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public void setComplete(boolean complete) {
        this.complete = complete;
    }
    
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }
    
    public void setTransactionCount(int count) {
        this.transactionCount = count;
    }

    public static AbnAmroBufferedAccount create(String credentialsId, String bankId) {
        AbnAmroBufferedAccount bufferedAccount = new AbnAmroBufferedAccount();
        bufferedAccount.setCredentialsId(credentialsId);
        bufferedAccount.setAccountNumber(Long.valueOf(bankId));
        bufferedAccount.setTransactionCount(0);
        bufferedAccount.setComplete(false);

        return bufferedAccount;
    }
}
