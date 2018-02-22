import pandas as pd
import re
from nltk.corpus import stopwords 
from nltk import word_tokenize
from nltk.tokenize import TweetTokenizer
from nltk.stem.wordnet import WordNetLemmatizer
from gensim import corpora, models

#load CSV
df_tweets = pd.read_csv("tweets_2017-01-17.csv", engine="python")
df_tweets_2 = pd.read_csv("tweets_2017-01-16.csv", engine="python")
df_tweets_3 = pd.read_csv("tweets_2017-01-15.csv", engine="python")
df_tweets_4 = pd.read_csv("tweets_2018-01-14.csv", engine="python")
frames = [df_tweets, df_tweets_2, df_tweets_3, df_tweets_4]
df_tweets = pd.concat(frames)

df_tweets = df_tweets.sort_values(['Tweets']).groupby('User').agg({'Tweets':'sum'})
list_tweets = df_tweets["Tweets"].tolist()


#remove URLs, words containing digits, and replacing 'n't' and replacing apostrophies
list_tweets = [re.sub(r"\w+:\/{2}[\d\w-]+(\.[\d\w-]+)*(?:(?:\/[^\s/]*))*", "", str(tweet)) for tweet in list_tweets ]
list_tweets = [re.sub("\S*\d\S*", "", tweet).strip() for tweet in list_tweets]
list_tweets = [re.sub("n't*", "not", tweet) for tweet in list_tweets]
list_tweets = [tweet.replace("'","") for tweet in list_tweets]

#tokenize tweets
tokenizer = TweetTokenizer()
list_tweets = [[word.lower() for word in word_tokenize(tweet)] for tweet in list_tweets]

#remove stopwords from tokens
stopwords = set(stopwords.words("english"))
unigram = [word for tweet in list_tweets for word in tweet if len(word) == 1]
bigram =  [word for tweet in list_tweets for word in tweet if len(word) == 2]
stopwords.update(bigram, unigram, ['.', ',', '"', "'", '?', '!', ':', ';', '(', ')', '[', ']', '{', '}', '-', "..."])

list_tweets = [[token for token in tweet if token not in stopwords] for tweet in list_tweets] 
list_tweets = [[token for token in tweet if not token.endswith("rt")] for tweet in list_tweets]
 
#lemmatize stopped tokens 
lemmatizer = WordNetLemmatizer()
list_tweets= [[lemmatizer.lemmatize(token) for token in tweet] for tweet in list_tweets]

#build corpus dictionary from tweet_list; assigns unique id to each token
dictionary = corpora.Dictionary(list_tweets)
dictionary.save("final_dictionary_user_analysis_user_pool_defaultalpha_40t_70p.dict")

#convert corpus into bag of words matrix
bag_of_words_matrix = [dictionary.doc2bow(tweet) for tweet in list_tweets]

#running and training LDA model on the document term matrix.
lda_params      = {"num_topics": 40, "passes": 70}

ldamodel = models.LdaModel(bag_of_words_matrix, id2word=dictionary, num_topics=lda_params["num_topics"], passes=lda_params["passes"])

#serialize bag of word matrix/vector space corpus 
corpora.MmCorpus.serialize("final_market_matrix_user_analysis_user_pool_defaultalpha_40t_70p.mm", bag_of_words_matrix)

print(ldamodel.print_topics(num_topics = 50))
ldamodel.save("final_lda_model_user_analysis_user_pool_defaultalpha_40t_70p.lda")

#all files are saved into you working directory
#even though we just saved 3 files explicitly you will find more in your working directory
#follow instructions in the file 'Instructions LDAvis' inside the folder 'LDAvis data'
#in order to visualize the LDA model go the the Jupyter Notebook










