from ElasticSearchRequestHandler import *
from AWSManager import *
import os
import argparse
import sys
from constants import find_aws_log_link_query
import websockets
import asyncio
import aws_google_auth
from datetime import timedelta
from LogOrganiser import organise
from datetime import datetime


def get_timestamp():
    now = datetime.now()
    return str(now).replace(" ", "T")


async def send_message(ws: websockets.WebSocketServerProtocol, message: str, payload: str):
    if ws is not None:
        mes = {
            "message": message,
            "payload": payload
        }
        await ws.send(json.dumps(mes))
    else:
        print(message)


async def run(cookie,
              query,
              output_folder,
              host,
              idp_id,
              sp_id,
              username,
              payload="",
              authenticate_for_aws_command=None,
              ws: websockets.WebSocketServerProtocol = None):
    print("Download request has been received")
    output_folder = os.path.join(output_folder, get_timestamp())

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
        await send_message(ws, "Output folder is created " + output_folder, payload)

    # We will first make the exact same query that the user made in the browser and then
    # detect unique requestIDs there
    elasticsearch_manager = ElasticSearchRequestHandler(cookie, host)

    # First determine the set of requestIDs for the result of the query
    result = elasticsearch_manager.make_query(query)
    unique_keys = result.get_unique_keys()
    await send_message(ws, "Fetched unique requestID+credentialsID+providerName pairs. There are " + str(
        len(unique_keys)) + " unique pair", payload)

    # Get the timestamps for the lower and upper limit of time range used in the query
    gte = json.loads(query)["bool"]["must"][-1]["range"]["@timestamp"]["gte"]
    lte = json.loads(query)["bool"]["must"][-1]["range"]["@timestamp"]["lte"]

    gte_date = datetime.strptime(gte, "%Y-%m-%dT%H:%M:%S.%fZ") - timedelta(minutes=10)
    lte_date = datetime.strptime(lte, "%Y-%m-%dT%H:%M:%S.%fZ") + timedelta(minutes=10)

    gte_date = gte_date.strftime("%Y-%m-%dT%H:%M:%S") + ".000Z"
    lte_date = lte_date.strftime("%Y-%m-%dT%H:%M:%S") + ".999Z"

    metadata = []
    download_requests = []

    for index, key in enumerate(unique_keys):

        request_id = key.split("_")[0]
        credentials_id = key.split("_")[1]
        provider_name = key.split("_")[2]

        # For each <requestID,providerName> pair, make a query to ElasticSearch to fetch all logs belonging to this
        # pair. Note that we are using a bigger time range to ensure that we will fetch all logs
        # (the time range for the query that the user made might not coincide completely with the time range
        # when the full traffic for this requestID+providerName occurred)
        logs_for_session = elasticsearch_manager.make_query(query=json.dumps(find_aws_log_link_query),
                                                            replacements=[
                                                                ("<requestId>", request_id),
                                                                ("<credentialsId>", credentials_id),
                                                                ("<providerName>", provider_name),
                                                                ("<gte>", gte_date),
                                                                ("<lte>", lte_date)
                                                            ])

        await send_message(ws, "Fetched log for the flow with requestID = " + request_id
                           + ", credentialsID = " + credentials_id + ", provider name = " + provider_name +
                           " (flow " + str(index + 1) + "/" + str(len(unique_keys)) + ")", payload)

        # Now we have all logs for the session identified by request_id. Find AWS HTTP debug log for it
        # TODO: If log is older than 7 days we need to ignore them since we do not keep those logs

        found_aws_log_link = False
        for log in logs_for_session.get_results():

            # If this log contains an AWS S3 link, get the link and save it and do not check other logs
            # (since we already find what we are looking for)
            if "AWS CLI" in log.message:
                http_debug_log_link = log.message.split("AWS")[1].split("CLI: ")[1].strip()
                status = log.message.split("\n")[0].split(":")[1].strip()
                unique_file_name = status + "_" + log.requestId + "_" + log.providerName + ".log"

                metadata.append({
                    "status": status,
                    "unique_file_name": unique_file_name,
                    "log_path": http_debug_log_link,
                    "requestId": log.requestId,
                    "credentialsId": log.credentialsId,
                    "userId": log.userId,
                    "providerName": log.providerName,
                    "timestamp": log.timestamp
                })
                download_requests.append(AWSRequest(http_debug_log_link,
                                                    os.path.join(output_folder, unique_file_name)))
                found_aws_log_link = True

        if found_aws_log_link:
            await send_message(ws, "Found AWS log link for request with requestID = " + request_id
                               + " and provider name = " + provider_name, payload)
        else:
            await send_message(ws, "Could not find AWS log link for request with requestID = " + request_id
                               + " and provider name = " + provider_name, payload)

    with open(os.path.join(output_folder, "metadata.json"), "w", encoding="utf-8") as out:
        json.dump(metadata, out, indent=4)

    # Create the necessary Terminal command to download all AWS debug logs that we discovered
    download_log_command = AWSManager.create_download_command(download_requests)

    # TODO: We can find a way to send the Google password of the user so user will not need to enter his
    # password every time, we should just find a safe and convenient way to do so
    """
        Assuming that we have a good way to send the Google password to this method and it is stored in 
        "password" variable, the following way is to make the script use the password automatically
        
        keyring.set_password("aws-google-auth", username, password)
        
        and then in "aws_google_auth.cli" call below we need to use --keyring argument
    """

    # Create the necessary Terminal command to authenticate for AWS
    if authenticate_for_aws_command is None:
        await send_message(ws, "You will be asked for credentials, please check the Terminal where the Python server"
                               " is running and provide the necessary input", payload)
        authenticate_for_aws_command = aws_google_auth.cli(
            ["-d", "3600", "-u", username, "-I", idp_id, "-S", sp_id, "--ask-role",
             "--resolve-aliases", "--print-creds"])

    command = authenticate_for_aws_command + " && " + download_log_command

    await send_message(ws, "Running the commands to download the logs, you can check it on Python server", payload)
    os.system(command)
    organise(output_folder)
    await send_message(ws, "Logs are downloaded", payload)
    print("Download is completed")
    return authenticate_for_aws_command


