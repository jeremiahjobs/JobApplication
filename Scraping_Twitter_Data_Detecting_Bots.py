import tweepy as ty
import botometer
import pandas as pd
import time
import datetime

timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d')

#twitter api credentials
print("Laoding API credentials...")
consumer_key='mibaANdTsz7nMdMLB2iibgEy7'
consumer_secret='IvPlYOJC3fuJA0dzZbhCnCMTd4MPu1mb5MSR47W3610b4gOHQs'
access_token='283690451-kBOI5UdLQKE36RsuVdPy2bOtXZdU8Tenk9pQCnM2'
access_token_secret='6AQCBZRYmJRheTSJmtNyXVluaJKhoTvLOkhdTsO7ojYb9'

mashape_key = "wPVjQ80nJTmshnXvasHdArGOAJKTp1K2NXQjsnY8Y0BmFhcLtm"

twitter_app_auth = {'consumer_key': consumer_key,
                    'consumer_secret': consumer_secret,
                    'access_token': access_token,
                    'access_token_secret': access_token_secret
                    }


#Twitter authentication
print("Validating Twitter authentication...")
auth = ty.OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token, access_token_secret)
api = ty.API(auth, wait_on_rate_limit=True)


#Get trending Topics for USA
print("Scraping trending topics...")
country = 23424977
trends = api.trends_place(country)
trends_names = pd.DataFrame()

for i in range(len(trends[0]["trends"])):
    trend_temp = {
            "name": trends[0]["trends"][i]["name"],
            "url": trends[0]["trends"][i]["url"]
            }
    df_trend_names = pd.DataFrame(trend_temp, index=[i])
    trends_names = trends_names.append(df_trend_names)
print(trends_names)

#writing to csv so in case of a exception we don't lose the data
trends_names.to_csv("trends_" + timestamp + ".csv", index = False)


#Get all users activ on trend
print("Scraping users for each trend...")    
user_id_list = []
user_screen_name_list = []
user_trend = []	 

users = {'id':user_id_list, 'screen_name':user_screen_name_list}

for row in trends_names.itertuples():
    print("Iterating through trends list...")
    trend = (row[1])
    print(trend)
    count = 0
    for tweet in ty.Cursor(api.search, q=(trend)).items(500):
        print("Pulling user from a trend, user number... ")
        print(count)
        user_protected = tweet.author.protected
        if user_protected == False:
            user_id = tweet.author.id_str
            user_screen_name = tweet.author.screen_name
            if not user_id_list.__contains__(user_id):
                user_id_list.append(user_id)
                user_trend.append(trend)
            if not user_screen_name_list.__contains__(user_screen_name):
                user_screen_name_list.append(user_screen_name)
            count += 1
    
    user_from_trends = pd.DataFrame({"User ID":user_id_list, "User Name":user_screen_name_list, "Trend":user_trend})
 
#writing to csv so in case of a exception we don't lose the data
user_from_trends.to_csv("user_from_trends_" + timestamp + ".csv", index = False)


# initialise Botometer
print("Initialize botometer...")
bom = botometer.Botometer(wait_on_ratelimit=True,
                          mashape_key=mashape_key,
                          **twitter_app_auth)


# Get Score for a Twitter Account	- validating each account takes up to 6 seconds
user_score = pd.DataFrame() 
index_count = 0


user_from_trends_cut = user_from_trends.iloc[index_count:]

for row in user_from_trends_cut.itertuples():
    accountname = row[2]
    count = 0

    try:
        if api.user_timeline(accountname) == []:
            print("no timeline!")
            continue
        else:
            print("Get score for user...")
            result = bom.check_account(accountname)
            count =+ 1
            print(accountname)
    except ty.error.TweepError as err:
        print(err)
        print(accountname)
        continue
    except ConnectionError as err2:
        print(err2)
        print(accountname)
        time.sleep(5)
        break
            
    new_user_score = {
    'Id': result['user']['id_str'],
    'User': result['user']['screen_name'],
    'score':result[ 'scores']['english'],
    'trend':user_from_trends_cut["Trend"]
    }
    df_score = pd.DataFrame(new_user_score, index=[index_count])
    user_score = user_score.append(df_score)
    print("FOUND A BOT!!!")
    index_count += 1
    
    user_score.to_csv("scored_user_" + timestamp + ".csv", index = False)
    

file_name = "scored_user.csv"

#writing to csv so in case of a exception we don't lose the data
df = pd.read_csv("scored_user_" + timestamp + ".csv", engine='python')
  
    

# Get users time line text per post

all_tweets = []
all_tweet_ids = []
all_users = []
all_user_ids = []
all_created_at = []
all_favourite_counts = []
all_retweets_count = []
user_counter = 0

#bots = user_score.drop(user_score[user_score.score < 0.5].index).reset_index(drop=True)

print("Get users timeline...")
for row in user_score.itertuples():
    
    user = row[2]
    user_id = row[1]
    
    try:
        timeline = api.user_timeline(screen_name = user, count = 200, include_rts = True, tweet_mode="extended")
    except ty.error.TweepError as err:
        print(err)
        print(user)
        continue
    except ConnectionError as err2:
        print(err2)
        print(user)
        time.sleep(5)
        break
    
    for status in timeline: 
        counter = 0
        print("Scraping tweet number...")
        print(counter)
        counter += 1
        all_retweets_count.append(status.retweet_count)
        all_favourite_counts.append(status.favorite_count)
        all_created_at.append(status.created_at)
        all_tweets.append(status.full_text)
        all_users.append(user)
        all_user_ids.append(user_id)
        all_tweet_ids.append(status.id)
    user_counter += 1
    print(user_counter)
    
tweets = pd.DataFrame({
        'User': all_users, 
        'User_ID': all_user_ids,
        'Tweets': all_tweets, 
        'Tweet_IDs':all_tweet_ids,
        'Created at': all_created_at,
        'Favourite count':  all_favourite_counts,
        'Retweet count': all_retweets_count
        })
        
tweets_with_score = pd.merge(tweets, user_score, on='User')

bots = tweets_with_score.drop(tweets_with_score[tweets_with_score.score <= 0.6].index).reset_index(drop=True)

user = tweets_with_score.drop(tweets_with_score[tweets_with_score.score > 0.6].index).reset_index(drop=True)


#Write tweets data frame into a csv file

bots.to_csv("bots" + timestamp + ".csv", index=False)

user.to_csv("user" + timestamp + ".csv", index=False)

   


















