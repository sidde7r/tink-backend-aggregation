# Tink Backend Aggregation

## Integration Squad

## Getting started

Read through these documents:

[Environment Setup](https://docs.google.com/document/d/1GirwFcub-0q2RK1zXLzKJt_dUTXEkhpPWJGKozPVias/)

[Integration Playbook](https://docs.google.com/document/d/18pSzbRPlHYbKJtCDntMYE_4TqNWFdyTFuETq6lyNZBk)

[Entities](https://docs.google.com/document/d/1jZj9p-jgafdX3iFZzNhEQynpoNi_pyR16Pwnq4UOd9c)

[Good To Know Commands](https://docs.google.com/document/d/1tfVv733hbOBUxDByGhIpdPphMARVjaoxcsK4xnM3MOM)

More documents can be found in the[Onboarding Folder](https://drive.google.com/drive/folders/1vuuznSI7I7FJpXeGwy3V_wNS_aZoq-yH)in Google Drive 

### Up and running

Check [trouble shooting list](./TROUBLESHOOTING.md) when you're failed in some steps.

0. Prerequisites: Git, Java 8,
   [Bazel](https://bazel.build/versions/master/docs/install.html), Vagrant
   (alongside VirtualBox or some other virtualization provider).
   [forego](https://github.com/ddollar/forego) is not a requirement but it
   greatly simplifies running all the services together.

1. Clone [tink-infrastructure](https://github.com/tink-ab/tink-infrastructure),
   [tink-backend-encryption](https://github.com/tink-ab/tink-backend-encryption),
   [tink-backend](https://github.com/tink-ab/tink-backend)
   and this repository.

### Setup mobile app to use your local Tink service

1. Make sure your phone and workstation is connected to the same network

2. Find the IP-address of your workstation

3. Switch to custom mode in the app by going to the start view (logged out)
    * Android: Not implemented yet.
    * iOS: Press the "Get Started / Kom igång" button with two fingers for 5 seconds.

4. Input your workstation ip into the custom input dialog

5. Create an account in the app and see the requests in your local server logs

## Coding guidelines

Read [CONTRIBUTING.md](CONTRIBUTING.md)

## Working with Bazel

Build is defined in `BUILD` files (see `find . -name BUILD`). Versions of
third-party dependencies are declared in `WORKSPACE`. Project Bazel
configuration is in `.bazelrc`. IntelliJ IDEA project configuration is in
`.bazelproject`.

### Build the JARs

Everything\*: `bazel build :all`.

A specific target: `bazel build :common-utilities`.

deb packages: `bazel build deb:all`.

Integration Framework: `bazel build :integration_framework`

### Running tests

Unit tests: `bazel test :all`

To adjust logs during test do one of the following:
1. add option `test_output` on command line
1. change `test_output` in `.bazelproject`
1. change log level in `tools/bzl/junit.bzl

_NB_ see: [test_output](https://docs.bazel.build/versions/master/command-line-reference.html#build-options)

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

## Certificates for APN

 * production.p12 - se.tink.frontend.mobile.apple Production/Sandbox APN
 * enterprise.p12 - se.tink.iphone Production/Sandbox APN

