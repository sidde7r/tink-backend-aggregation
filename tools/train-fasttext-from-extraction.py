import subprocess
import re
import StringIO
import contextlib
import datetime
import contextlib
import csv
import logging
import multiprocessing
import os
import os.path
import sys
import tempfile

TEMPDIR = '/mnt/categorization-classification/tmp'

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

LABEL_PATTERN = re.compile("__label__[^ ]+")
def predict(model, inputfile):
	p = subprocess.Popen("pv {0} | sed -e 's/__label__[^ ]+//' | ./fastText/fasttext predict {1} - 1".format(inputfile, model), stdout=subprocess.PIPE, shell=True)
	tries, correct = 0, 0
	with open(inputfile) as f:
		for guess, answer_line in zip(p.stdout, f):
			answer = LABEL_PATTERN.match(answer_line).group(0)
			if guess.strip() == answer.strip():
				correct += 1
			tries += 1
	p.wait()
	return correct, tries


def above(s, threshold):
	return s.isdigit() and int(s) > threshold


def read_users(cat_threshold):
	with open('categorization_levels.txt') as f:
		return set([line.strip().split(' ')[0] for line in f if above(line.strip().split(' ')[1], cat_threshold)])


cutoff_date = datetime.date.today() - datetime.timedelta(days=365)


def convert_file(infilename):
	s = StringIO.StringIO()
	with open(infilename) as f:
		safecontent = f.read().replace('\0', '')
		fakefile = StringIO.StringIO()
		fakefile.write(safecontent)
		fakefile.seek(0)

		reader = csv.reader(fakefile, delimiter="\t")
		for line in reader:
			date, description, category = line[:]
			date = datetime.datetime.strptime(date, '%Y-%m-%d').date()
			if "expenses:" in category and date >= cutoff_date:
				#s.write("__label__" + category + " " + description + "\n")
				s.write("__label__" + category + " " + description.replace(",", " ") + "\n")
				#s.write("__label__" + category + " " + description.replace(",", " ").upper() + "\n")
	return s.getvalue()


def combine(inputfiles, f):
	p = multiprocessing.Pool(40)
	with contextlib.closing(p):
		for result in p.imap_unordered(convert_file, inputfiles, chunksize=300):
			f.write(result)


@contextlib.contextmanager
def tempfilename(*args, **kwargs):
	filename = tempfile.mktemp(*args, **kwargs)
	yield filename
	if os.path.isfile(filename):
		os.unlink(filename)


USERFILE_PATTERN = re.compile('([0-9a-f]{32}).txt')

# Eval set is ~18% of full set.
TRAIN_PATTERN = re.compile('[0-9a-c].*')
EVAL_PATTERN = re.compile('[d-f].*')


def train_and_eval(args):
	threshold = int(args[1])
	logging.info("Threshold: %d", threshold)

	#inputpath = "extraction/user-modified"
	inputpath = "extraction/all"
	logging.info("Input path: %s", inputpath)

	evalfiles = [s for s in os.listdir(inputpath) if USERFILE_PATTERN.match(s) and EVAL_PATTERN.match(s)]
	logging.info("Number of evaluation files: %d", len(evalfiles))
	trainingfiles = [s for s in os.listdir(inputpath) if USERFILE_PATTERN.match(s) and TRAIN_PATTERN.match(s)]
	logging.info("Number of training files: %d", len(trainingfiles))

	users = read_users(threshold)
	evalfiles = [os.path.join(inputpath, s) for s in evalfiles if USERFILE_PATTERN.match(s).group(1) in users]
	logging.info("Number of filtered evaluation files: %d", len(evalfiles))
	trainingfiles = [os.path.join(inputpath, s) for s in trainingfiles if USERFILE_PATTERN.match(s).group(1) in users]
	logging.info("Number of filtered training files: %d", len(trainingfiles))

	with tempfile.NamedTemporaryFile(suffix='.txt', prefix='evaldata', dir=TEMPDIR) as evalf, \
		tempfile.NamedTemporaryFile(suffix='.txt', prefix='trainingdata', dir=TEMPDIR) as trainingf, \
		tempfilename(".bin", "model", dir=TEMPDIR) as modelfile:

		modelfilenosuffix = modelfile[:-4]
		logging.info("Model file without suffix: %s", modelfilenosuffix)

		logging.info("Building evaluation data...")
		combine(evalfiles, evalf)
		evalf.flush()
		logging.info("done.")
		logging.info("Building training data...")
		combine(trainingfiles, trainingf)
		trainingf.flush()
		logging.info("done.")

		logging.info("Training model...")
		os.system("./fastText/fasttext supervised -thread 16 -verbose 1 -input {0} -output {1}".format(trainingf.name, modelfilenosuffix))
		logging.info("Done training model.")

		logging.info("Extracting size of model.")
		os.system("du -sh {0}".format(modelfile))

		logging.info("Evalulating model...")
		os.system("./fastText/fasttext test {0} {1}".format(modelfile, evalf.name))
		logging.info("Done evaluating model.")

		logging.info("Calculating accuracy...")
		correct, tries = predict(modelfile, evalf.name)
		logging.info("Tests: %d", tries)
		logging.info("True positive: %d", correct)
		logging.info("Accuracy (for threshold %d): %f", threshold, 1.0 * correct / tries)

	return 0


def train(args):
	modelfile = args[1]

	inputpath = "extraction/all"
	logging.info("Input path: %s", inputpath)

	trainingfiles = [os.path.join(inputpath, s) for s in os.listdir(inputpath) if USERFILE_PATTERN.match(s)]
	logging.info("Number of training files: %d", len(trainingfiles))

	with tempfile.NamedTemporaryFile(suffix='.txt', prefix='trainingdata', dir=TEMPDIR) as trainingf:

		modelfilenosuffix = modelfile[:-4]
		logging.info("Model file without suffix: %s", modelfilenosuffix)

		logging.info("Building training data...")
		combine(trainingfiles, trainingf)
		trainingf.flush()
		logging.info("done.")

		logging.info("Training model...")
		os.system("./fastText/fasttext supervised -thread 36 -verbose 1 -input {0} -output {1}".format(trainingf.name, modelfilenosuffix))
		logging.info("Done training model.")

		logging.info("Extracting size of model.")
		os.system("du -sh {0}".format(modelfile))

	return 0


if __name__=="__main__":
	sys.exit(train(sys.argv))
