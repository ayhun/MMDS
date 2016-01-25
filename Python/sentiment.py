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

positive_emoji_regex = re.compile('|'.join(positive_emojis))
negative_emoji_regex = re.compile('|'.join(negative_emojis))

class NaiveBayesAnalyzer():
        train = [
    	('Why are iPhone battery so shit?', 'neg'),
    	('When will iPhone battery life never not be a piece of shit', 'neg'),
    	('Battery life is ok', 'pos'),
    	('My Iphone 6 battery has started dying', 'neg'),
    	("iPhone 5s battery is such shit", 'neg'),
    	('iPhone battery is fucking shit at times like this', 'neg'),
    	('My battery on my iPhone good', 'pos'),
    	("the iphone 6s battery is so good", 'pos'),
    	('Still waiting for the day when iPhone battery life is decent ', 'neg'),
    	('iPhone battery life is garbage.', 'neg'),
    	('iPhone.Its easy to use and has a good battery life.','pos'),
    	('Battery life is crappy','neg'),
    	('Battery life is bad','neg'),
    	('I hate my phones battery','neg'),
    	('iPhones battery sucks for sure','neg'),
    	('iPhones love my iPhones battery life','pos'),
    	('iPhones battery is just fine','pos'),
    	('the battery life is actually good on the phone','pos'),#
    	('the battery life is perfect','pos'),
    	('the battery life is not perfect','neg'),
    	('the battery lastst for a day perfectly','pos'),
    	('the battery life could be better','neg'),
    	('Worst thing about iPhone is battery','neg'),
    	('iPhones battery is good enough','pos'),
    	('I love the Iphone 6S this battery is good','pos'),
    	('iPhone 6S Plus battery is pretty good so far. ','pos'),
    	('Seriously the battery life in the iPhone 6S is supper good','pos'),
	('The battery on an iPhone is so bad','neg'),
	('I have an iPhone 5c and the battery is so bad on it','neg'),
	('why does the phone battery suck on every iPhone','neg'),
	('iphone battery you suck','neg'),
	('I need a hook up on iPhone battery this hoe suck','neg'),
	('This iPhone battery really suck!','neg'),
	('iPhone battery suck! Even on low power mode','neg'),
	('Turnout the new iphone battery suck.','neg'),
	('for christs sake the iPhone 5 battery is a load of crap','neg'),
	('As I didnt have an iPhone for 5 months I forgot how crap the battery is','neg'),
	('iPhone battery life is crap Im so done','neg'),
	('As I didnt have an iPhone for 5 months I forgot how crap the battery is','neg'),
	('My battery is screwed','neg'),
	('I hate this battery','neg'),
	('This battery is a piece of shit','neg'),
	('iPhone battery still draining like crazy,','neg'),
	('I like my phones battery life','pos'),
	('Ever felt like screaming when the battery on your iPhone dies?','neg'),
	('iPhone battery drain so fast','neg'),
	('Really love this new iPhone,','pos'),
	('Battery life on iPhone 4s is shocking, cant live like this much longer!','neg'),
	('battery annoys me a lot','neg'),
	('Draining battery.Annoying!','neg'),
	('iPhone batter life is garbage','neg'),
	('The batter life on the iPhone 6 is fucking amazing','pos'),
	('Amazing battery life','pos'),
	('Amazed by new iPhones battery','pos'),
	('If I must say, the iPhone batter life has drastically improved.','pos'),
	('First day with my new iPhone 6s and the batter life in comparison to the 5c is crazy.','pos'),
	('Love how the iPhone 6 batter lasts so long','pos'),
	('I love new phones man.','pos'),
	('Still waiting for the day when iPhone battery life is decent','neg'),
	('iPhone batterys die so fast','neg'),
	('battery is dying','neg'),
	('Batteries die in half a day','neg')]

        cl = None
     
        def __init__(self):
            with open('training_data.json', 'r') as f:
                self.cl = NaiveBayesClassifier(f, format='json')    
            self.cl.show_informative_features(20)
	def analyze(self, text):
	    return self.cl.classify(text)
	    #cl.show_informative_features(32)


if __name__ == "__main__":
    tweets = []
    for line in open('battery.json', 'r'):
       tweets.append(json.loads(line))
        
    #print(tweets[0]['text'])
    naiveBayesAnalyzer=NaiveBayesAnalyzer()
    correct=0
    false=0
    
    for x in range(0,len(tweets)):
        if "http" in tweets[x]['text']:
            continue 
        blob=TextBlob(tweets[x]['text'])
        print tweets[x]['text']
        pos_emojis = len(positive_emoji_regex.findall(tweets[x]['text']))
        neg_emojis = len(negative_emoji_regex.findall(tweets[x]['text']))
        
        corrected=blob.correct()
        #if corrected.detect_language() != "en":
        #    continue
        
        if corrected.sentiment.polarity == 0:
            continue
        
        #TextBlob implementation
        if corrected.sentiment.polarity>0:
        	analyze1='pos'
        else:
        	analyze1='neg'
        print corrected.sentiment.polarity
        
        if pos_emojis > neg_emojis:
            #Emoji
            analyze2='pos'
        elif neg_emojis > pos_emojis:
            analyze2='neg'
        else:
            #Naive Bayes
            analyze2=naiveBayesAnalyzer.analyze(corrected)
         
        if analyze1==analyze2:
        	correct=correct+1
        	print 'correct'
        else:
        	false=false+1
        	print 'false'
        	   
    
    print 'correct:'+str(correct)
    print 'false:'+str(false)
    
    
    
    
    
    #naiveBayesAnalyzer=NaiveBayesAnalyzer()
    #testTweet=TextBlob('iphone battery going to shit')
    #corrected=testTweet.correct()
    
    #print corrected.sentiment
    #naiveBayesAnalyzer.analyze(corrected)
    #print naiveBayesAnalyzer.analyze('I feel happy')
    
    
   
    

