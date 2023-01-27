let stompClient = null;
let roomId = (() => {
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	return urlParams.get('roomId');
})();


if (!roomId) alert("You don't have a room number!");

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
        
        
        stompClient.subscribe(`/user/topic/${roomId}`, (message) => {
			let parsedMessage = JSON.parse(message.body);
			if (parsedMessage.type == "user-status-alert") showUserStatus(parsedMessage);
        });

        stompClient.subscribe(`/topic/${roomId}`, (message) => {
			let parsedMessage = JSON.parse(message.body);
			if (parsedMessage.type == "chat-message") showChatMessage(parsedMessage);
			
			if (["user-status-alert", "user-status-connect", "user-status-disconnect"].includes(parsedMessage.type)) {
				showUserStatus(parsedMessage);
			}
			if (parsedMessage.type == "usernames") addOpponentUsername(parsedMessage);
			if (parsedMessage.type == "user-status-disconnect") removeOpponentUsername();
        });

        
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
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

// from list of usernames find and add the opponent's username
function addOpponentUsername(usernames) {
	for (const username of usernames.content) {
		console.log("usernames: " + username);
		if (myUsername != username) {
			document.getElementById("p-vs-p").innerHTML = `Opponent: ${username}`;	
			return;	
		}
	}

}

function removeOpponentUsername() {
	document.getElementById("p-vs-p").innerHTML = `Waiting for opponent...`;	
}

$(() => {
    $("form").on('submit', (e) => {
        e.preventDefault();
    });
    $("#connect").click(() => { connect(); });
    $("#disconnect").click(() => { disconnect(); });
    $("#send").click(() => { sendName(); });
});