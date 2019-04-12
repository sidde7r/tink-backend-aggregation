# tink-backend-aggregation

## config maps

TODO

## deployment

TODO

## ingresses

TODO

## secrets

TODO


## provisioning-cronjob.yaml

The provisioning-cronjob.yaml introduces the kubernetes job configuration that we use in order for the AddClientConfigurationsCommand to provision successfully new aggregators in the Aggregation Cluster.

The reasoning of adding this cronjob in the existing aggregation namespace, is to avoid having duplicate config maps between namespaces.

The AddClientConfigurationsCommand is a dropwizard CLI that depends on valid yaml configuration. By adding the cronjob on the same namespace we are able to avoid issues around configuration evolution, but we are bound to use the same rules on applying the charts in a given cluster.

By that we mean that even if we might not want to provision clients in the aggregation-staging cluster, since the `./charts/tink-backend-aggreagtion` apply per cluster, we cannot avoid it.

### How does the AddClientsConfigurationsCommand work

The command loads the AggregationServiceConfiguration yaml from the configmaps. In the configmaps we expect to see the `ProvisionClientsConfig` which is a structured map, with key as the `clientName` and the values include the `aggregatorIdentifier` (for now), eg:

```
clients:
    company-inc-testing:
        aggregatorIdentifier: "Company Inc AB"

 ```

Every day the cronjob runs and checks if we have any missing client-configurations from the database. If any is missing the command creates a client for them based on the yaml configuration. The cluster that the client is connected to is `oxford-production` and it's hardcoded in the AddClientsConfigurationCommand flow.

### How to add Clients

- Based on the request from the TAMs, add the client configuration in the [values](.charts/tink-backend-aggregation/values/aggregation-production.yaml).
- Deploy aggregation production.
- Command will provision the client the next time it runs.
- Next steps should be similar to before (map the apiKey to the aggregation-controller secrets).






