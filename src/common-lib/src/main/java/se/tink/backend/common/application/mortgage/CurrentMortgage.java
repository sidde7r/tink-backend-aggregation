package se.tink.backend.common.application.mortgage;

import java.util.Date;
import java.util.List;

public class CurrentMortgage {
    
    public class CurrentMortgagePart {
        private double amount;
        private Date firstSeen;
        private String id;
        private Date initialDate;
        private double interestRate;
        
        public double getAmount() {
            return amount;
        }
        
        public Date getFirstSeen() {
            return firstSeen;
        }
        
        public String getId() {
            return id;
        }
        
        public Date getInitialDate() {
            return initialDate;
        }
        
        public double getInterestRate() {
            return interestRate;
        }
        
        public void setAmount(double amount) {
            this.amount = amount;
        }
        
        public void setFirstSeen(Date firstSeen) {
            this.firstSeen = firstSeen;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public void setInitialDate(Date initialDate) {
            this.initialDate = initialDate;
        }
        
        public void setInterestRate(double interestRate) {
            this.interestRate = interestRate;
        }
    }
    
    private double amount;
    private double interestRate;
    private List<CurrentMortgagePart> loanParts;
    private String providerDisplayName;
    private String providerName;
    
    public double getAmount() {
        return amount;
    }
    
    public double getInterestRate() {
        return interestRate;
    }
    
    public List<CurrentMortgagePart> getLoanParts() {
        return loanParts;
    }
    
    public String getProviderDisplayName() {
        return providerDisplayName;
    }
    
    public String getProviderName() {
        return providerName;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
    
    public void setLoanParts(List<CurrentMortgagePart> loanParts) {
        this.loanParts = loanParts;
    }
    
    public void setProviderDisplayName(String displayName) {
        this.providerDisplayName = displayName;
    }
    
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
