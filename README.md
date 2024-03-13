A basic discord bot to manage your Minecraft server and optionally ngrok if you dont want to port forward
You may need some java knowledge in order to make it work for yourself because this was only for my use but i tried my best to make sure you don't need it but you still might.

Features
-
-Start, Stop or see the status of the server

-Server will automaticly shut down if no one is playing after some time(configurable)

-Send message to server from discord (/say)

-Send message to discord from server (!d {message} in minecraft chat)

-Some admin commands that only owner of the discord server can use(usually for debugging)

-  Send commands to server through discord server 
   - !command op [name]
  
- Stop, restart ngrok
  - !ngrok stop, !ngrok restart
  - if somethings wrong with ngrok you can stop or restart it and it will send the new ip
  
- Lockdown mode for server
  - !server switch lockdown
  - only owner can use /server commands
  
- Names of the players in the server
  - !server players
  - lists the players

How-to-use
-
1.Edit envexample.txt

- install ngrok(if you want) and config your authtoken (check ngrok website for how to)

- get your discord bot token and enter it in token section

- enter the directory of the server.jar file

- enter the id of the channel that you want the bot to send message drom server to discord

- others are optional and default is probably fine


2.change its name to .env


3.add your bot to your discord


4.run the DiscordBot file on an ide


5.???


6.profit


Other-things
-
- High chance to produce an error because there were not enough testing
- There are some localized messages you can ignore them
- You definietly can add very useful things if you know a bit java

Debug
-
- If you encounter some problems with ngrok just change the env NGROK to false and manually open ngrok


