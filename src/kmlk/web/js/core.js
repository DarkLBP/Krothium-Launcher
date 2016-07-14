//Core javascript functions for KMLK inner functionality
var play_interval;
var progress_value;
var play_value;
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
    play_interval = setInterval(function(){play_update();}, 1000);
    postRequest("play", null);
}
function play_update(){
    var response = postRequest("status", "");
    var data = response.split("\n");
    if (data.constructor === Array){
        if (data.length === 2){
            var status = data[0];
            var progress = data[1];
            if (progress !== progress_value){
                document.getElementById("progress").value = progress;
                progress_value = progress;
            }
            if (status !== play_value){
                switch (status){
                    case "0":
                        document.getElementById("play").value = "PLAY";
                        clearInterval(play_interval);
                        break;
                    case "1":
                        document.getElementById("play").value = "DOWNLOADING";
                        break;
                    case "2":
                        document.getElementById("play").value = "PLAYING";
                        break;
                }
                play_value = status;
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