---------------------------------------
## Changes this Pull Request Introduces
---------------------------------------
<!---Describe the big picture of your changes here to communicate to the reviewers why they should accept this pull request. If it fixes a bug or resolves a feature request, be sure to link to the trello card (if available).--->


---------------------------------------
## Reviewer's Checklist
---------------------------------------
This is a guideline for the reviewer on what to check to ensure that the quality of the code is kept at a Tink standard.  

_Put an `x` in the boxes that have been checked and fullfiled_ 

- [ ] The code packages follow the [Tink structure](https://github.com/tink-ab/tink-backend-aggregation/wiki/Development-and-Code#package-structure)
- [ ] The code is clean from `development-only` code (`client.setDebugOutput`, `println`, `client.setProxy()`) 
- [ ] The known errors are being handled and unknown logged:
	- [ ] The Authenticator throws an AuthenticationException (e.g the password is incorrect)
	- [ ] The Authenticator throws an AuthorizationException if the user attempts to access a resource it does not have privileges to (e.g. if the account has been blocked).
	 You may want to throw this exception if the http status code is 401/403
- [ ] The account fetcher sets the appropriate account type, logs unknown types and does not fall back to known values
- [ ] The `JSON/XML` values are sanitized (Not hardcoded from string concatenation of input values; Use `Jackson`!) 
- [ ] The code uses a [`Constants Class`](https://github.com/tink-ab/tink-backend-aggregation/wiki/Code-Structure#constants) for constants 
- [ ] The code uses insensitive comparison of constants (`equalsIgnoreCase`)
- [ ] The code uses `Catalog` for translatable strings
- [ ] The code follows the naming conventions of [Tink](https://github.com/tink-ab/tink-backend-aggregation/wiki/Development-and-Code)
- [ ] The identifiers are understandable (e.g. nationalId instead of ssn/nId or any other nondesciptive/appropriate name)
- [ ] The `Amounts` have the correct sign (positive/negative) 
- [ ] The code follows the coding style and format of [Tink](https://docs.google.com/document/d/1GirwFcub-0q2RK1zXLzKJt_dUTXEkhpPWJGKozPVias/edit#) (space after if-statment, newlines at EOF, etc)
- [ ] The code does not return `null` from methods. `Optional<>` is used in case of potential absent resource needed as return value
- [ ] The commits and the code changes are correctly labeled based on the [contributing guidelines](https://github.com/tink-ab/tink-backend-aggregation/blob/master/CONTRIBUTING.md)
- [ ] The code does not use static imports in production code
- [ ] The code uses `equals` to compare objects instead of `==` (to avoid cofusion and possible bugs)

---------------------------------------
## Further comments
---------------------------------------
<!---If this is a relatively large or complex change, kick off the discussion by explaining why you chose the solution you did and what alternatives you considered, etc...--->
