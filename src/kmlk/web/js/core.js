//Core javascript functions for KMLK inner functionality
var play_interval;
var progress_value;
var play_value;
var last_key;
var mouse_out;
window.onbeforeunload = close;
window.onkeydown = keyreg;
window.onmousemove = mousereg;
window.onmouseout = mousereg;
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
function keyreg(e){
    console.info(e.keyCode);
    last_key = e.keyCode;
}
function mousereg(e){
    if (e.type === "mousemove"){
        mouse_out = false;
    } else {
        if (e.relatedTarget === null){
            mouse_out = true;
        } else {
            mouse_out = false;
        }
    }
}
function close(){
    var shutdown = false;
    if (last_key !== 116 && mouse_out){
        shutdown = true;
    } else if (last_key === 17 || last_key === 18 || last_key === 115 || last_key === 87){
        shutdown = true;
    } else if (last_key === 116){
        shutdown = false;
    } else {
        return "Are you sure you want to quit? Any progress won't be saved!";
    }
    if (shutdown){
        postRequest("close", null);
    }
    return null;
}
function postRequest(action, parameters){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/action/" + action, false);
    xhr.send(parameters);
    return xhr.responseText;
}