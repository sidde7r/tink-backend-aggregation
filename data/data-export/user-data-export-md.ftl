<#--
For date-types, we user (mydate?datetime)! to tell freemarker that it's a date and that the default value is empty with !
-->
# User Details

## User
    Name:               ${userDetails.name}
    National ID:        ${userDetails.nationalId}
    Gender:             ${userDetails.gender}
    Market:             ${userDetails.market}
    Currency:           ${userDetails.currency}
    Username:           ${userDetails.username}
    Date Created:       ${userDetails.created}
<#if booleans.userDeleted>
    Deletion Time:      ${userDetails.deleted!""}
    Comment:            ${userDetails.comment}
    Reasons:            ${userDetails.reasons}
</#if>

<#if booleans.hasFacebook>
<#-- Show this only if user has facebook -->
## Facebook Details
    First Name:         ${fb.firstname}
    Last Name:          ${fb.lastname}
    Birthday:           ${fb.birthday}
    Email:              ${fb.email}
    Location:           ${fb.locationname}
    State:              ${fb.state}
    <#list fb.friends as friend>
    ${friend.name}
    </#list>
</#if>

<#if fraudDetails.fraudDetails?has_content>
## ID Control Details
<#list fraudDetails.fraudDetails as fraud>
### ID-Control ${fraud_index + 1}
    Type:               ${fraud.type}
    Status:             ${fraud.status}
    Details:            ${fraud.detailsContent}
    Date:               ${(fraud.date)!}
    Updated:            ${(fraud.updated)!}
</#list>
</#if>

<#if consents.consents?has_content>
## Consents
<#list consents.consents as consent>
### Consent ${consent_index + 1}
    Title:                  ${consent.title}
    Version:                ${consent.version}
    User Action:            ${consent.action}
    Payload:                ${consent.payload}
    Locale:                 ${consent.locale}
    Date:                   ${(consent.timestamp)!}
</#list>
</#if>

<#if userDevices.devices?has_content>
## Devices
<#list userDevices.devices as device>
### Device ${device_index + 1}
    Updated:            ${device.updated}
    Status:             ${device.status}
    Device details:     ${device.userAgent} <#-- note deserialize and check content-->
    Extra info:         ${device.payload}   <#-- note deserialize and check content-->
</#list>
</#if>

<#if userEvents.events?has_content>
## User Events
<#list userEvents.events as event>
    ${(event.date)!},  ${event.type},  ${event.remoteAddress}
</#list>
</#if>

<#if userLocations.locations?has_content>
## User Locations
<#list userLocations.locations as location>
    ${(location.date)!},  (Latitude, Longitude): (${location.latitude}, ${location.longitude})
</#list>
</#if>

## Settings
<#assign notif = userDetails.settings.notificationSettings>
<#if notif?has_content>
### Enabled Notifications:
| Balance | Budget | Double Charge | Income | Large Expense | Monthly Summary | Weekly Summary | Transaction | Unusual Account | Unusual Category | E-Invoices | ID-Check | Left to Spend | Loan Updated |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| ${notif.balance} | ${notif.budget} | ${notif.doubleCharge} | ${notif.income} | ${notif.largeExpense} | ${notif.summaryMonthly} | ${notif.summaryWeekly} | ${notif.transaction} | ${notif.unusualAccount} | ${notif.unusualCategory} | ${notif.einvoices} | ${notif.fraud} | ${notif.leftToSpend} | ${notif.loanUpdate} |
</#if>
<#if userDetails.subscriptions?has_content>
    Subscriptions:          <#list userDetails.subscriptions as subscription>${subscription.type}</#list>
</#if>

<#if applications.applications?has_content>
<#-- Note Repos from both applications and application_forms. Is it possible? -->
## Applications
<#list applications.applications as application>
### Application ${application_index + 1}
    Type:                   ${application.type}
    Status:                 ${application.status}
    Created:                ${(application.created)!}
    Updated:                ${(application.updated)!}
</#list>
</#if>

<#if applicationEvents.events?has_content>
## Application Events
<#list applicationEvents.events as event>
    ${(event.applicationUpdated)!} - ${event.applicationType} ${event.applicationStatus}
</#list>
</#if>

<#if documents.documents?has_content>
## Documents
<#list documents.documents as document>
### Document ${document_index + 1}:  ${document.mimeType}
</#list>
</#if>


# User Financial Data

<#if properties.properties?has_content>
## Residences
<#list properties.properties as property>
### Residence ${property_index + 1}
    Address:                ${property.address}
    City:                   ${property.city}
    Community:              ${property.community}
    Postal Code:            ${property.postalCode}
    Latitude:               ${(property.latitude)!}
    Longitude:              ${(property.longitude)!}
    Type:                   ${property.type}
    Status:                 ${property.status}
    Address Registered:     ${property.addressRegistered}
    Created:                ${(property.created)!}
    Number of Rooms:        ${(property.numberOfRooms)!}
    Number of sq. m:        ${(property.numberOfSquareMeters)!}
    Most Recent Valuation:  ${(property.mostRecentValuation)!}
