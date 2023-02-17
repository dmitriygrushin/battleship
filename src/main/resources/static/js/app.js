let stompClient = null;
let shipCount = 0; 
const maxShipCount = 3;
let hasOpponent = false;
let roomIsReady = false;
let isReady = false;
let isGameFinished = false;
let sound = false;

// Board: |0 - ocean|, |1 - hit|, |2 - miss|,  |3 - ship|

// 10x10 arrays
// You don't get the actual opponents board. This is more of a reference board
const opponentBoard = Array.from({ length: 10 }, () => Array(10).fill(0)); 
const myBoard = Array.from({ length: 10 }, () => Array(10).fill(0)); 

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
		$("#p-vs-p").text("Waiting for opponent...");
    }
    $("#messages").html("");
}

function connect() {
    let socket = new SockJS('/fallback-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, (frame) => {
        setConnected(true);
        console.log('Connected: ' + frame);
        
        drawBoard("prep-board", myBoard);
        
        stompClient.subscribe(`/user/topic/${roomId}`, async (message) => {
			let parsedMessage = JSON.parse(message.body);

			switch(parsedMessage.type) {
				case "user-status-alert":
					$("#messages").append("<tr><td>" + parsedMessage.content + ": status" + "</td></tr>"); // show user status
					break;
			  	case "ready-room-battle":
					$("#whose-turn").prop("disabled", false); 
					isYourTurn = true;
					break;
			  	case "battle-coordinates":
					handleBattleCoordinates(parsedMessage.content);
					drawBoard("opponent-board", opponentBoard);
					drawBoard("my-board", myBoard);
					break;
			  	case "battle-coordinates-hit":
					updateOpponentBoard(parsedMessage.content, 1);
					console.log(`${parsedMessage.content} was a HIT`);
					drawBoard("opponent-board", opponentBoard);
					drawBoard("my-board", myBoard);
					playSound(0);
					await new Promise(resolve => setTimeout(resolve, 1000)); 
					playSound(1);
					break;
			  	case "battle-coordinates-miss":
					updateOpponentBoard(parsedMessage.content, 2);
					console.log(`${parsedMessage.content} was a MISS`);
					drawBoard("opponent-board", opponentBoard);
					drawBoard("my-board", myBoard);
					playSound(0);
					await new Promise(resolve => setTimeout(resolve, 1000)); 
					playSound(2);
					break;
			  	case "battle-finish":
					win();
					break;
				}
        });
		
        stompClient.subscribe(`/topic/${roomId}`, (message) => {
			let parsedMessage = JSON.parse(message.body);

			switch(parsedMessage.type) {
				case "chat-message":
					$("#messages").append("<tr><td>" + parsedMessage.content + "</td></tr>"); // show user message
					break;
			  	case "user-status-alert":
			  	case "user-status-connect":
					$("#messages").append("<tr><td>" + parsedMessage.content + ": status" + "</td></tr>"); // show user status
					break;
			  	case "usernames":
					addOpponentUsername(parsedMessage.content);
					break;
			  	case "user-status-disconnect":
					$("#messages").append("<tr><td>" + parsedMessage.content + ": status" + "</td></tr>"); // show user status
					if (roomIsReady) win(); // opponent left mid game
					$("#p-vs-p").text("Waiting for opponent...");
					break;
			  	case "ready-room-success":
					$('#game-board-prep-board').remove();
					drawBoard("opponent-board", opponentBoard);
					drawBoard("my-board", myBoard);
					roomIsReady = true;
					break;
				}
        });
    });
}

function win() {
  	isGameFinished = true;
	alert("You won!");
  	setTimeout(() => { location.reload(); }, 5000);
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
	location.reload();
}

// from list of usernames find and add the opponent's username and enable ready button
function addOpponentUsername(usernames) {
	for (const username of usernames) {
		console.log("usernames: " + username);
		if (myUsername != username) {
			$("#p-vs-p").text(`Opponent: ${username}`);
			if (shipCount == maxShipCount && !isReady) $("#ready-button").prop("disabled", false); 
			hasOpponent = true;
			return;	
		}
	}
}

function broadcastCoordinates(coordinates) {
	const {row, col} = convertCoordinates(coordinates);
	
	if(isGameFinished){
		alert("Game is finished");
	} else if (isYourTurn && opponentBoard[row - 1][col - 1] == 0) { 
		console.log("coordinates send: " + coordinates);
		stompClient.send(`/app/coordinates/${roomId}`, {}, JSON.stringify({'content': coordinates}));
		$("#whose-turn").prop("disabled", true); 
		isYourTurn = false;
	} else if (isYourTurn && opponentBoard[row - 1][col - 1] != 0) {
		alert("You've already shot that!");
	} else {
		alert("It's not your turn!");
	}
}

