# Kibana AWS Log Downloader

This is an experimental project implemented to provide an easy way to download AWS debug logs by using Kibana on Chrome browser.

## Components

The project consists of two main components: **Chrome extension** and **Python server**. Chrome extension can be divided into two components, namely **content script** and **background script**. Below the responsibilities of each component are explained:

**Content script**: This script is automatically injected into Kibana web page ("https://kibana.aggregation-production.tink.network/app/kibana/..." or "https://kibana.aggregation-staging.tink.network/app/kibana/..."). For each tab, this script has its own scope.
- It handles the creation of extra DOM elements inside the website (*"Download Logs" button* and *messaging popup*).
- From the background script, it receives the cookie and request payload of the POST request that Kibana webpage is doing to ElasticSearch and stores them. When user clicks to "Download Logs" button, it sends them to background script and initiates the download process.

**Background script**: This script is responsible from handling the communication between content script and Python server. It is also responsible from intercepting POST request that are done from Kibana webpage to ElasticSearch and extracting request payload and cookies. For all Chrome processes there is one background script so its scope is shared.
- It intercepts requests to ElasticSearch, extracts cookie and request payload and sends them to the content script of the webpage that triggered the request
- It relays messages between content script and Python server

**Python server**: This server communicates with background script and does the necessary queries and executes the necessary commands to download the logs. Once it gets the cookie and request payload from background script it does the following:
- It does the same query to ElasticSearch, gets the result and finds all unique requestIDs. 
- For each requestID, it makes a query to ElasticSearch where this requestID is used as a filter and it tries to find a link for AWS debug log
- For each AWS debug log link it could find, it executes necessary commands to download them.
- After it downloads the logs to a predetermined folder, it organizes the folder in a way that will be explained later.

## Initialization

Both Chrome extension and Python script must be running to be able to use it.

### Initializing Chrome Extension

- Open the Chrome browser
- Click to three dots icon on top-right and go to More Tools - >Extensions
- You need to enable "Developer mode"
- Click to "Load unpacked"
- Pick the folder "<path_to_tink_backend_aggregation>/aws_log_fetcher/chrome_extension"

That's it! When there is a code update, you need to remove the extension and perform the same steps (TODO: Find a better way to handle extension script updates...)

To test it, open Kibana and click Discover. On top right, just next to the "Refresh" button, you should see "Download Logs" button. This button is created automatically by extension so if you can see this button, it means that the extension is loaded correctly.

### Running Chrome Extension

There is no running step. Once it is initialized it will run automatically when browser is running.

### Initializing Python Server

Python server uses Python 3.x. Running the server in virtualenv is recommended.

- Open Terminal and go to "<path_to_tink_backend_aggregation>/aws_log_fetcher/python_server" folder.
- Initialize dependencies by running `pip3 install -r requirements.txt`

### Running Python Server

- Open Terminal and go to "<path_to_tink_backend_aggregation>/aws_log_fetcher/python_server" folder.
- Run the following:

```
python3 server.py -o <download_folder> -u <your_Tink_mail_address> -i <GOOGLE_IDP_ID> -s <GOOGLE_SP_ID>
```

To find GOOGLE_IDP_ID and GOOGLE_SP_ID, run `cat /Users/<username>/.aws/config`. You will see the values for that there.

## Usage

- Python server must be already running before you run Kibana in your browser. Otherwise Python server and the Chrome extension will not be able to connect
to each other.
- Open Kibana website and make any query.
- Then click to "Download Logs" button. A popup window will appear where you will see some messages sent by Python server.
- If it is the first time you are trying to download logs, it means you are not authenticated to AWS. In this case you will see a message saying the following:
```
You will be asked for credentials, please check the Terminal where the Python server is running and provide the necessary input
```
Do what it says, once you are authenticated the login information will be cahced for 1 hour so you will not need to perform this step again for that period.
- At the end, it will message "Logs are downloaded". You can see the logs in the folder that you gave as an argument (-o) when running the Python server.
- The logs are organized in the following way:
  - In the output folder, for each time user clicks "Download Logs" button a folder with a timestamp is created. This is done this way to avoid mixing up logs that are downloaded at different requests.
  - In each folder with timestamp, for each different provider, there will be a folder with the name of the provider.
  - In each provider folder, there will be enumerated (0,1,2,...) folders. The logs that are in the same folder end in a call to the same endpoint.
  - In each enumerated folder, there will be "<requestId>.log".

## What to do when it stops working

This is a guide for how to fix this extension when it starts not working.

- First of all, check which version of aws_google_auth module this extension is using in this project and which version tink_infrastructure is using. 

    - To check which version this extension is using, simply go to `python_server/aws_google_auth/_version.py`
    - To check which version tink_infrastructure is using, go to https://console.cloud.google.com/gcr/images/tink-containers/GLOBAL/aws-google-auth and check the tag of the latest image

- If the versions are different, then probably this is the reason for the failure. To fix the issue, you need to do the followings:

- In a Terminal (let's call it Terminal 1) execute the following command

``` 
docker run -it gcr.io/tink-containers/aws-google-auth:latest
```

- In another Terminal (Terminal 2) execute the following command:

``` 
docker container ls
```

There you need to copy the containerId for the image `gcr.io/tink-containers/aws-google-auth:latest`

- Now in Terminal 2 execute

``` 
docker cp <containerId>:/aws-google-auth/aws_google_auth <path_to_tink_backend_aggregation_repo_root>/aws_log_fetcher/python_server
```

After executing this command you can close Terminal 2 and in Terminal 1, after executing `exit` you can close Terminal 1 as well. Then do the following:

- Go to `<path_to_tink_backend_aggregation_repo_root>/aws_log_fetcher/python_server` and remove the `tests` folder it is redundant.

- There is a commit with name `feat(AWSLogFetcher): Patch for aws_google_auth v0.0.36`. Find this commit, check the changes and apply the same patch in the necessary Python files in `python_server/aws_google_auth` folder. Note that since the version of aws_google_auth will be different, it is possible that there will be no one-to-one match so you might need to be careful and adapt to the curernt version when applying the patch.

- Now try to run the Python server, you might fail due to missing dependencies. If this is the case install the missing dependencies by using `pip install <...>`

- When you can run the Python server, go to `python_server` folder in Terminal and execute 

```
pip freeze > requirements.txt
```

- Make a commit where you include all changes in `python_server` folder.

That's all, have fun using the extension :)

