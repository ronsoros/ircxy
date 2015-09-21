# ircxy
IRCXY IRCv4 Implementation

## starting the server

java TES -p=port

port: port to listen on

configure it by editing ircxy.conf, or start it up and
do: /oper user pass
then using the (beta testing, experimental) CONF command,
add an O-line: /quote CONF add o-line N:myusername:mypassword

## compiling

just do: javac *.java and it is compiled! You need JDK for this.
