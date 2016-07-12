//Core javascript functions for KMLK inner functionality
var play_interval;

function authenticate(user, pass){
    var parameters = "u=" + user + "&p=" + pass;
    var response = postRequest("authenticate", parameters);
    if (response === "OK"){
        document.location.href("/");
    } else {
        alert(response);
    }
}
function play(){
    postRequest("play", "");
    play_interval = setInterval(function(){play_update();}, 1000);
}
function play_update(){
    console.info("WORKS");
}
function postRequest(action, parameters){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/action/" + action, false);
    xhr.send(parameters);
    return xhr.responseText;
}