async def main():
    parser = argparse.ArgumentParser(description="Kibana fetcher")
    parser.add_argument(
        "-c",
        "--cookie",
        type=str,
        help="Cookie to be used for Kibana"
    )
    parser.add_argument(
        "-q",
        "--query",
        type=str,
        help="JSON string for Kibana query"
    )
    parser.add_argument(
        "-h",
        "--host",
        type=str,
        help="Host to be used (example: https://kibana.aggregation-production.tink.network"
    )
    parser.add_argument(
        "-o",
        "--output",
        type=str,
        help="Path for output folder"
    )
    parser.add_argument(
        "-u",
        "--username",
        type=str,
        help="Username (example: berk.gedik@tink.se)"
    )

    # Execute "cat .aws/config" to fetch the following parameters
    parser.add_argument('-i', '--idp_id', help='Google SSO IDP identifier ($GOOGLE_IDP_ID)')
    parser.add_argument('-s', '--sp_id', help='Google SSO SP identifier ($GOOGLE_SP_ID)')

    args = parser.parse_args()

    if args.cookie is None:
        print("Cookie must be provided by argument -c. Exiting...")
        sys.exit(1)

    if args.query is None:
        print("Query must be provided by argument -q. Exiting...")
        sys.exit(1)

    if args.host is None:
        print("Host must be provided by argument -h. Exiting...")
        sys.exit(1)

    if args.output is None:
        print("Output path must be provided by argument -q. Exiting...")
        sys.exit(1)

    if args.username is None:
        print("Username must be provided by argument -u. Exiting...")
        sys.exit(1)

    if args.idp_id is None:
        print("Google SSO IDP identifier must be provided by argument -i. Exiting...")
        sys.exit(1)

    if args.sp_id is None:
        print("Google SSO SP identifier must be provided by argument -s. Exiting...")
        sys.exit(1)

    await run(cookie=args.cookie,
              query=args.query,
              output_folder=args.output,
              host=args.host,
              username=args.username,
              idp_id=args.idp_id,
              sp_id=args.sp_id)


if __name__ == "__main__":
    asyncio.get_event_loop().run_until_complete(main())
