from typing import List


class AWSRequest:

    def __init__(self, link, output_path):
        self.link = link
        self.output_path = output_path


class AWSManager:

    @staticmethod
    def create_download_command(download_requests: List[AWSRequest]):

        # Commands to execute to get logs
        commands_to_execute = []

        for request in download_requests:
            commands_to_execute.append(
                "aws s3 cp " + request.link + " " + request.output_path)

        command = " && ".join(commands_to_execute)
        return command