</#list>
</#if>

<#if propertyEstimates.estimates?has_content>
## Booli Estimate
<#list propertyEstimates.estimates as estimate>
### Estimate ${estimate_index + 1}
    Total Area:                 ${estimate.additionalAndLivingArea}
    Additional Area:            ${estimate.additionalArea}
    Apartment Number:           ${estimate.apartmentNumber}
    Balcony:                    ${estimate.balcony}
    Bathroom Condition:         ${estimate.bathroomCondition}
    Has Elevator:               ${estimate.buildingHasElevator}
    Has Car Parking:            ${estimate.canParkCar}
    Ceiling Height:             ${estimate.ceilingHeight}
    Construction Era:           ${estimate.constructionEra}
    Construction Year:          ${estimate.constructionYear}
    Fireplace:                  ${estimate.fireplace}
    Floor:                      ${estimate.floor}
    Has Basement:               ${estimate.hasBasement}
    Kitchen Condition:          ${estimate.kitchenCondition}
    Knowledge:                  ${estimate.knowledge}
    Last Ground Drainage:       ${estimate.lastGroundDrainage}
    Last Roof Renovation:       ${estimate.lastRoofRenovation}
    Latitude:                   ${estimate.latitude}
    List Price:                 ${estimate.listPrice}
    Living Area:                ${estimate.livingArea}
    Longitude:                  ${estimate.longitude}
    Residence Type:             ${estimate.residenceType}
    Operating Cost:             ${estimate.operatingCost}
    Operating Cost Per Sqm:     ${estimate.operatingCostPerSqm}
    Patio:                      ${estimate.patio}
    Plot Area:                  ${estimate.plotArea}
    Rent:                       ${estimate.rent}
    Rent Per Sqm:               ${estimate.rentPerSqm}
    Rooms:                      ${estimate.rooms}
    Street Address:             ${estimate.streetAddress}
    Bidding Average Prediction: ${estimate.biddingAveragePrediction}
    Bidding Average Weight:     ${estimate.biddingAverageWeight}
    Difference Average:         ${estimate.differenceAverage}
    Difference Cv:              ${estimate.differenceCv}
    Knn Prediction:             ${estimate.knnPrediction}
    Knn Weight:                 ${estimate.knnWeight}
    Prediction Date:            ${estimate.predictionDate}
    Predictor:                  ${estimate.predictor}
    Previous Sale Prediction:   ${estimate.previousSalePrediction}
    Previous Sale Weight:       ${estimate.previousSaleWeight}
    Price Cv:                   ${estimate.priceCv}
    Recommendation:             ${estimate.recommendation}
    Accuracy:                   ${estimate.accuracy}
    Price:                      ${estimate.price}
    Price Range High:           ${estimate.priceRangeHigh}
    Price Range Low:            ${estimate.priceRangeLow}
    Sqm Price:                  ${estimate.sqmPrice}
    Sqm Price Range High:       ${estimate.sqmPriceRangeHigh}
    Sqm Price Range Low:        ${estimate.sqmPriceRangeLow}
    Number Of References:       ${estimate.numberOfReferences}
</#list>
</#if>

<#if credentials.credentials?has_content>
## Connected Services <#-- Credentials -->
<#list credentials.credentials as credential>
### Service ${credential_index + 1}
    Provider:               ${credential.providerName} <#-- use provider display name -->
    Payload:                ${credential.payload}       <#-- note deserialize and check if should use -->
    Fields:                 ${credential.fields}
</#list>
</#if>

<#if loans.loans?has_content>
## Loan Events
<#list loans.loans as loan>
### Loan Event ${loan_index + 1}
    Account:                ${loan.loanNumber} <#-- Note check if this is correct! -->
    Amortized:              ${(loan.amortized)!}
    Balance:                ${(loan.balance)!}
    Initial Balance:        ${(loan.initialBalance)!}
    Interest:               ${(loan.interest)!}
    Monthly Amortization:   ${(loan.monthlyAmortization)!}
    Name:                   ${loan.name}
    Initial Date:           ${(loan.initialDate)!}
    Day-of-Terms Change:    ${(loan.nextDayOfTermsChange)!}
    Months Bound:           ${(loan.monthsBound)!}
    Provider:               ${loan.providerName}
    Type:                   ${loan.type}
    Response:               ${loan.loanResponse}   <#-- from serialized loan response -->
</#list>
</#if>

