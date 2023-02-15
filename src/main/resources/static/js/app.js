let stompClient = null;
let shipCount = 0; 
const maxShipCount = 3;
let hasOpponent = false;


// Board: |0 - ocean|, |1 - hit|, |2 - miss|,  |3 - ship|

// You don't get the actual opponents board. This is more of a reference board
let opponentBoard = 
//cols           A,B,C,D,E,F,G,H,I,J    
			[	[0,0,0,0,0,0,0,0,0,0],  // 1 rows
				[0,0,0,0,0,0,0,0,0,0],  // 2 
				[0,0,0,0,0,0,0,0,0,0],  // 3 
				[0,0,0,0,0,0,0,0,0,0],  // 4 
				[0,0,0,0,0,0,0,0,0,0],  // 5 
				[0,0,0,0,0,0,0,0,0,0],  // 6 
				[0,0,0,0,0,0,0,0,0,0],  // 7 
				[0,0,0,0,0,0,0,0,0,0],  // 8  
				[0,0,0,0,0,0,0,0,0,0],  // 9  
				[0,0,0,0,0,0,0,0,0,0]]; // 10 


let myBoard = 
//cols           A,B,C,D,E,F,G,H,I,J    row:
			[	[0,0,0,0,0,0,0,0,0,0],  // 1 
				[0,0,0,0,0,0,0,0,0,0],  // 2 
				[0,0,0,0,0,0,0,0,0,0],  // 3 
				[0,0,0,0,0,0,0,0,0,0],  // 4 
				[0,0,0,0,0,0,0,0,0,0],  // 5 
				[0,0,0,0,0,0,0,0,0,0],  // 6 
				[0,0,0,0,0,0,0,0,0,0],  // 7 
				[0,0,0,0,0,0,0,0,0,0],  // 8  
				[0,0,0,0,0,0,0,0,0,0],  // 9  
				[0,0,0,0,0,0,0,0,0,0]]; // 10 

let roomId = (() => {
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	return urlParams.get('roomId');
})();

if (!roomId) alert("You don't have a room number!");

$("#ready-button").prop("disabled", true); 
$("#whose-turn").prop("disabled", true); 
let isYourTurn = false;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    } else {
        $("#conversation").hide();
        removeOpponentUsername();
    }
    $("#messages").html("");
}

