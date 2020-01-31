/*
	The cookie that will be used in the request to ElasticSearch, in the beginning it is null,
	When the browser will make a request to ElasticSearch, the extension will automatically catch
	the request and fetch the cookie out of it
*/
let cookie = null;

/*
	The query JSON in the request to ElasticSearch, (it goes the same way with cookie)
*/
let query = null;

/*
	Reference to download button (DOM element) that the extension creates in Kibana page.
	We keep the reference to be able to check if it is created already so we will not create
	a button multiple times
*/
let downloadButton = null;
let messagePopup = null;
let messageArea = null;
let popupCloseButton = null;

function addMessage(message) {
	messageArea.val(messageArea.val() + "\n" + message);
}

function checkIfButtonShouldBeVisible() {

	/* If we added download button to the webpage already, do not add it again*/	
	if (downloadButton != null)
		return;

	/* If we already know the query JSON and cookie to be used we can create the download button
	as we know everything that we need to fetch the logs*/
	if (query != null && cookie != null) {

		// DOM element that we will append the download button to
		let queryBarDOM = $(".kbnQueryBar").eq(0);

		/* Adding a download button with style :) Not important */
		downloadButton = $("<div/>", {
			class: "euiFlexItem euiFlexItem--flexGrowZero"
		});
		let dom_el1 = $("<span/>", {class: "euiToolTipAnchor"});
		let dom_el2 = $("<button/>", {class: "euiButton euiButton--primary euiButton--fill", type: "button"})
		let dom_el3 = $("<span/>", {class: "euiButton__content"});
		dom_el3.html("Download logs");
		downloadButton.append(dom_el1);
		dom_el1.append(dom_el2);
		dom_el2.append(dom_el3);
		queryBarDOM.append(downloadButton);
		/* Download button is added */

		/*
			When user clicks to the button we will trigger the background script which
			will talk with the Python server.

			TODO: Path can be added when we implement the functionality to ask the 
			user for an output folder to save the logs
		*/
		downloadButton.click(function() {
			messagePopup.css("visibility", "visible");
			messageArea.val("");

			chrome.runtime.sendMessage({
				cookie: cookie,
				query: query,
				path: null
			}, function(response) {
				if (response)
					addMessage(response["data"]);
			});
		});

		// Will create a hidden popup to show server message to user

		messagePopup = $("<div/>", {id: "extension_message_popup"});
		messagePopup.css(extension_message_popup_style);
		messageArea = $("<textarea/>", {id: "extension_message_textarea"});
		messageArea.css(extension_message_textarea_style);

		popupCloseButton = $("<input/>", {type: "button", value: "Close"});
		popupCloseButton.click(function() {
			messagePopup.css("visibility", "hidden");
		});

		messagePopup.append(messageArea);
		messagePopup.append(popupCloseButton);

		$("body").append(messagePopup);
	}
}

/*
	The code below is a listener that gets the messages sent by the background script
	(background script is the script that manages the communication between this script and
	the Python server)
*/

chrome.runtime.onMessage.addListener(
	function(message, sender, sendResponse) {
		/*
			If the background script catched the call to ElasticSearch and fetched the cookie,
			it sends the cookie to this script and we need to store that.
		*/
		if (message.type === "COOKIE") {
			cookie = message["cookie"];
			checkIfButtonShouldBeVisible();
		}
		/*
			If the background script catched the call to ElasticSearch and fetched the query JSON,
			it sends the query JSON to this script and we need to store that.
		*/
		else if (message.type === "QUERY") {
			query = message["query"];
			checkIfButtonShouldBeVisible();
		}
		/*
			If the background script received a message from the Python server, it relays the message
			to this script.
		*/
		else if (message.type === "MESSAGE") {
			addMessage(message["data"]);
		}
		/*
			We should never come here...
		*/
		else {
			throw "Unknown request type : " + JSON.stringify(message);
		}
});