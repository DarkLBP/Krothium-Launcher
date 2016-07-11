//Core javascript functions for KMLK inner functionality

function authenticate(user, pass){
    var parameters = "u=" + user + "&p=" + pass;
    postRequest("authenticate", "POST", parameters);
}
function postRequest(action, requestType, parameters){
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
    xhr.open(requestType, "/action/" + action, true);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.send(parameters);
}