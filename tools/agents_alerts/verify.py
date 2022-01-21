"""
Connectivity alert templates verification script.
Performs many static checks against malformed definitions of alerts, including:
- malformed YAML files
- syntax errors in PromQL expressions
- missing or mistyped fields in alerts definitions

List of performed checks is incomplete, that is there are some invalid definitions
that pass checks, for instance:
- wrong/non-existent team name in labels/who
- recording rules identifier typos
- (in some cases) using a template value that's not present in values YAML file

tink-backend-aggregation repo root path is assumed to be ~/src/tink-backend-aggregation,
otherwise it can be specified by passing it as the first and only argument:

$ python3 verify.py [root_path]

helm and promtool are required (for chart rendering and PromQL semantic checks, respectively),
as well as PyYAML and schema libraries for Python 3:

$ brew install helm
$ brew install prometheus
$ pip3 install -r requirements.txt

Script is Connectivity-specific (agent alerts), as some paths are hardcoded,
but it's possible to make it more generic in the future.
"""
import os
import pathlib
import re
import subprocess
import sys
import tempfile
from typing import Dict, Any, List, Tuple, Optional

from schema import And, Optional as SchemaOptional, Regex, Schema, SchemaError
import yaml


PROMTOOL_CMD = 'promtool'
HELM_CMD = 'helm'
ERROR_MSG_SEPARATOR = '=' * 50


def print_err(*args, **kwargs):
    kwargs['file'] = sys.stderr
    print(*args, **kwargs)


def bytes_to_str(b: bytes) -> str:
    return b.decode('UTF-8')


def remove_prefix(s: str, prefix: str) -> str:
    if s and s.startswith(prefix):
        return s[len(prefix):]
    else:
        return s


# based on https://stackoverflow.com/questions/2319019/using-regex-to-remove-comments-from-source-files
def remove_comments(s: str) -> str:
    pattern = r"(\".*?\"|\'.*?\')|(# [^\r\n]*$)"
    regex = re.compile(pattern, re.MULTILINE | re.DOTALL)

    def _replacer(match):
        if match.group(2) is not None:
            return ""
        else:
            return match.group(1)

    return regex.sub(_replacer, s)


def sanitize_schema_errors(errors: Optional[List[Optional[str]]]) -> List[str]:
    if not errors:
        errors = []
    return [line for line in errors if line is not None]


def get_rule_name(rule: Dict[str, Any]) -> Optional[str]:
    if 'alert' in rule:
        return rule['alert']
    elif 'record' in rule:
        return rule['record']
    else:
        return None


class Verifier:
    NON_EMPTY_STR = And(str, len, error='Value should be a non-empty string')
    ALERT_RULE_SCHEMA = Schema({
        'alert': NON_EMPTY_STR,
        'expr': NON_EMPTY_STR,
        SchemaOptional('for'): NON_EMPTY_STR,
        'labels': Schema({
            SchemaOptional('priority'):
                Regex('^P[1-4]$', error='Priority must be one of: P1, P2, P3, P4'),  # defaults to P3 in opsgenie

            'severity': Regex('^(urgent|ping)$', error='Severity must be one of: urgent, ping'),
            'who': NON_EMPTY_STR,
            SchemaOptional(str): NON_EMPTY_STR
        }),
        'annotations': {
            'description': NON_EMPTY_STR,
            SchemaOptional('summary'): NON_EMPTY_STR,
            SchemaOptional('tags'): NON_EMPTY_STR,
            SchemaOptional(str): NON_EMPTY_STR
        },
        SchemaOptional(str): NON_EMPTY_STR
    }, ignore_extra_keys=True)

    RECORDING_RULE_SCHEMA = Schema({
        'record': NON_EMPTY_STR,
        'expr': NON_EMPTY_STR,
        SchemaOptional('labels'): {
            SchemaOptional(str): NON_EMPTY_STR
        }
    })

    def __init__(self, rule_group_name: str, rules: List[Dict[str, Any]]):
        self.warnings = []

        for rule in rules:
            rule_name = get_rule_name(rule)
            self._preprocess_rule(rule)

            try:
                if 'alert' in rule:
                    Verifier.ALERT_RULE_SCHEMA.validate(rule)
                elif 'record' in rule:
                    Verifier.RECORDING_RULE_SCHEMA.validate(rule)
            except SchemaError as e:
                self._warn_schema_error(e, rule_name)

        with tempfile.TemporaryDirectory() as tmpdir:
            tmp_filename = os.path.join(tmpdir, rule_group_name + '.yaml')

            # format expected by promtool
            rule_group_dict = {
                'groups': [{
                    'name': rule_group_name,
                    'rules': rules
                }]
            }
            with open(tmp_filename, 'w') as tmp_file:
                yaml.dump(rule_group_dict, tmp_file)

            promtool_run = subprocess.run([PROMTOOL_CMD, 'check', 'rules', tmp_filename],
                                          stdout=subprocess.PIPE, stderr=subprocess.PIPE)

            if promtool_run.returncode != 0:
                self._warn(sanitize_promtool_error_msg(bytes_to_str(promtool_run.stderr)))

    def _warn(self, msg: str, rule_name: Optional[str] = None):
        if rule_name:
            self.warnings.append(f'[{rule_name}]\n{msg}')
        else:
            self.warnings.append(msg)

    def _warn_schema_error(self, e: SchemaError, rule_name: Optional[str]):
        def _lines():
            indent = 0
            for line in sanitize_schema_errors(e.autos):
                yield (' ' * indent) + line
                indent += 2
            yield 'Details:'
            yield from sanitize_schema_errors(e.errors)

        err_msg = '\n'.join(_lines())
        self._warn(err_msg, rule_name)

    def _preprocess_rule(self, rule: Dict[str, Any]) -> None:
        # silently move 'priority' from top-level to labels so that promtool doesn't complain
        if 'alert' in rule and 'priority' in rule:
            labels = rule.get('labels', dict())
            priority = rule.pop('priority')
            if 'priority' in labels:
                self._warn("duplicate 'priority' field; consider keeping it in 'labels' section only",
                           get_rule_name(rule))
            else:
                labels['priority'] = priority


