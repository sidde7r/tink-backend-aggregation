## Trouble shooting

### Get Tink backend up and running (on Mac)

0. /bin/bash: msgfmt: command not found
Install gettext package
```
brew install gettext
brew link --force gettext
```

1. Failed to seed database with tables can not be created

This is a [bug](https://github.com/saltstack/salt/pull/46919) from newer SaltStack version, which has been fixed but not released.
To get it solved you need to ssh to Vargrant env `vagrant ssh development` and run the following commands there.
Note that answer N when asked about the configuration change, otherwise answer Y to all questions.
```
wget -O - https://repo.saltstack.com/apt/debian/9/amd64/2017.7/SALTSTACK-GPG-KEY.pub | sudo apt-key add -
echo "deb http://repo.saltstack.com/apt/ubuntu/16.04/amd64/2017.7 xenial main" | sudo tee /etc/apt/sources.list.d/saltstack.list
sudo apt-get update
sudo apt-get remove salt-minion
sudo apt-get install salt-minion=2017.7.5+ds-1 salt-common=2017.7.5+ds-1
sudo salt-call --local state.highstate
```

See more details [here](https://tink.slack.com/archives/C82D6P0LR/p1524151774000134)

2. `forego start` failed with encryption service is not available 

Start encryption only (this might take some time) and then switch back to start all (or necessary services)
```
forego start -c main=0,system=0,connector=0,aggregation=0,encryption=1
```