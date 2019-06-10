0. From your local `tink-infrastructure` run `./setup-minikube.sh --create-tink-backend-databases`
1. Start up the aggregation service `tink-backend-aggregation$ bazel run :aggregation --jvmopt=-Djava.net.preferIPv4Stack=true server etc/development-minikube-aggregation-server.yml`
2. Make sure you have all necessary packages installed by doing:
	a) `$ pip install virtualenv` # Only do this if you not already have virtualenv installed
	b) `$ cd tink-backend-aggregation/tools/testing/`
	c) `$ virtualenv my_project` # Only do this if you not already have a virtualenv project for the testing
	d) `$ source my_project/bin/activate`
	e) `$ pip install -r requirements.txt`
3. Start up the web server `python webServer.py` (`python webServer.py -h`)
4. Start up the test script `python testAggregation.py` (`python testAggregation.py -h`)