function setUpMyCoordinates(coordinates) {
	if (shipCount < maxShipCount) {
		const {row, col} = convertCoordinates(coordinates);

		// check if a ship is not in those coordinates already
		if (myBoard[row - 1][col - 1] == 0) {
			myBoard[row - 1][col - 1] = 3;
		
			drawBoard("prep-board", myBoard);
			++shipCount;
		}	
	} else {
		alert("That's enough ships!");
	}

	if (shipCount == maxShipCount && hasOpponent && !isReady) $("#ready-button").prop("disabled", false);
}

function drawBoard(name, array) {
	let gameBoard = document.getElementById(`game-board-${name}`);
	gameBoard.innerHTML = "";

	let h1 = document.createElement("h1");
	h1.textContent = name;

	gameBoard.appendChild(h1);

	// set up letter coordinates A - J
	let row = document.createElement("div");
	row.classList.add("row", "w-75");
	for (let j = 0; j <= 10; j++) {
		row.insertAdjacentHTML('beforeend', `<div class="col-1 border border-primary">${String.fromCharCode(j + 64)}</div>`);
	}	
	gameBoard.appendChild(row);
	
	for (let i = 1; i <= 10; i++) {
		let row = document.createElement("div");
		row.classList.add("row", "w-75");
		for (let j = 0; j <= 10; j++) {
			let col = document.createElement("div");
			col.classList.add("col-1", "border", "border-primary");
			col.textContent = i;
			if (j != 0) {
				col.classList.add(`${name}-coords`);
				if (array[i - 1][j - 1] == 1) {
					col.textContent = "💥";
				} else if (array[i - 1][j - 1] == 2) {
					col.textContent = "❌";
				} else if (array[i - 1][j - 1] == 3) {
					col.textContent = "🚢";
				} else {
					col.textContent = "🌊";
				}
				col.id = `${i},${String.fromCharCode(j + 64)}`;
			}
			row.appendChild(col);
		}	
		gameBoard.appendChild(row);
	}
	assignBoardOnClickType(name);
}

// depending on the board name different onClick methods will be assigned
function assignBoardOnClickType(name) {
	const collections = document.getElementsByClassName(`${name}-coords`);
	for (const element of collections) {
		element.addEventListener("click", () => {
			if (name == "opponent-board") broadcastCoordinates(element.getAttribute("id"))
			if (name == "prep-board") setUpMyCoordinates(element.getAttribute("id"))
		});
	}
}


// Game Loop - #4
function handleBattleCoordinates(coordinates) {
	const {row, col} = convertCoordinates(coordinates);
	
	// broadcast a hit(1)/miss(2)
	if (myBoard[row - 1][col - 1] == 3) {
		stompClient.send(`/app/hit/${roomId}`, {}, JSON.stringify({'content': coordinates}));
		$("#whose-turn").prop("disabled", false); 
		isYourTurn = true;
		myBoard[row - 1][col - 1] = 1;

		--shipCount;
		if (shipCount < 1) {
			stompClient.send(`/app/finish/${roomId}`, {}, JSON.stringify({'content': coordinates}));
			isGameFinished = true;
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

function updateOpponentBoard(coordinates, hitOrMiss) {
	const {row, col} = convertCoordinates(coordinates);
	opponentBoard[row - 1][col - 1] = hitOrMiss;
}

function convertCoordinates(coordinates) {
	const row = Number(coordinates.split(",")[0]);
	const col = Number(coordinates.split(",")[1].charCodeAt(0) - 64); // change from letter to number
	return {row, col};
}

function playSound(type) {
	if (sound) {
		if (type == 0) 	new Audio('/audio/nyoom.mp3').play()
		if (type == 1) 	new Audio('/audio/hit.mp3').play()
		if (type == 2) 	new Audio('/audio/miss.mp3').play()
	}
}

$(() => {
    $("form").on('submit', (e) => { e.preventDefault(); });
    $("#connect").click(() => { connect(); });
    $("#disconnect").click(() => { disconnect(); });
    $("#send").click(() => { 
		stompClient.send(`/app/message/${roomId}`, {}, JSON.stringify({'content': $("#message").val()}));
	});
    $("#ready-button").click(() => { 
		stompClient.send(`/app/ready/${roomId}`, {}, JSON.stringify({})); // send ready signal
		$("#ready-button").prop("disabled", true); 
		isReady = true;
	});
	$("#copy-room-link-btn").click((e) => { 
		e.preventDefault();
		navigator.clipboard.writeText(`${window.location.href}`);
	});
    $("#sound-btn").click((e) => { 
		sound = !sound;	
		sound ? $(e.target).text("SOUND Effects [ON]") : $(e.target).text("SOUND Effects [OFF]");
	});
});