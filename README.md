
---] sentence2songs - build given sentence out of song titles [--- 

This utility reads lines from standard input and attempts 
to compose them out of song titles pulled from Spotify database
(http://developer.spotify.com/technologies/web-api/).
 
Main features:
	- parallel queries and response processing to Spotify database
 	- query response caching from Apache's HTTP client
	- tree-based internal representation of known tracks titles
		- allows fast lookup of repeated queries
		
---] Usage -------------------------------------------------- [---

	Run by:
		java -jar ./lib/sentence2songs.jar
		
	The input sentences are read as lines from standard input.
	
	The output is in format: 
		<spotify:track:uri> <track title>
		
	The track URI is omitted in case it was not possible to match the
	given part of the sentence with known song titles.
	
---] Sample output ------------------------------------------ [---

black dog ramble on fuel
	  spotify:track:1r4QUamMkx8zFubdebDbFH black dog
	  spotify:track:0Hh9YWV4pXiJ7QwmEUpjkP ramble on
	  spotify:track:2Ef9nSuFrEBlHfXbkp8BkH fuel

mama said: orion, welcome home
	  spotify:track:0xPWcpHa0tJmerVInCccsV mama said
	  spotify:track:4qGRu4gQo4ILlztsdXoOGQ orion
	  spotify:track:48Ih1FjoQ5KoCpsdKUk8Eu welcome home 

if I can't let it go out of my mind
	  spotify:track:2mdctbLUgYc2KtQDvGu7cH if i can't
	  spotify:track:2RqZFOLOnzVmHUX7ZMcaES let it go
	  spotify:track:3zageWW7Zbqdo0edJI8StA out of my mind

	  
