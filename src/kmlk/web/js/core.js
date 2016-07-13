//Core javascript functions for KMLK inner functionality
function authenticate(user, pass){
    var parameters = "u=" + user + "&p=" + pass;
    var response = postRequest("authenticate", parameters);
    if (response === "OK"){
        document.location.href = "/";
    } else {
        alert(response);
    }
}
function play(){
    postRequest("play", "");
}
function update(){
    var response = postRequest("status", "");
    var data = response.split("\n");
    if (data.constructor === Array){
        if (data.length === 2){
            var gameStarted = data[0];
            var progress = data[1];
            document.getElementById("progress").value = progress;
            if (gameStarted === "true"){
                document.getElementById("play").value = "PLAYING";
            } else {
                document.getElementById("play").value = "PLAY";
            }
        }
    }
}
function postRequest(action, parameters){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/action/" + action, false);
    xhr.send(parameters);
    return xhr.responseText;
}