from ElasticSearchRequestHandler import *
from AWSManager import *
import os
import argparse
import sys
from constants import requestid_query
import websockets
import asyncio
import aws_google_auth
import keyring
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
              aws_token=None,
              ws: websockets.WebSocketServerProtocol = None):

    output_folder = os.path.join(output_folder, get_timestamp())

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)
        await send_message(ws, "Output folder is created " + output_folder, payload)

    # We will first make the exact same query that the user made in the browser and then
    # detect unique requestIDs there
    elasticsearch_manager = ElasticSearchRequestHandler(cookie, host)

    # First determine the set of requestIDs for the result of the query
    result = elasticsearch_manager.make_query(query)
    request_ids = result.get_request_ids()
    await send_message(ws, "Fetched unique request IDs. There are " + str(len(request_ids)) + " ids", payload)

    # Get the timestamps for the lower and upper limit of time range used in the query
    gte = int(json.loads(query)["bool"]["must"][-1]["range"]["@timestamp"]["gte"])
    lte = int(json.loads(query)["bool"]["must"][-1]["range"]["@timestamp"]["lte"])

    metadata = []
    download_requests = []

    for index, request_id in enumerate(request_ids):

        # For each requestID make a query to ElasticSearch to fetch all logs belonging to this requestID
        # Note that we are using a bigger time range to ensure that we will fetch all logs
        # (the time range for the query that the user made might not coincide completely with the time range
        # when the full traffic for this requestID occurred)
        logs_for_session = elasticsearch_manager.make_query(query=json.dumps(requestid_query),
                                                            replacements=[
                                                                ("<requestId>", request_id),
                                                                ("<gte>", str(gte - 10000)),
                                                                ("<lte>", str(lte + 10000))
                                                            ])

        await send_message(ws, "Fetched logs for the flow with requestID = " + request_id
                     + " (flow " + str(index + 1) + "/" + str(len(request_ids)) + ")", payload)

        # Now we have all logs for the session identified by request_id. Find AWS HTTP debug log for it
        # TODO: If log is older than 7 days we need to ignore them since we do not keep those logs

        found_aws_log_link = False
        for log in logs_for_session.get_results():

            # If this log contains an AWS S3 link, get the link and save it and do not check other logs
            # (since we already find what we are looking for)
            if "Flushed debug log for further investigation" in log.message:
                http_debug_log_link = log.message.split("AWS")[1].split("CLI: ")[1].strip()
                metadata.append({
                    "log_path"  : http_debug_log_link,
                    "requestId" : request_id,
                    "userId"    : logs_for_session.find_user_id_by_request_id(request_id),
                    "providerName": logs_for_session.find_provider_name_by_request_id(request_id),
                    "timestamp": logs_for_session.get_timestamp_by_request_id(request_id)
                })
                download_requests.append(AWSRequest(http_debug_log_link,
                                                    os.path.join(output_folder, request_id + ".log")))
                found_aws_log_link = True
                break

        if found_aws_log_link:
            await send_message(ws, "Found AWS log link for request with ID = " + request_id, payload)
        else:
            await send_message(ws, "Could not find AWS log link for request with ID = " + request_id, payload)

    with open(os.path.join(output_folder, "metadata.json"), "w", encoding="utf-8") as out:
        json.dump(metadata, out)

    # Create the necessary Terminal command to download all AWS debug logs that we discovered
    download_log_command = AWSManager.create_download_command(download_requests)

    # TODO: We can find a way to send the Google password of the user so user will not need to enter his
    # password everytime, we should just find a safe and convenient way to do so
    """
        Assuming that we have a good way to send the Google password to this method and it is stored in 
        "password" variable, the following way is to make the script use the password automatically
        
        keyring.set_password("aws-google-auth", username, password)
        
        and then in "aws_google_auth.cli" call below we need to use --keyring argument
    """

    # Create the necessary Terminal command to authenticate for AWS
    if aws_token is None:
        await send_message(ws, "You will be asked for credentials, please check the Terminal where the Python server"
                               " is running and provide the necessary input", payload)
        authenticate_for_aws_command = aws_google_auth.cli(
            ["-d", "3600", "-u", username, "-I", idp_id, "-S", sp_id, "--ask-role",
             "--resolve-aliases", "--print-creds"])
    else:
        authenticate_for_aws_command = aws_token["auth_command"]

    command = authenticate_for_aws_command + " && " + download_log_command

    await send_message(ws, "Running the commands to download the logs, you can check it on Python server", payload)
    os.system(command)
    organise(output_folder)
    await send_message(ws, "Logs are downloaded", payload)
    return {
        "auth_command": authenticate_for_aws_command,
        "timestamp": get_timestamp()
    }


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
