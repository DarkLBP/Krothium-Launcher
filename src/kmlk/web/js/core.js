//Core javascript functions for KMLK inner functionality
function authenticate(user, pass){
    var url = "/action/authenticate";
    var parameters = "u=" + user + "&p=" + pass;
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        if (xhr.readyState === XMLHttpRequest.DONE) {
            alert(xhr.responseText);
        }
    }
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.send(parameters);
}