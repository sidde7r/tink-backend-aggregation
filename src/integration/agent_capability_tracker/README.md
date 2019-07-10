#Getting started

##GRPC and GRPC_CLI

- Install: `brew install grpc`.

##Database

###Start Database

- Run `cd db && docker-compose up`.

###Other Database

- Recommended tool to browse DB is [DBeaver](https://dbeaver.io/).
- Can install it with `brew cask install dbeaver-community`.


##Start Server Locally

- In `tink-backend-aggregation` run: `bazel run :agent-capability-tracker etc/integration/development-configuration.yaml`