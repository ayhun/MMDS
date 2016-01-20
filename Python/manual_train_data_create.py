import json
import sys
import os
import random

output_file = "training_data.json"

training_data = []

def save_training_data():
    with open("training_data.json","w") as f:
        f.write(json.dumps(training_data))

if len(sys.argv) < 2:
    print "Please give tweet.json file as argument"
    sys.exit(0)

tweet_json_file = sys.argv[1]

if not os.path.isfile(tweet_json_file):
    print "File %s doesn't exist." % tweet_json_file
    sys.exit(0)

if os.path.isfile(output_file):
    with open(output_file) as f:
        training_data = json.load(f)
        print training_data

with open(tweet_json_file) as f:
    lines = f.readlines()
    random.shuffle(lines)
    for line in lines:
        line_json = json.loads(line)
        print line_json["text"]
        decision = raw_input("pos(p)|neg(n)|skip(s) or quit(q)?")
        if decision == "pos" or decision == "p":
            training_data.append({"text":line_json["text"],"label":"pos"})
        elif decision == "neg" or decision == "n":
            training_data.append({"text":line_json["text"],"label":"neg"})
        elif decision.startswith("q"):
            save_training_data()
            print "Saved training data, quitting"
            sys.exit(0)
        elif decision.startswith("s"):
            print "Skipping"
            continue
        else:
            print "Unrecognized command, write q to save&exit, skipping this tweet"
            continue
