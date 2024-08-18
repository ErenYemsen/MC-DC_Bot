# MC-DC Bot

A discord bot with GUI
to manage your Minecraft server and optionally use 
Oracle Cloud Infrastructure
if you don't want to or can't port forward. 

It uses open source reverse proxy (FRP) to use the Oracle instances as a reverse proxy server 

I tried my best to make sure you need the least amount of work, but you still do.

## Features

- Start, Stop or see the status of the server

- Server will automatically shut down if no one is playing after some time(configurable)

- Send a message to server from discord (`/say`)

- Send a message to discord from server (`!d {message}` in minecraft chat)

- Some admin commands that only owner of the discord server can use(usually for debugging)


-  Send commands to server through discord server 
   - `!command op [name]`
  

- Lockdown mode for server
  - `!server switch lockdown`
  - only owner can use `/server` commands
  

- Names of the players in the server
  - `!server players`
  - lists the players

## How-to-use

1. Open the jar file and fill the Options tab(see [below](#options-tab)), press Save and then Refresh (Only on first use)
2. Press Run on the Status page
3. Press "Install frp to server" button (Only on first use)
4. ???
5. profit

### Options Tab

- [Mandatory] Discord Bot Token
  - Create the Discord bot from Discord Developer Portal
  - Enable all three Gateway Intents (Presence Intent, Server Members Intent, Message Content Intent)
  - Copy and paste your token to the text field


- [Mandatory] Private Key Path
  - Create Oracle Cloud Infrastructure account
  - Create an Ubuntu instance, generate and save your private key file
  - Select the file by pressing the button
  - Example: `/Users/{name}/Downloads/ssh-key.key`
  - [Important] Configure Oracle Cloud Infrastructure config file (See [below](#how-to-configure-oci-config-file))


- [Mandatory] Instance OCID
  - Copy the OCID of OCI instance from Oracle Cloud and paste to the text field


- [Mandatory] Server Path
  - Select the .jar file inside the server folder by pressing the button
  - Example: `/Users/{name}/Documents/Server/`


- [Mandatory] OS
  - Select the OS and the processor's architecture
  - This is required to use the correct FRP client
  - Select "Other" to specify the FRP Client path manually by filling the FRPC Path option


- [Optional] Server to Discord Channel ID
  - The ID of the Discord channel that you want to send the messages 
  that came from Minecraft Server using the `!d` command
  - Leave blank if not wanted


- [Optional] XMX
  - The size of RAM that is allocated to the Minecraft server (GB)
  - Default value is 6


- [Optional] Server Port
  - The port that Minecraft server uses
  - Default value is 25565


- [Optional] Command Timeout
  - The time commands time out (Seconds)
  - Default value is 60
  - Consider increasing if received timed out errors on Discord


- [Optional] Server Timeout Min
  - The time Minecraft server will shut down if there are no players (Minutes)
  - Default value is 5


- [Optional] Use OCI
  - Selection whether Oracle Cloud Infrastructure will be used
  - Default value is True


- [Optional] OCI Timeout
  - The time that program will wait to start the OCI instance if it's stopped
  - Default value is 180


- [Optional] Public Ip OCID
    - If the instance has more than one public ip, paste the OCID of the Public ip to the text field
    - Leave blank if there is only one public ip for the instance
    - Default value is blank


- [Optional] Public Ip
    - If the "Public Ip OCID" option cannot be used, paste the public ip to the text field
    - Default value is blank


- [Optional] FRPC Path
  - If the OS is selected "Other" paste the path to the FRP Client executable to the text field
  - Default value is blank


- [Optional] !command Permissions
  - Select who can use the command
  - Default value is Owner


- [Optional] !server switch lockdown Permissions
    - Select who can use the command
    - Default value is Owner


- [Optional] !server players Permissions
    - Select who can use the command
    - Default value is Owner


- [Optional] Discord bot activity
    - Select the activity for bot
    - Default value is Playing


- [Optional] Discord bot activity message
    - Select the message for bot activity
    - Default value is Owner


- [Optional] Discord bot nickname
    - Select the nickname for bot
    - Default value is MC-DC Bot

### How to Configure OCI Config File
1. Install OCI CLI (refer to [this](https://docs.cloud.oracle.com/iaas/Content/API/SDKDocs/cliinstall.htm), and [this](https://youtube.com/playlist?list=PLKCk3OyNwIzsCHxUC3zWh5l0E2J55VHxU))
2. Execute `oci setup config` this will provide a walkthrough
3. Press Enter to select the default location config (Leave Blank)
4. Enter the user OCID (ocid1.user.oc1..xxxxxx)
5. Enter the tenancy OCID (ocid1.tenancy.oc1..xxxxxx)
6. Enter the region that instance is located
7. Enter Y to generate a key pair
8. Press Enter to use the default location for the key (Leave Blank)
9. Press Enter to use the default name for the key (Leave Blank)
10. Do not use passphrase if asked
11. Upload the created public key to Oracle Cloud

#### Other Things

- The program is only for one Discord server and one Minecraft server

- If received an error, check the logs and console tab for troubleshooting

#### Known Issues

- On Windows FRProxy may be blocked by antivirus software, this is caused by usage of [FRP](https://github.com/fatedier/frp) on the project

#### Changelog

##### V2.0.1
  - STDERR updated
  - logger bugfix

##### V2.0
  - Removed Ngrok completely because I hate it
  - Added Oracle Cloud Infrastructure because I love it
  - Made a GUI for better use