function connect() {
    let socket = new SockJS('/fallback-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, (frame) => {
        setConnected(true);
        console.log('Connected: ' + frame);
        
        drawPrepBoard();
        
        stompClient.subscribe(`/user/topic/${roomId}`, (message) => {
			let parsedMessage = JSON.parse(message.body);
			if (parsedMessage.type == "user-status-alert") showUserStatus(parsedMessage);
			if (parsedMessage.type == "ready-success") console.log("YOU ARE READY FOR IT");

			// Game Loop - #2
			if (parsedMessage.type == "ready-room-battle") {
				$("#whose-turn").prop("disabled", false); 
				isYourTurn = true;
			}
			
			// Game Loop - #4
			if (parsedMessage.type == "battle-coordinates") {
				// handle coordinates and say hit or miss
				handleBattleCoordinates(parsedMessage.content);
				drawBoard("opponent-board", opponentBoard);
				drawBoard("my-board", myBoard);
			}
			
			// Game Loop - #6 - Almost end. End is when 1 user's ships are all gone
			if (parsedMessage.type == "battle-coordinates-hit") {
				updateOpponentBoard(parsedMessage.content, 1);
				console.log(`${parsedMessage.content} was a HIT`);
				drawBoard("opponent-board", opponentBoard);
				drawBoard("my-board", myBoard);
			}
			
			if (parsedMessage.type == "battle-coordinates-miss") {
				updateOpponentBoard(parsedMessage.content, 2);
				console.log(`${parsedMessage.content} was a MISS`);
				drawBoard("opponent-board", opponentBoard);
				drawBoard("my-board", myBoard);
			}
			
			// Game Loop - END
			if (parsedMessage.type == "battle-finish") {
				alert("You won!");
			  	setTimeout(() => { location.reload(); }, 5000);
			}
			
			
        });
		
        stompClient.subscribe(`/topic/${roomId}`, (message) => {
			let parsedMessage = JSON.parse(message.body);
			
			// TODO: Refactor to use switch later

			if (parsedMessage.type == "chat-message") showChatMessage(parsedMessage);
			
			if (["user-status-alert", "user-status-connect", "user-status-disconnect"].includes(parsedMessage.type)) {
				showUserStatus(parsedMessage);
			}
			if (parsedMessage.type == "usernames") addOpponentUsername(parsedMessage.content);

			if (parsedMessage.type == "user-status-disconnect") removeOpponentUsername();

			
			// Game Loop - #0 - Room is ready
			if (parsedMessage.type == "ready-room-success") {
				$('#game-board-prep-board').remove();
				drawBoard("opponent-board", opponentBoard);
				drawBoard("my-board", myBoard);
			}
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
	location.reload();
}

function sendName() {
    stompClient.send(`/app/message/${roomId}`, {}, JSON.stringify({'content': $("#message").val()}));
}

function showChatMessage(message) {
    $("#messages").append("<tr><td>" + message.content + "</td></tr>");
}

function showUserStatus(message) {
    $("#messages").append("<tr><td>" + message.content + ": status" + "</td></tr>");
}

// from list of usernames find and add the opponent's username and enable ready button
function addOpponentUsername(usernames) {
	for (const username of usernames) {
		console.log("usernames: " + username);
		if (myUsername != username) {
			document.getElementById("p-vs-p").innerHTML = `Opponent: ${username}`;	
			if (shipCount == maxShipCount) $("#ready-button").prop("disabled", false); 
			hasOpponent = true;
			return;	
		}
	}
}

function removeOpponentUsername() {
	document.getElementById("p-vs-p").innerHTML = `Waiting for opponent...`;	
}

function sendReadySignal() {
    stompClient.send(`/app/ready/${roomId}`, {}, JSON.stringify({}));
}

function broadcastCoordinates(coordinates) {
	if (isYourTurn) {
		console.log("coordinates send: " + coordinates);
		stompClient.send(`/app/coordinates/${roomId}`, {}, JSON.stringify({'content': coordinates}));
		$("#whose-turn").prop("disabled", true); 
		isYourTurn = false;
	} else {
		alert("ITS NOT YOUR TURN YET!");
	}
}

function setUpMyCoordinates(coordinates) {
	if (shipCount < maxShipCount) {
		const row = Number(coordinates.split(",")[0]);
		const col = Number(coordinates.split(",")[1].charCodeAt(0) - 64); // change from letter to number

		// check if a ship is not in those coordinates already
		if (myBoard[row - 1][col - 1] == 0) {
			myBoard[row - 1][col - 1] = 3;
		
			drawPrepBoard();
			++shipCount;
		}	
	} else {
		alert("That's enough ships!");
	}

	if (shipCount == maxShipCount && hasOpponent){
		$("#ready-button").prop("disabled", false);
	}
}

function drawPrepBoard() {
	let name = "prep-board";
	let array = myBoard;
	$(`#game-board-${name}`).empty();
	let gameBoard = document.getElementById(`game-board-${name}`);
	let h1 = document.createElement("h1");
	h1.innerHTML = name;
	gameBoard.appendChild(h1);

	// set up letter coordinates A - J
	let row = document.createElement("div");
	row.classList.add("row");
	row.classList.add("w-75");
	for (let j = 0; j <= 10; j++) {
		let col = document.createElement("div");
		col.classList.add("col-1");
		col.classList.add("border");
		col.classList.add("border-primary");
		col.innerHTML = `${String.fromCharCode(j + 64)}`;
		if (j == 0) col.innerHTML = "";
		row.appendChild(col);
	}	
	gameBoard.appendChild(row);
	
	for (let i = 1; i <= 10; i++) {
		let row = document.createElement("div");
		row.classList.add("row");
		row.classList.add("w-75");
		for (let j = 0; j <= 10; j++) {
			let col = document.createElement("div");
			col.classList.add("col-1");
			col.classList.add("border");
			col.classList.add("border-primary");
			col.innerHTML = `${i}`;
			if (j != 0) {
				col.classList.add(`${name}-coords`);
				if (array[i - 1][j - 1] == 1) {
					col.innerHTML = "💥";
				} else if (array[i - 1][j - 1] == 2) {
					col.innerHTML = '❌';
				} else if (array[i - 1][j - 1] == 3) {
					col.innerHTML = '🚢';
				} else {
					col.innerHTML = '🌊';
				}
				col.setAttribute('id', `${i},${String.fromCharCode(j + 64)}`)
			}
			
			row.appendChild(col);
		}	
		gameBoard.appendChild(row);
	}
	
	isAllowedToClickBoard(name);
}

function drawBoard(name, array) {
	$(`#game-board-${name}`).empty();
	let gameBoard = document.getElementById(`game-board-${name}`);
	let h1 = document.createElement("h1");
	h1.innerHTML = name;
	gameBoard.appendChild(h1);

	// set up letter coordinates A - J
	let row = document.createElement("div");
	row.classList.add("row");
	row.classList.add("w-75");
	for (let j = 0; j <= 10; j++) {
		let col = document.createElement("div");
		col.classList.add("col-1");
		col.classList.add("border");
		col.classList.add("border-primary");
		col.innerHTML = `${String.fromCharCode(j + 64)}`;
		if (j == 0) col.innerHTML = "";
		row.appendChild(col);
	}	
	gameBoard.appendChild(row);
	
	for (let i = 1; i <= 10; i++) {
		let row = document.createElement("div");
		row.classList.add("row");
		row.classList.add("w-75");
		for (let j = 0; j <= 10; j++) {
			let col = document.createElement("div");
			col.classList.add("col-1");
			col.classList.add("border");
			col.classList.add("border-primary");
			col.innerHTML = `${i}`;
			if (j != 0) {
				col.classList.add(`${name}-coords`);
				if (array[i - 1][j - 1] == 1) {
					col.innerHTML = "💥";
				} else if (array[i - 1][j - 1] == 2) {
					col.innerHTML = '❌';
				} else if (array[i - 1][j - 1] == 3) {
					col.innerHTML = '🚢';
				} else {
					col.innerHTML = '🌊';
				}
				col.setAttribute('id', `${i},${String.fromCharCode(j + 64)}`)
			}
			
			row.appendChild(col);
		}	
		gameBoard.appendChild(row);
	}
	
	isAllowedToClickBoard(name);
	
}

function isAllowedToClickBoard(name) {
	if (name == "opponent-board") {
		const collections = document.getElementsByClassName("opponent-board-coords");
		for (const element of collections) {
			element.addEventListener("click", () => {
				broadcastCoordinates(element.getAttribute("id"))
			});
		}
	}
	
	if (name == "prep-board") {
		const collections = document.getElementsByClassName("prep-board-coords");
		for (const element of collections) {
			element.addEventListener("click", () => {
				setUpMyCoordinates(element.getAttribute("id"))
			});
		}
	}
}


// Game Loop - #4
function handleBattleCoordinates(coordinates) {
	console.log(`handles coordinates ${coordinates}`);
	const row = Number(coordinates.split(",")[0]);
	const col = Number(coordinates.split(",")[1].charCodeAt(0) - 64); // change from letter to number
	
	console.log(`row: ${row}, col: ${col}`);
	
	// broadcasta hit(1)/miss(2)
	if (myBoard[row - 1][col - 1] == 3) {
		stompClient.send(`/app/hit/${roomId}`, {}, JSON.stringify({'content': coordinates}));
		$("#whose-turn").prop("disabled", false); 
		isYourTurn = true;
		myBoard[row - 1][col - 1] = 1;

		--shipCount;
		if (shipCount < 1) {
			stompClient.send(`/app/finish/${roomId}`, {}, JSON.stringify({'content': coordinates}));
			alert("You lost");
		  	setTimeout(() => { location.reload(); }, 5000);
		}
	} else {
		stompClient.send(`/app/miss/${roomId}`, {}, JSON.stringify({'content': coordinates}));
		$("#whose-turn").prop("disabled", false); 
		isYourTurn = true;
		myBoard[row - 1][col - 1] = 2;
	}
}

function updateOpponentBoard(coordinates, hitMiss) {
	// change your opponent board
	const row = Number(coordinates.split(",")[0]);
	const col = Number(coordinates.split(",")[1].charCodeAt(0) - 64); // change from letter to number
	
	opponentBoard[row - 1][col - 1] = hitMiss;
}

$(() => {
    $("form").on('submit', (e) => {
        e.preventDefault();
    });
    $("#connect").click(() => { connect(); });
    $("#disconnect").click(() => { disconnect(); });
    $("#send").click(() => { sendName(); });
    $("#ready-button").click(() => { sendReadySignal(); });
});