def sanitize_promtool_error_msg(msg: str) -> str:
    def _lines():
        seen = set()
        for line in msg.splitlines()[1:]:
            if 'unmarshal errors' in line:
                continue

            line = line.strip()
            i = line.find(': ')  # strip line numbers
            if i != -1:
                line = line[i + 2:]

            # generally, an error in one alerting rule is likely be present in every other rule
            # filter out duplicate messages to avoid spam
            if line in seen:
                continue

            seen.add(line)
            yield line

    return '\n'.join(_lines())


def split_chart_str(chart_str: str) -> Tuple[str, str]:
    source_line_prefix = '# Source: '
    assert chart_str.startswith(source_line_prefix)
    lines = chart_str.splitlines()

    source_filename = remove_prefix(lines[0], source_line_prefix)

    # comments within expressions are not removed by Helm, so we're doing it manually
    chart_str = '\n'.join(remove_comments(line) for line in lines[1:])

    return source_filename, chart_str


def verify_master_chart(master_chart_str: str) -> None:
    charts_with_sources = [split_chart_str(chart.strip()) for chart in master_chart_str.split('\n---\n')]

    all_ok = True
    for source, chart_str in charts_with_sources:
        source = remove_prefix(source, 'tink-backend-aggregation-agents/templates/')
        chart = yaml.safe_load(chart_str)
        assert chart is not None

        rule_group_name = chart['metadata']['name']
        rules = chart['spec']['rules']

        verifier = Verifier(rule_group_name, rules)
        if verifier.warnings:
            all_ok = False
            print(f'[{source}]')
            for warning in verifier.warnings:
                print(warning)
            print(ERROR_MSG_SEPARATOR)

    if all_ok:
        print('OK!')


def main():
    argv = sys.argv
    if len(argv) == 1:
        repo_root = os.path.expanduser("~/src/tink-backend-aggregation/")
    elif len(argv) == 2:
        repo_root = os.path.expanduser(argv[1])
    else:
        print_err('Usage: python3 verify.py [tink-backend-aggregation-repo-root]')
        sys.exit(1)

    charts_dir = os.path.join(repo_root, '.charts/tink-backend-aggregation-agents')
    values_dir = os.path.join(charts_dir, 'values')

    if not pathlib.Path(values_dir).is_dir():
        print_err(f'No such directory: {values_dir}')
        sys.exit(1)

    for cmd in [HELM_CMD, PROMTOOL_CMD]:
        try:
            subprocess.run([cmd], stderr=subprocess.PIPE, stdout=subprocess.PIPE)
        except FileNotFoundError:
            print_err(f'Could not execute command {cmd}. Remember to install necessary packages:')
            print_err(f'$ brew install helm')
            print_err(f'$ brew install prometheus')
            sys.exit(1)

    values_yaml_paths = [os.path.join(values_dir, yaml_path)
                         for _, _, file_paths in os.walk(values_dir)
                         for yaml_path in file_paths]

    for values_path in values_yaml_paths:
        display_values_path = values_path[values_path.rfind('/') + 1:]
        print(f'Checking with values={display_values_path}...')

        helm_run = subprocess.run([HELM_CMD, 'template', '--debug', f'--values={values_path}', charts_dir],
                                  stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        ret_code = helm_run.returncode
        if ret_code != 0:
            print(f'Helm error (exit code {ret_code}):')
            print(bytes_to_str(helm_run.stderr))
            print(ERROR_MSG_SEPARATOR)
            continue

        master_chart_str = remove_prefix(bytes_to_str(helm_run.stdout), '---')
        verify_master_chart(master_chart_str)


if __name__ == '__main__':
    main()
