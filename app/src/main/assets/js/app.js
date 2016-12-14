window.onload = function() {
    var screen=document.getElementById('screen');
    var ws_url=location.href.replace('http://', 'ws://');
    var ws_new_socket_url = ws_url.replace('8080', '6060');

    var socket=new WebSocket(ws_new_socket_url);

    console.log("ws_url: " + ws_url);
    console.log("ws_new_socket_url: " + ws_new_socket_url);
    console.log("socket: " + socket);


    socket.onopen = function(event) {
        console.log('onOpen');
      // console.log(event.currentTarget.url);
    };

    socket.onerror = function(error) {
      console.log('WebSocket error: ' + error);
    };

    socket.onmessage = function(event) {
        console.log('onMessage');
        console.log('onMessage: ' + event.data);
//      screen.src=event.data;
    };
}
