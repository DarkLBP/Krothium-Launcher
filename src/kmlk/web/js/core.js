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
            var status = data[0];
            var progress = data[1];
            document.getElementById("progress").value = progress;
            switch (status){
                case "0":
                    document.getElementById("play").value = "PLAY";
                    break;
                case "1":
                    document.getElementById("play").value = "DOWNLOADING";
                    break;
                case "2":
                    document.getElementById("play").value = "PLAYING";
                    break;
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