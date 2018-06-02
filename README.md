# Tink Backend

## Getting started

### Up and running

Below is the easiest method to run the whole Tink backend service locally on
your machine.

Check [trobule shooting list](./TROUBLESHOOTING.md) when you're failed in some steps.

0. Prerequisites: Git, Java 8,
   [Bazel](https://bazel.build/versions/master/docs/install.html), Vagrant
   (alongside VirtualBox or some other virtualization provider).
   [forego](https://github.com/ddollar/forego) is not a requirement but it
   greatly simplifies running all the services together.

1. Clone [tink-infrastructure](https://github.com/tink-ab/tink-infrastructure),
   [tink-backend-encryption](https://github.com/tink-ab/tink-backend-encryption),
   and this repository.

2. Execute `./fetch-dependencies.sh` from `tink-backend` to fetch
   gRPC protobuf definitions ([tink-grpc](https://github.com/tink-ab/tink-grpc) repository).

3. [Optional] If you use Oracle JDK, Install [Oracle Java Cryptography
   Extension](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
   to allow extended encryption key sizes.

4. Run development VM from the infrastructure repository using Vagrant
   (`vagrant up development` from the infrastructure repository).

5. Build the project using Bazel: `bazel build :all`.

6. Seed the databases running inside of the Vagrant VM: `bazel run :system
   seed-database etc/development-system-server.yml`.

7. Run the Tink backend services: `forego start`. If you prefer to do it
   manually you can do it by running commands from `Procfile` in multiple
   shells.

8. `$ curl -i localhost:9090/api/v1/monitoring/healthy` 🎉

### Setup mobile app to use your local Tink service

1. Make sure your phone and workstation is connected to the same network

2. Find the IP-address of your workstation

3. Switch to custom mode in the app by going to the start view (logged out)
    * Android: Not implemented yet. 
    * iOS: Press the "Get Started / Kom igång" button with two fingers for 5 seconds.

4. Input your workstation ip into the custom input dialog

5. Create an account in the app and see the requests in your local server logs

### Beginner checklist

 * [ ] Understand the responsibilities for the different applications Main,
   System, Aggregation, Connector & Encryption.
 * Know about the aliases (Oxford, Leeds etc.) and what they mean. See
   [this](https://docs.google.com/spreadsheets/d/1neWEvQsMjdx9yA0SM-cFpzfNOdcvllvl0OhwcdJoCoc/edit#gid=0)
   document.
 * [ ] Understand how a Spring Data JPA repository works. Check out
   `UserRepository` and understand how it relates `UserRepositoryCustom` and
   `UserRepositoryImpl`. Note that Spring Data JPA automagically implements the
   functions in `UserRepository`.
 * [ ] Check out `main` method and initialization logic in a container. See for
   example `MainServiceContainer`.
 * [ ] Understand how we do REST-based RPC calls between services. Have a look
   at `UpdateService` and its implementation `UpdateServiceResource`. Then
   check out `SystemServiceFactory` and its implementations.
 * [ ] Understand how our we inject configuration from YAML files. See the YAML
   files in the `etc/` directory and `ServiceConfiguration` class.
   * [ ] Have a look at YAML template files we use in production. See for
     example [this file](https://github.com/tink-ab/tink-infrastructure/blob/development/states/tink/system/system-server.yml).
     Notice how we use [Jinja2](http://jinja.pocoo.org/docs/2.9/) logic to
     generate the actual templates. We mostly use
     [Salt pillars](https://docs.saltstack.com/en/latest/topics/tutorials/pillar.html)
     to generate the templates.
 * [ ] Know what [Zookeeper](https://zookeeper.apache.org/) is and what it can be
   used for and what it shouldn't be used for. Find a place in code where we
   take a distributed lock.
 * [ ] Know what [Kafka](https://kafka.apache.org) is and what it can be used for.
 * [ ] Know what [ElasticSearch](https://www.elastic.co/products/elasticsearch) is
   and what it can be used for.
 * [ ] Understand what
   [dependency injection](https://en.wikipedia.org/wiki/Dependency_inversion_principle)
   is, how [Google Guice](https://github.com/google/guice) helps, what a module
   is (see for example `EncryptionServiceModule`) and how we use them.
 * [ ] Understand how we use metrics and labels, what
   [Prometheus](https://prometheus.io) is and how we use
   [Grafana](https://grafana.com). Find a `Counter` and a `Histogram` in code
   and understand what they do. Visit
   [our Prometheus](https://prometheus.global.tink.network) and
   [our Grafana](https://grafana.global.tink.network) and click around!
   * [ ] Read up on [Prometheus metric
     types](https://prometheus.io/docs/concepts/metric_types/), deconstruct the
     query in
     [this Grafana view](https://grafana.global.tink.network/dashboard/db/enterprise-connector-ingest-and-processing-performance?panelId=57&fullscreen&edit)
     In random order: Try to play around a little with the query in
     [our global Prometheus server](https://prometheus.global.tink.network), find in Java
     code where the timers are defined, what buckets they use (and why?) and
     understand how a quantile is estimated. The
     [`histogram_quantile` function documentation](https://prometheus.io/docs/querying/functions/#histogram_quantile())
     might help. Bonus homework: How would you calculate the average latency
     for timers across all machines on a specific `(cluster, environment)`?
 * [ ] Have a look at the overall structure of an Aggregation agent. See
   `ICABankenAgent` for a basic example.
 * [ ] Learn the basic basic data model of Cassandra. Read
   [this article](https://www.datastax.com/dev/blog/basic-rules-of-cassandra-data-modeling).
 * [ ] Learn about [Dropwizard](http://www.dropwizard.io)
   [command](http://www.dropwizard.io/0.9.2/docs/manual/core.html#commands)s.
   See
   [`DemoUserTraversalCommand`](https://github.com/tink-ab/tink-backend/blob/development/src/system-lib/src/main/java/se/tink/backend/system/cli/seeding/DemoUserTraversalCommand.java)
   for a basic example of a command traversing all our users concurrently.
   Don't forget that all commands must be registered in
   [`SystemServiceContainer`](https://github.com/tink-ab/tink-backend/blob/development/src/system-service/src/main/java/se/tink/backend/system/SystemServiceContainer.java).

## Coding guidelines

Read [CONTRIBUTING.md](CONTRIBUTING.md)

## Working with Bazel

Build is defined in `BUILD` files (see `find . -name BUILD`). Versions of
third-party dependencies are declared in `WORKSPACE`. Project Bazel
configuration is in `.bazelrc`. IntelliJ IDEA project configuration is in
`.bazelproject`.

### Build the JARs

Everything\*: `bazel build :all`.

\* _Excluding `:integration-test` and `:system-test` as those are
[marked](https://github.com/tink-ab/tink-backend/blob/931b8dfab91ad6e6696ef474b97ba500fe983fd0/BUILD#L1138)
as
[`manual`](https://docs.bazel.build/versions/master/be/common-definitions.html#common.tags)._

A specific target: `bazel build :common-utilities`.

deb packages: `bazel build deb:all`.

### Running tests

Unit tests: `bazel test :all`

Integration tests: `bazel test :integration-test`

System tests: `bazel test :system-test`

One test class: `bazel test --test_filter=se.tink.backend.core.AmountTest
:main-api-test`

A single test: `bazel test
--test_filter=se.tink.backend.core.AmountTest#testEquality :main-api-test`

In debug mode: `bazel test --java_debug -- :main-lib-test`. After startup the
JVM will wail for a remote debugger connection before proceeding.

If you want to compare test durations, `tools/test-times.awk` might help.

To adjust logs during test do one of the following:
1. add option `test_output` on command line
1. change `test_output` in `.bazelproject`
1. change log level in `tools/bzl/junit.bzl

_NB_ see: [test_output](https://docs.bazel.build/versions/master/command-line-reference.html#build-options)

### Running binaries

Running a service: `bazel run :main server etc/development-main-server.yml`

Running a command: `bazel run --jvmopt=-DdryRun=false -- :system clean-database
etc/development-system-cli.yml`

In debug mode: `bazel run -- :main --wrapper_script_flag=--debug server
etc/development-main-server.yml`. After startup the JVM will wail for a remote
debugger connection before proceeding.

To run whole Tink backend in ABN AMRO configuration run `forego start -f
Procfile.abn-amro`.

### Configuration

If you experience problems with excessive resource usage by Bazel you can use
add one of the following to `/etc/bazel.bazelrc`:

`build --ram_utilization_factor=5` (roughly 5% of the RAM)

or

`build --local_resources 2048,2.0,0.5` (2048 MB of RAM, 2 cores, 0.5 of I/O)

See `bazel help build` for more details. For build sandboxing to work properly
on Linux you'll need a kernel compiled with `CONFIG_USER_NS=y`.

## Developing in IntelliJ IDEA

### Import Bazel project

- Install two Bazel plugins to IntelliJ (In IntelliJ go to Preferences ->
  Plugins -> Browse Repositories, search for Bazel)
  * IntellJ with Bazel
  * Bazel Build Formatter

- Close the open project if you have one opened

- Click "Import Bazel Project"

- Workspace: Select the directory where you checked out `tink-backend`

- Enter location of the Bazel binary (`/usr/local/bin/bazel` for Homebrew)

- Select "Import from workspace" and select the `.bazelproject` file in the
  tink-backend directory

### Development

- There're preconfigured Run configurations for tests and different services
  available

- If you want to work on a service (say Main) from IDEA without having to start
  the whole backend manually:

  * start rest of the services from command line using `forego start -c 'all=1,main=0'`
  * run/restart/debug Main service from the IDE as usually

- After changes in a `BUILD` file you'll need to reimport it using "Sync
  project with BUILD files button"

- Sometimes project may fall out of sync. You can right-click the root project
  directory and select "Partially Sync" option.

- You can build/run/test (almost) any Bazel target from IntelliJ. To do that
  got to Run/Debug dropdown -> Edit Configurations... and create a new Bazel
  Command

## Useful git stuff

### List all branches that has been merged
Go to `development` and issue:

```
git branch --merged | grep -v master | grep -v development
```

If you feel **comfortable removing those branches** you can issue (_NOTE:_ go through the list first):

```
git branch --merged | grep -v master | grep -v development | sed 's/origin\///' | xargs -n 1 git branch -d
```

The same can be done but looking at origin:

```
git branch -r --merged | grep -v master | grep -v development | sed 's/origin\///'
```

and **delete those**, you delete the local branches--if it exists, (_NOTE:_ go through the list first):
```
git branch -r --merged | grep -v master | grep -v development | sed 's/origin\///' | xargs -n 1 git branch -d
```

## Certificates for APN

 * production.p12 - se.tink.frontend.mobile.apple Production/Sandbox APN
 * enterprise.p12 - se.tink.iphone Production/Sandbox APN

## How to create a new container

 1. Double check with at least one peer it seems reasonable :-)
 2. Have a look at the checklist in https://github.com/tink-ab/tink-backend/pull/2007.
 3. Make a pull request for review.

## Useful Kafka stuff

Create topic with 10 partitions

```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --partitions 10 --replication-factor 1 --topic UPDATE_TRANSACTIONS
```

List consumer groups

```
bin/kafka-consumer-groups.sh --new-consumer --bootstrap-server localhost:9092 --list
```

Show lag and connected consumers
```
bin/kafka-consumer-groups.sh --new-consumer --bootstrap-server localhost:9092 --describe --group default
```

Delete a topic
```
bin/kafka-topics.sh --zookeeper localhost:2181 --delete --topic UPDATE_TRANSACTIONS
```

## Development with Minikube (Experimental)

Start the development environment by running `setup-minikube.sh` from `tink-infrastructure`.

[kubernetes-generator](https://github.com/tink-ab/tink-infrastructure/tree/development/kubernetes-generator) is used to generate Kubernetes configs from templates.

### Running applications

```bash
# Connect Docker to the docker server running inside Minikube
eval $(minikube docker-env);

# Build docker images with Bazel 
bazel build docker:bundle.tar && docker load -i bazel-bin/docker/bundle.tar;

# Render and apply a Chart
kubernetes-generator --chart insights --cluster local --environment development | kubectl apply -f -;
```
