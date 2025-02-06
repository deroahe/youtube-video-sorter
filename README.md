# youtube-video-sorter
A Spring Boot application for sorting videos in YouTube playlists by title (alphabetically or numerically).
Tests for sorting methods included.

In order to be able to run this application and actually sort your videos, there are quite a few preparations steps that must be taken.

### tl;dr you have to go to the Google Cloud Console of your Google account, set up a new project, create an OAuth client ID and add the JSON object to the project's resources
I'll list the detailed steps below.

### Before running the application:
- go to the Google Cloud Console
- create a new project and select it
- go to your new project's APIs & Services > Enabled APIs & services
- go to Enable APIs and Services
- search for YouTube Data API v3 and enable it
- go to OAuth consent screen
  - select External and click Create
  - fill in the required fields and click Save and continue
  - click Add or remove scopes
  - add required YouTube scopes (I use ".../auth/youtube.force-ssl", but if you think that's sus, use what you think is best (as long as it allows the application to read and edit your playlists), but don't forget to also edit the SCOPES list in YouTubeService.class)
  - click Save and continue
  - add yourself as a test user by clicking Add users, filling in your email and clicking Add
  - click Save and continue
- go to Enabled APIs & services
  - click to YouTube Data API v3
  - click Credentials
  - click Create credentials and choose OAuth client ID
  - choose Desktop app, fill in the name and click Create
  - click Download JSON
- rename the downloaded file "client_secret.json" and move it to the resources directory (.../youtube-video-sorter/src/main/resources)
- you're ready to run the application

### Running the application:
- open a terminal at the project's root location
- run the following command
  - > ./mvnw spring-boot:run

### Using the application:
- the application needs an authentication token do its thing, so for the first time you run the application and also every time your token expires, you have to authenticate the app like described below: 
  - when calling any endpoint of the application, you will see a link in the application console that redirects you to the Google OAuth consent screen
  - click the link, log in with the Google account that you used to create the OAuth client ID and allow the application to access your YouTube account
- fetching your playlists:
  - open Postman and import the following curl command (or run directly from your terminal) 
    - > curl --location 'http://localhost:8080/api/youtube'
  - what you will see in the response is a JSON containing all your playlists; find the title of the playlist you want to sort and copy its ID
- sorting your playlist:
  - there are 2 query parameters that you can add to the request URL:
    - ascending -> true by default (if not inputted) -> determines if the playlist should be sorted ascending (when true) or descending (when false)
    - sortType:
      - WHOLE_TITLE_ALPHABETICAL by default (if not inputted) -> specifies that the playlist should be sorted by the title, alphabetically
      - HASHTAG_NUMBER_NUMERICAL -> specifies a number should be extracted from each video's title and then used in numerical sorting; this number can be either the first number that follows the first hashtag (#) or if there are no hashtags in the title, then it will be the first number encountered in the title
  - sorting your playlist alphabetically ascending (A to Z):
    - open Postman and import the following curl command (or run directly from your terminal) 
    - > curl --location --request POST 'http://localhost:8080/api/youtube/playlistId'
    - don't forget to replace "playlistId" with the ID of the playlist you want to sort
  - sorting your playlist alphabetically descending (Z to A):
    - open Postman and import the following curl command (or run directly from your terminal) 
    - > curl --location --request POST 'http://localhost:8080/api/youtube/playlistId?ascending=false'
    - don't forget to replace "playlistId" with the ID of the playlist you want to sort
  - sorting your playlist numerically ascending (A to Z):
    - open Postman and import the following curl command (or run directly from your terminal) 
    - > curl --location --request POST 'http://localhost:8080/api/youtube/playlistId?sortType=HASHTAG_NUMBER_NUMERICAL'
    - don't forget to replace "playlistId" with the ID of the playlist you want to sort
  - sorting your playlist alphabetically descending (Z to A):
    - open Postman and import the following curl command (or run directly from your terminal) 
    - > curl --location --request POST 'http://localhost:8080/api/youtube/playlistId?sortType=HASHTAG_NUMBER_NUMERICAL&ascending=false'
    - don't forget to replace "playlistId" with the ID of the playlist you want to sort

## IMPORTANT:
- depending on how many playlists you have and how many videos you have in the playlist that you're sorting, you can very easily run out of queries
  - queries consumed when fetching playlists: 1 query per 50 playlists (if you have 100 playlists, you consume 2 queries; if you have 157 playlists, you consume 4 queries)
  - queries consumed when updating video positions in a playlist: 50 queries per 1 position update (if you need to update the positions of 50 videos, you consume 2500 queries)
  - the daily quota is 10,000 queries; that's very little, so you can very easily run out of queries
### If you run out of queries:
- you can wait until the next day for your quota to refresh
- you can make another Google Cloud Console project by following the steps above
  - in case you choose to make a new project, besides adding the new client_secret.json to the resources, you also have to REMOVE the token found at .../youtube-video-sorter/tokens

### Happy sorting!
