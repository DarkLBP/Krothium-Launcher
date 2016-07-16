//Core javascript functions for KMLK inner functionality
var play_interval = null;
var progress_value = 0;
var play_value = "";
var keepAlive_interval = setInterval(function(){keepAlive();}, 2000);
function authenticate(user, pass){
    var parameters = "u=" + user + "&p=" + pass;
    var response = postRequest("authenticate", parameters);
    if (response === "OK"){
        redirect("/play.html");
    } else {
        alert(response);
    }
}
function playGame(){
    play_interval = setInterval(function(){playGame_Update();}, 1000);
    postRequest("play", null);
}
function playGame_Update(){
    var response = postRequest("status", "");
    var data = response.split("\n");
    if (data.constructor === Array){
        if (data.length === 2){
            var status = data[0];
            var progress = data[1];
            if (progress !== progress_value){
                updateElement("progress", progress);
                progress_value = progress;
            }
            if (status !== play_value){
                switch (status){
                    case "0":
                        updateElement("play", "PLAY");
                        clearInterval(play_interval);
                        break;
                    case "1":
                        updateElement("play", "DOWNLOADING");
                        break;
                    case "2":
                        updateElement("play", "PLAYING");
                        break;
                }
                play_value = status;
            }
        }
    }
}
function redirect(url){
    window.location.href = url;
}
function updateElement(element, value){
    document.getElementById(element).value = value;
}
function shutdown(){
    postRequest("close", null);
}
function keepAlive(){
    postRequest("keepalive", null);
}
function postRequest(action, parameters){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/action/" + action, false);
    xhr.send(parameters);
    return xhr.responseText;
}