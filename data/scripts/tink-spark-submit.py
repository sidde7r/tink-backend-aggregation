#!/usr/bin/env python
"""
Helper script to execute Tink Java Spark jobs installed with the
`tink-backend-analytics` Debian package.

The script essentially constructs a command line execution and delegates the
execution to `dse spark-submit`. For simple debugging/hacking, you can run the
script with `--log-level DEBUG --dry-run`.
"""

import argparse
import collections
import logging
import subprocess
import sys
import os
import os.path


DEFAULT_TINK_ANALYTICS_PATH = '/usr/share/tink-backend-analytics'
SPARK_USERNAME_PROPERTY = 'spark.cassandra.auth.username'
SPARK_PASSWORD_PROPERTY = 'spark.cassandra.auth.password'


def read_all_properties(f):
  data = {}
  for line in f:
    stripped = line.strip()
    if stripped:
      key, value = stripped.split('=', 1)
      key = key.strip()
      value = value.strip()
      assert key not in data, "Key defined multiple times."
      data[key] = value
  return data


def list_jars(path):
  return [
      os.path.join(path, f)
      for f in os.listdir(path)
      if os.path.isfile(os.path.join(path, f)) and f.endswith('.jar')
  ]


def construct_execution_array(args):
  with open(args.config_file) as f:
    properties = read_all_properties(f)
  assert SPARK_USERNAME_PROPERTY in properties
  assert SPARK_PASSWORD_PROPERTY in properties

  jars = list_jars(args.lib)

  e = []

  if args.dse:
      e +=[
          'dse'
      ]

  e +=[
      'spark-submit',
      '--conf', '='.join((SPARK_USERNAME_PROPERTY, properties[SPARK_USERNAME_PROPERTY])),
      '--conf', '='.join((SPARK_PASSWORD_PROPERTY, properties[SPARK_PASSWORD_PROPERTY])),
      '--conf', 'spark.eventLog.enabled=true',
      '--conf', 'spark.executor.memory=3g',

      # This config is applied when deploy-mode=cluster
      '--conf', 'spark.driver.memory=3g',

      # This config is applied when deploy-mode=client (default)
      '--driver-memory', '3g',

      # Uncomment this to enable gc logs
      #'--conf', 'spark.executor.extraJavaOptions=-XX:+PrintGCDetails -XX:+PrintGCTimeStamps',

      # Uncomment this to run single threaded without master
      #'--master', 'local'
  ]

  if jars:
    e += ['--jars', ','.join(jars)]
  if args.verbose_driver:
    e.append('--verbose')
  e += [
      '--class', args.jobclass,
      args.jar,
  ]

  e += ' '.join(args.jobarguments).split( )

  e += [args.config_file]

  return e


def run(args):
  to_execute = construct_execution_array(args)
  if args.dry_run:
    logging.info('Would have executed: %s', " ".join(to_execute))
    return 0    # OK
  else:
    logging.info('Executing: %s', " ".join(to_execute))
    retcode = subprocess.call(to_execute)
    return retcode


def parse_args():
  parser = argparse.ArgumentParser(description='Execute a Tink Analytics Spark'
      ' job.', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
  parser.add_argument('jobclass', metavar='CLASS', help='The Java class to'
      ' execute. Example: se.tink.analytics.DemoJob')
  parser.add_argument('--lib', metavar='PATH',
      default='{defaultpath}/lib'.format(defaultpath=DEFAULT_TINK_ANALYTICS_PATH),
      help='Path to where all JAR dependencies resides.')
  parser.add_argument('--jar', metavar='PATH',
      default='{defaultpath}/analytics-jobs.jar'.format(defaultpath=DEFAULT_TINK_ANALYTICS_PATH),
      help='Path to the JAR file to execute.')
  parser.add_argument('--cwd', metavar='PATH',
      default=DEFAULT_TINK_ANALYTICS_PATH, help='The working directory where'
      ' the driver will execute. Note that workers will _not_ be executing in'
      ' this path.')
  parser.add_argument('--log-level', metavar='LEVEL', default='WARN',
      help='Increase verbositeness.')
  parser.add_argument('--config-file', default='/etc/tink/tink-analytics.properties',
      help='The configuration file to the Spark job. Given as first argument to'
      ' the main method array. Format of the config file is a Java property'
      ' file.')
  parser.add_argument('jobarguments', nargs='*', metavar='ARG', help='Command line arguments to the Spark driver.')
  parser.add_argument('--dry-run', action='store_true')
  parser.add_argument('--verbose-driver', action='store_true', help='Make the driver execute verbosely.')

  feature_parser = parser.add_mutually_exclusive_group(required=False)
  feature_parser.add_argument('--dse', dest='dse', action='store_true')
  feature_parser.add_argument('--no-dse', dest='dse', action='store_false')
  parser.set_defaults(dse=True)

  return parser.parse_args()


def setup_logging(args):
  '''Global logging initialization.'''
  # Ripped from https://docs.python.org/2/howto/logging.html#logging-to-a-file
  numeric_level = getattr(logging, args.log_level.upper(), None)
  if not isinstance(numeric_level, int):
    raise ValueError('Invalid log level: %s' % args.log_level)
  logging.basicConfig(level=numeric_level, format='%(levelname)s %(asctime)s %(message)s')


def main():
  '''Boring initialization and delegation to run(...).'''
  args = parse_args()
  setup_logging(args)
  logging.debug('Parsed arguments: %s', args)

  if not args.jobclass.startswith('se.tink.analytics.'):
    logging.warn("The jobclass doesn't start with expected 'se.tink.analytics.': %s", args.jobclass)

  return run(args)


if __name__=="__main__":
  sys.exit(main())
