#!/usr/local/bin/rebol2

REBOL [Title: "Echo Server" Author: "Jerrysnow"]
print "A TCP Echo Server by Jerrysnow"
port: make integer! ask "port: "
print join "hosting at " port
addr: make url! join "tcp://:" port
listen: open/no-wait addr

clients: make block! [ ]
forever [
	if not-equal? none wait [ listen 0 ] [
		client: make block! [ ]
		append client first listen
		append client none
		append client ""
		append/only clients client
		append first client join "... Connected" newline
	]
	if greater? length? clients 0 [
		client: make block! first clients
		remove head clients
		port: first client
		input: copy port
		if not equal? none input [
			if greater? length? input 0 [
				append port input
				print join "send: " input
			]
			if not equal? port "" [
				client: make block! [ ]
				append client port
				append/only clients client
			]
		]
	]
]

