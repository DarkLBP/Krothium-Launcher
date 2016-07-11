//Core javascript functions for KMLK inner functionality

function authenticate(user, pass){
    var parameters = "u=" + user + "&p=" + pass;
    postRequest("authenticate", parameters);
}
function play(){
    postRequest("play", "");
}
function postRequest(action, parameters){
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            switch (action){
                case "authenticate":
                    if (xhr.responseText === "OK"){
                        document.location.href("/");
                    }else{
                        alert(xhr.responseText);
                    }
                    break;
            }
        }
    };
    xhr.open("POST", "/action/" + action, true);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.send(parameters);
}