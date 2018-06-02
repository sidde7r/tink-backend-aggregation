consent-lib
==================

"All" things related to handling consents.  

ModelMapper with `MatchingStrategies.STRICT` is used to map entities. Unit tests will 
assert that all fields are mapped. 


To improve
==================

`ConsentService` and `ConsentServiceJerseyTransport` 
are still defined in `main-api` since they depend on `se.tink.backend.core.User` 
for authentication. 

`se.tink.backend.common.config.DistributedRepositoryConfiguration`in `common-lib` 
is updated to scan for cassandra repos at in `se.tink.backend.consent.repository.cassandra`.


