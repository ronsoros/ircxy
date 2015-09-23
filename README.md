# ircxy
IRCXY IRCv4 Implementation

## starting the server

java TES -p=port

port: port to listen on

configure it by editing ircxy.conf, or start it up and
do: /oper user pass
then using the (beta testing, experimental) CONF command,
add an O-line: /quote CONF add o-line N:myusername:mypassword

modify your motd by making/editing motd.txt

## compiling

just do: 

   $ cd src
   $ javac *.java and it is compiled! You need JDK for this.

## i use windows!

Grab the latest .jar file from http://aws.ronsor.net/ircxy.jar

You will need a copy of ircxy.conf from here.

after you have a copy of ircxy.conf and have customized it, and you have the ircxy.jar file,
double click the .jar file. your server is now running!
