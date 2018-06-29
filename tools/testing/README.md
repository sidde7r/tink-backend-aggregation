0. From your local `tink-infrastructure` run `./setup-minikube.sh --create-tink-backend-databases`
1. Start up the aggregation service
2. Run `./aggregation_db_seed/local_aggregation_db_seeder.sh`
3. Make sure you have all necessary packages install by doing `pip install -r requirements.txt`.
	- Preferably use virtualenv
4. Start up the web server `python webServer.py` (`python webServer.py -h`)
5. Start up the test script `python testAggregation.py` (`python testAggregation.py -h`)