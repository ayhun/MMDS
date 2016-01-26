# -*- coding: utf-8 -*-
"""Sentiment analysis implementations.
.. versionadded:: 0.5.0
"""
from __future__ import absolute_import
from collections import namedtuple
import json
from textblob import TextBlob

from textblob.en import sentiment as pattern_sentiment
from textblob.tokenizers import word_tokenize
from textblob.decorators import requires_nltk_corpus
from textblob.base import BaseSentimentAnalyzer, DISCRETE, CONTINUOUS
from textblob.classifiers import NaiveBayesClassifier
import re


positive_emojis = [u"\U0001F601",u"\U0001F602",u"\U0001F603",u"\U0001F604",u"\U0001F605",u"\U0001F606",u"\U0001F609",u"\U0001F60A",u"\U0001F60B",u"\U0001F60C", u"\U0001F60D",u"\U0001F60F",u"\U0001F618",u"\U0001F61A", u"\u2764", u"\u263A", u"\U0001F44D",u"\U0001F44F", u"\U0001F60E"]
negative_emojis = [u"\U0001F612",u"\U0001F613",u"\U0001F614",u"\U0001F616",u"\U0001F61E",u"\U0001F620",u"\U0001F621",u"\U0001F622",u"\U0001F623",u"\U0001F624",u"\U0001F625",u"\U0001F628",u"\U0001F629",u"\U0001F62A",u"\U0001F62B",u"\U0001F62D",u"\U0001F630",u"\U0001F631",u"\U0001F44E", u"\U0001F494", u"\U0001F610"]

neutral_words = [u"iphone", u"battery" u"camera", u"screen"]

positive_emoji_regex = re.compile(u'|'.join(positive_emojis))
negative_emoji_regex = re.compile(u'|'.join(negative_emojis))
neutral_words_regex = re.compile(u'|'.join(neutral_words))

dimensions = [u"iphone", u"battery" u"camera", u"screen"]
months = {u"1": u"August",
          u"2": u"September",
          u"3": u"October",
          u"4": u"November",
          u"5": u"December"}


class NaiveBayesAnalyzer():
    cl = None

    def __init__(self):
        with open('training_data.json', 'r') as f:
            self.cl = NaiveBayesClassifier(f, format='json')
        self.cl.show_informative_features(20)

    def analyze(self, text):
        return self.cl.classify(text)


def create_tweet_buckets_with_bayesian(tweets, use_emoticons=True):
    positive_tweets = []
    negative_tweets = []
    neutral_tweets = []

    naive_bayes_analyzer = NaiveBayesAnalyzer()

    for tweet in tweets:
        if use_emoticons:
            positive_emoji_count = len(positive_emoji_regex.findall(tweet['lowercase_text']))
            negative_emoji_count = len(negative_emoji_regex.findall(tweet['lowercase_text']))
            if positive_emoji_count > negative_emoji_count:
                tweet["polarity"] = u"positive"
                positive_tweets.append(tweet)
                continue
            elif positive_emoji_count < negative_emoji_count:
                tweet["polarity"] = u"negative"
                negative_tweets.append(tweet)
                continue
        polarity = naive_bayes_analyzer.analyze(corrected)
        if polarity == "pos":
            tweet["polarity"] = u"positive"
            positive_tweets.append(tweet)
        elif polarity == "neg":
            tweet["polarity"] = u"negative"
            negative_tweets.append(tweet)
        else:
            tweet["polarity"] = u"neutral"
            neutral_tweets.append(tweet)

    return positive_tweets, negative_tweets, neutral_tweets


def create_tweet_buckets_with_textblob(tweets):
    positive_tweets = []
    negative_tweets = []
    neutral_tweets = []

    for tweet in tweets:
        corrected = TextBlob(tweet["lowercase_text"]).correct()
        polarity = corrected.sentiment.polarity
        if polarity > 0:
            tweet["polarity"] = u"positive"
            positive_tweets.append(tweet)
        elif polarity < 0:
            tweet["polarity"] = u"negative"
            negative_tweets.append(tweet)
        else:
            tweet["polarity"] = u"neutral"
            neutral_tweets.append(tweet)

    return positive_tweets, negative_tweets, neutral_tweets


def load_tweets_from_file(filename):
    tweets = []
    for line in open(filename, 'r'):
        tweet = json.loads(line)

        tweets.append(tweet)
    return tweets


def filter_non_english_tweets(tweets):
    filtered_tweets = []
    for tweet in tweets:
        if tweet["lang"] != "en":
            continue
        filtered_tweets.append(tweet)
    return filtered_tweets


def filter_link_tweets(tweets):
    filtered_tweets = []
    for tweet in tweets:
        if "http" in tweet["lowercase_text"]:
            continue
        filtered_tweets.append(tweet)
    return filtered_tweets


def lowercase_tweets(tweets):
    for tweet in tweets:
        tweet["lowercase_text"] = tweet["text"].lower()
    return tweets


def filter_neutral_words(tweets):
    for tweet in tweets:
        tweet["lowercase_text"] = neutral_words_regex.sub("", tweet["lowercase_text"])
    return tweets


def filter_duplicate_tweets(tweets):
    tweet_dictionary = {}
    filtered_tweets = []
    for tweet in tweets:
        if tweet["lowercase_text"] not in tweet_dictionary:
            filtered_tweets.append(tweet)
            tweet_dictionary[tweet["lowercase_text"]] = True
    return filtered_tweets


def filter_tweets(tweets):
    tweets = filter_link_tweets(tweets)
    tweets = filter_non_english_tweets(tweets)
    tweets = filter_neutral_words(tweets)
    tweets = filter_duplicate_tweets(tweets)
    return tweets


def report_for_dimension(dimension):
    print "Creating report for %s" % dimension
    for month in months:
        print "Creating report for the month %s" % months[month]
        tweets = load_tweets_from_file(u"./data/%s_%s.json" %(dimension, month))
        tweets = lowercase_tweets(tweets)
        tweets = filter_tweets(tweets)
        positive_tweets, negative_tweets, neutral_tweets = create_tweet_buckets_with_textblob(tweets)
        total_count = len(tweets)
        positive_tweets_count = len(positive_tweets)
        negative_tweets_count = len(negative_tweets)
        neutral_tweets_count = len(neutral_tweets)

        print "%d\t%d\t%d\t%d" % (total_count, positive_tweets_count, negative_tweets_count, neutral_tweets_count)
        print "%d\t%d\t%d\t%d" % (round(total_count*100.0/total_count, 1),
                                  round(positive_tweets_count*100.0/total_count, 1),
                                  round(negative_tweets_count*100.0/total_count, 1),
                                  round(neutral_tweets_count*100.0/total_count, 1))


def main():
    for dimension in dimensions:
        report_for_dimension(dimension)


if __name__ == "__main__":
    main()

#naiveBayesAnalyzer=NaiveBayesAnalyzer()
#testTweet=TextBlob('iphone battery going to shit')
#corrected=testTweet.correct()

#print corrected.sentiment
#naiveBayesAnalyzer.analyze(corrected)
#print naiveBayesAnalyzer.analyze('I feel happy')
    
    
   
    

