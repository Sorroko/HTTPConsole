HTTPConsole
===========
HTTPConsole - Issue Console Commands Over HTTP

-----------------------

#About
Make requests to http://127.0.0.1:8765/console?command=<command> (assuming you're using the default port and the server is running on localhost). If you're running your server locally, just open up your browser and type http://127.0.0.1:8765/console?command=save-all and SAVE THE WORLD!
	
#Features
 - Issue any command over HTTP that you can issue on the console.
 - Change the listener IP address, port, and log-level through the config file.
 - Get back the output of the issued command.
   - This only works for some commands, specifically commands that are not "threaded".
 - Accepts GET and POST (url or json encoded) requests.
 - Client IP address whitelist/blacklist.
 - Host name filtering.