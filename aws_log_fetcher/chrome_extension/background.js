
/*
	We want to catch requests only to the following URLs. We want to catch those request
	to get the cookie and query JSON out of them
*/
let filter = {
	urls: [
		"https://kibana.aggregation-production.tink.network/elasticsearch/logstash*/_search*",
		"https://kibana.aggregation-staging.tink.network/elasticsearch/logstash*/_search*",
	]
};

/*
	WebSocket to be used for communicating with the Python script
	Note that for all Kibana webpages, only one background script is running
	So this script (so WebSocket object) might be shared between multiple tabs
*/
var ws = null;

/* 
	Listens for the messages coming from the content_script (AKA web page)
	This is triggered when user clicks to the download button that the extension
	automatically creates on the webpage
*/
chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {

	/*
		Payload is just any data that will be sent back by the Python server when it sends a
		message to the background script. Here I prefer to send tabID so when this background script
		will receive a message from the Python server, thanks to the payload that will be sent along
		with the message, it will know to which browser tab it should relay the message

		host is either https://kibana.aggregation-production.tink.network or https://kibana.aggregation-staging.tink.network
		we need to send it to the Python script so that it can know from which environment it should try to download the 
		corresponding AWS logs.
	*/
	let data = {
		payload: sender["tab"]["id"],
		cookie: request.cookie,
		query: request.query,
		path: request.path,
		host: sender.url.substr(0, sender.url.indexOf("/app"))
	}

	/*
		If we have not initialized a WebSocket connection to the Python server before, just do that and then
		send the data. Otherwise, directly send it
	*/
	if (ws == null) {
		ws = new WebSocket("ws://127.0.0.1:8765/");
		ws.onopen = function(event) {
			ws.send(JSON.stringify(data));
		};

		// This will be triggered when the Python server will send a message to this background script
		ws.onmessage = function(event) {

			let data = JSON.parse(event["data"]);

			// Just relay the message that has been received from the Python server to the corresponding tab
			chrome.tabs.sendMessage(data["payload"], {
				"type": "MESSAGE",
				"data": data["message"]
			}, function(response) {});
		};

		ws.onclose = function(event) {
        	ws = null;
        };
	}
	else {
		ws.send(JSON.stringify(data));
	}

	chrome.tabs.sendMessage(sender["tab"]["id"], {
		"type": "MESSAGE",
		"data": "The request is sent to Python server"
	}, function(response) {});
});

/*
	This event will be triggered when the browser makes a request to one of the URLs stated in
	"filter" variable. When this is triggered, we will be able to fetch the headers used in the
	request to ElasticSearch and will just send the cookie to the content_script that runs in the tab
	that initiated this request
*/
chrome.webRequest.onSendHeaders.addListener(function(details) {

	let tabID = details["tabId"];

	let cookie = details.requestHeaders.filter(function(e) {
		return e["name"].localeCompare("Cookie") == 0;
	})[0]["value"];

	let data = {
		"type": "COOKIE",
		"cookie": cookie
	};

	chrome.tabs.sendMessage(tabID, data, function(response) {});
},
filter, 
["requestHeaders", "extraHeaders"]);

/*
	This event will be triggered when the browser makes a request to one of the URLs stated in
	"filter" variable. When this is triggered, we will be able to fetch the request body used in the
	request to ElasticSearch and will just send the query JSON to the content_script that runs in the tab
	that initiated this request
*/
chrome.webRequest.onBeforeRequest.addListener(function(details) {

	let payload = new TextDecoder("utf-8").decode(new Uint8Array(details.requestBody.raw[0].bytes));
	let requestJson = JSON.parse(payload);
	let queryJson = JSON.stringify(requestJson["query"]);

	let data = {
		"type": "QUERY",
		"query": queryJson
	};

	chrome.tabs.sendMessage(details["tabId"], data, function(response) {});
},
filter, 
["blocking", "requestBody"]);