<#if accounts.accounts?has_content>
## Accounts
<#list accounts.accounts as account>
### Account ${account_index + 1}
    Account Number:         ${account.accountNumber}
    Name:                   ${account.name}
    Type:                   ${account.type}
    Updated:                ${account.updated}
    Available Credit:       ${account.availableCredit}
    Balance:                ${account.balance}
    Closed:                 ${account.closed}
    Excluded:               ${account.excluded}
    Favored:                ${account.favored}
    Ownership:              ${account.ownership}
</#list>
</#if>

<#if accountHistory.events?has_content>
## Accounts Balance History
<#list accountHistory.events as event>
    ${(event.date)!} - Account: ${event.accountNumber} (${event.name}), Balance: ${event.balance}
</#list>
</#if>

<#if userTransactions.transactions?has_content>
## Transactions:
<#list userTransactions.transactions as transaction>
### Transaction ${transaction_index + 1}
    Date:                   ${transaction.date}
    Original Date:          ${transaction.originalDate}
    <#--Deleted:                ${transaction.deleted} &lt;#&ndash; Note from deletedTransaction repo. False by default &ndash;&gt;-->
    Description:            ${transaction.description}
    Original Description:   ${transaction.originalDescription}
    Amount:                 ${transaction.exactAmount}
    Original Amount:        ${transaction.exactOriginalAmount}
    Merchant:               ${transaction.merchantName}
    Note:                   ${transaction.note}
    Payload:                ${transaction.payload}  <#-- note deserialize and format in a good way -->
    Type:                   ${transaction.type}
    <#if transaction.category?has_content>
    Modified category:      ${transaction.category}
    </#if>
    <#--<#if transaction.parts?has_content>-->
    <#--Transaction parts:-->
    <#--<#list transaction.parts as part>-->
        <#--${transaction.parts} &lt;#&ndash; Todo: Properly format this &ndash;&gt;-->
    <#--</#list>-->
    <#--</#if>-->
</#list>
</#if>

<#if transfers.transfers?has_content>
## Transfer History
<#list transfers.transfers as transfer>
### Transfer Event ${transfer_index + 1}
    Type:                   ${transfer.type}
    Amount:                 ${transfer.exactAmount}
    Currency:               ${transfer.currency}
    Destination:            ${transfer.destination}
    Destination Message:    ${transfer.destinationMessage}
    Source:                 ${transfer.source}
    Source Message:         ${transfer.sourceMessage}
    Remote IP-address:      ${transfer.remoteIpAddress}
    Status:                 ${transfer.status}
    Created:                ${(transfer.created)!}
    Updated:                ${(transfer.updated)!}
</#list>
</#if>

<#if portfolios.portfolios?has_content>
## Portfolios
<#list portfolios.portfolios as portfolio>
### Portfolio ${portfolio_index + 1}
    Type:                   ${portfolio.type}
    Raw Type:               ${portfolio.rawType}
    Total Profit:           ${(portfolio.totalProfit)!}
    Total Value:            ${(portfolio.totalValue)!}
</#list>
</#if>

<#if instruments.instruments?has_content>
## Instruments
<#list instruments.instruments as instrument>
### Instrument ${instrument_index + 1}
    Portfolio Name:             ${(instrument.portfolioName)!}
    Type:                       ${(instrument.type)!}
    Raw Type:                   ${(instrument.rawType)!}
    Average Acquisition price:  ${(instrument.averageAcquisitionPrice)!}
    Currency:                   ${(instrument.currency)!}
    ISIN:                       ${(instrument.isin)!}
    Marketplace:                ${(instrument.marketplace)!}
    Market Value:               ${(instrument.marketValue)!}
    Price:                      ${(instrument.price)!}
    Profit:                     ${(instrument.profit)!}
    Quantity:                   ${(instrument.quantity)!}
    Ticker:                     ${(instrument.ticker)!}
</#list>
</#if>

<#if portfolioHistory.events?has_content>
## Portfolio History
<#list portfolioHistory.events as event>
    ${(event.timestamp)!} -  Portfolio: ${(event.name)!}, Profit: ${(event.totalProfit)!}, Value: ${(event.totalValue)!}
</#list>
</#if>

<#if instrumentHistory.events?has_content>
## Instrument History
<#list instrumentHistory.events as event>
    ${(event.timestamp)!} -  Instrument: ${(event.name)!}, Average Acquisition Price: ${(event.averageAcquisitionPrice)!}, Market Value: ${(event.marketValue)!}, Profit: ${(event.profit)!}, Quantity: ${(event.quantity)!}
</#list>
</#if>

<#if budgets.budgets?has_content>
## Budgets
<#list budgets.budgets as budget>
    ${budget.name}: ${(budget.budgetedAmount)!}
</#list>
</#if>

<#if savingsGoals.savingsGoals?has_content>
## Savings Goals
    <#list savingsGoals.savingsGoals as savingsGoal>
    ${savingsGoal.name}: ${(savingsGoal.targetAmount)!}, target date: ${savingsGoal.targetPeriod}
    </#list>
</#if>
