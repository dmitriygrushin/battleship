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
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    let socket = new SockJS('/fallback-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, (frame) => {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe(`/room/${roomId}`, (message) => {
			let parsedMessage = JSON.parse(message.body);
			if (parsedMessage.type == "chat-message") showChatMessage(parsedMessage);
			if (parsedMessage.type == "user-status") showUserStatus(parsedMessage);
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
    stompClient.send(`/battleship/message/${roomId}`, {}, JSON.stringify({'content': $("#message").val()}));
}

function showChatMessage(message) {
    $("#greetings").append("<tr><td>" + message.content + "</td></tr>");
}

function showUserStatus(message) {
    $("#greetings").append("<tr><td>" + message.content + ": status" + "</td></tr>");
}

$(() => {
    $("form").on('submit', (e) => {
        e.preventDefault();
    });
    $( "#connect" ).click(() => { connect(); });
    $( "#disconnect" ).click(() => { disconnect(); });
    $( "#send" ).click(() => { sendName(); });
});