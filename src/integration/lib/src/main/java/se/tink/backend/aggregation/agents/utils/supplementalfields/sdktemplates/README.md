# Sdk Templates

## Background
For Tink Link we are extending the supplemental info definitions - so they now how to render 2FA screen. 
This behaviour must be hidden from the customers using DI.

This library templates is backward compatible with customers using DI - please be aware that it will just work with Tink Link.

To trigger that templates on credentials level - you need to ```withTemplatesScreen = true``` to Credentials Service.

Implementation of supplemental information filtering can be found in: ```se.tink.credentials.service.templatesscreen.TemplatesScreenHandler```

Treat this is as temporary solution - new service will come in the future.

## Templates
Currently, you can find templates for 
 - app code, 
 - card reader, 
 - decoupled flow (with method switch), 
 - id completion - please note that it will work only in Tink Link!
 - sms code
 
**Fields will be sent to Direct Integration Customers only if Type and Style is backward compatible**
 
## How to use it?
### App code
Call ```AppCodeTemplate.getTemplate()``` method with ```AppCodeData``` object. 
Within the input field you shouldn't provide InGroup.

### Card reader
Call ```CardReaderTemplate.getTemplate()``` method with ```CardReaderData``` object. 
Within the input field you shouldn't provide InGroup.

As a help text you can provide list of strings which will be rendered as list.

### Decoupled flow
You have 2 options for decoupled flow. One with change method and one without.
When you decide to change the method - in supplemental info you will receive object {"CHANGE_METHOD": true}

Call ```DecoupledTemplate.getTemplate()``` method with ```DecoupledData``` object
or Call ```DecoupledTemplateWithChangeMethod.getTemplate()``` method with ```DecoupledWithChangeMethodData``` object. 

### Id Completion
**Please note that it will work only in Tink Link! There is no backward compatibility**
It represents the situation when user has the image with identity, password and one of a few identifications to choose.
This works for e.g. Millenium Bank in Poland. User has the image to check, password to fill and
need to choose between SSN, Id Number or Passport Number. All that data is provided within Supplemental Information.

Call ```IdCompletionTemplate.getTemplate()``` method with ```IdCompletionData``` object. 
Within the input field you should provide InGroup - so the identification fields are rendered within one group.

### Sms code
Call ```SmsCodeTemplate.getTemplate()``` method with ```SmsCodeData``` object. 
Within the input field you shouldn't provide InGroup.

As a help text you can provide list of strings which will be rendered as list.

### Backward compatibility
This will be used only for Tink Link. 
In tink-backend there is a handler (```TemplatesScreenHandler.java```) that will make this templates backward compatible with our existing clients. 

Tink Link will send additional hidden param (withTemplatesScreen) to ```credentialsService```, so these templates will be rendered.

## Other docs
Technical Approach: https://docs.google.com/document/d/1lHn4laEiFdCF-77K0ftTgSyl7VpRS3bgaru-UwsDTzA/edit#