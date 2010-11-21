REBOL [Title: "Echo Server" Author: "Jerrysnow"]
;!rebol2
print "A TCP Echo Server by Jerrysnow"
port: make integer! ask "port:"
listen: open/no-wait tcp://:5009

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
			foreach k input [
				append port k
			]
			if not equal? port "" [
				client: make block! [ ]
				append client port
				append/only clients client
			]
		]
	]
]

