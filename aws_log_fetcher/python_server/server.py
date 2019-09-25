import asyncio
import websockets
import json
import fetcher
import argparse

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

    aws_token = None

    while True:
        # Here we expect to fetch query and cookie from browser client
        request_data = await websocket.recv()
        request_data = json.loads(request_data)

        if request_data["path"] is not None:
            output_folder = request_data["path"]
        else:
            output_folder = args.output

        # TODO: Invalidate "aws_token" if timestamp is too old

        aws_token = await fetcher.run(
            cookie=request_data["cookie"],
            query=request_data["query"],
            host=request_data["host"],
            output_folder=output_folder,
            payload=request_data["payload"],
            username=args.username,
            idp_id=args.idp_id,
            sp_id=args.sp_id,
            aws_token=aws_token,
            ws=websocket
        )

start_server = websockets.serve(fetch_server, "localhost", 8765)

asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()
