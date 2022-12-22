alert("is this thing working?")

let stompClient = null;
let roomId = (() => {
	const queryString = window.location.search;
	const urlParams = new URLSearchParams(queryString);
	return urlParams.get('roomId');
})();


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
            showGreeting(JSON.parse(message.body));
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
    stompClient.send(`/battleship/message/${roomId}`, {}, JSON.stringify({'message': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message.message + "</td></tr>");
    console.log(`message from server: ${message.message}`);
}

$(() => {
    $("form").on('submit', (e) => {
        e.preventDefault();
    });
    $( "#connect" ).click(() => { connect(); });
    $( "#disconnect" ).click(() => { disconnect(); });
    $( "#send" ).click(() => { sendName(); });
});