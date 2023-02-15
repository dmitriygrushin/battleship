function makeRoomId(length) {
    let result           = '';
    let characters       = '0123456789';
    let charactersLength = characters.length;
    for (let i = 0; i < length; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

function makeRoom(roomId) {
	console.log("making room");
	window.location.href = `/room?roomId=${roomId}`;		
}


$(() => {
    $("#create-room-btn").click(() => { makeRoom(makeRoomId(8)); });
    $("#home-login").click(() => { 
			
	 });
});
