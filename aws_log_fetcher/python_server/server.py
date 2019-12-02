import asyncio
import websockets
import json
import fetcher
import argparse
from datetime import datetime

parser = argparse.ArgumentParser(description="Kibana fetcher")
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
# TODO: Fetch those values automatically from .aws/config file (or any other file whose path is optionally provided
# as an argument)
parser.add_argument('-i', '--idp_id', help='Google SSO IDP identifier ($GOOGLE_IDP_ID)')
parser.add_argument('-s', '--sp_id', help='Google SSO SP identifier ($GOOGLE_SP_ID)')
args = parser.parse_args()

"""
 Once the
    handler completes, either normally or with an exception, the server
    performs the closing handshake and closes the connection.
"""


async def fetch_server(websocket, path):
    aws_tokens = {}

    while True:
        # Here we expect to fetch query and cookie from browser client
        request_data = await websocket.recv()
        request_data = json.loads(request_data)

        if request_data["path"] is not None:
            output_folder = request_data["path"]
        else:
            output_folder = args.output

        host = request_data["host"]
        timestamp = int(datetime.now().timestamp())

        # Check if we should invalidate the token
        if host not in aws_tokens or aws_tokens[host]["timestamp"] + 3500 <= timestamp:
            aws_tokens[host] = {
                "token": None,
                "timestamp": -1
            }

        aws_token = aws_tokens[host]["token"]

        aws_token = await fetcher.run(
            cookie=request_data["cookie"],
            query=request_data["query"],
            host=host,
            output_folder=output_folder,
            payload=request_data["payload"],
            username=args.username,
            idp_id=args.idp_id,
            sp_id=args.sp_id,
            authenticate_for_aws_command=aws_token,
            ws=websocket
        )

        aws_tokens[host] = {
            "token": aws_token,
            "timestamp": int(datetime.now().timestamp())
        }


start_server = websockets.serve(fetch_server, "localhost", 8765)
asyncio.get_event_loop().run_until_complete(start_server)
print("AWSLogFetcher Server is running and waiting for request")
asyncio.get_event_loop().run_forever()
