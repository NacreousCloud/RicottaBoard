// var nodeStatic = require('node-static')
const app = require('express')();
const https = require('https');
const fs = require('fs');
const http = require('http');

const options = {
    key: fs.readFileSync('/etc/letsencrypt/live/k3a204.p.ssafy.io/privkey.pem'),
    cert: fs.readFileSync('/etc/letsencrypt/live/k3a204.p.ssafy.io/cert.pem'),
    ca: fs.readFileSync('/etc/letsencrypt/live/k3a204.p.ssafy.io/chain.pem'),
    requestCert: false,
    rejectUnauthorized: false
};

var server = https.createServer(options, app);
// var server = http.createServer(app);

var io = require('socket.io')(server);

// io.set('transports', ['websocket']);

app.all('/*', function(req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "X-Requested-With");
    next();
});

// app.get('/', function(req, res) {
// console.log("in socket");
// res.sendFile('Hellow Chating App Server');
// });



console.log("rtc server socket on");
// io.sockets.on('connection', function(socket) {
io.on('connection', function(socket) {
    socket.on('add candidate', function(connect) {
        var channel = connect.channel;

        io.sockets.in(channel).emit('candidate', connect);
    });

    socket.on('offer connect', function(connect) {
        var channel = connect.channel;

        io.sockets.in(channel).emit('sender info', connect);
    });

    socket.on('answer connect', function(connect) {
        var channel = connect.channel;

        io.sockets.in(channel).emit('receiver info', connect);
    });

    socket.on('join channel', function(channel) {
        socket.join(channel);

        // var clientsInRoom = io.sockets.adapter.rooms[channel];
        // var numClients = clientsInRoom ? Object.keys(clientsInRoom.sockets).length : 0;

        // io.sockets.in(channel).emit('new member', numClients);
        io.sockets.in(channel).emit('new member');
    });

    socket.on('new member', channel => {
        io.sockets.in(channel).emit('alert');
    });

    socket.on('alert member', info => {
        io.sockets.in(info.channel).emit('member', info.member);
    });

    socket.broadcast.emit('test', "connection success!");

});

server.listen(3031, function() {
    console.log("server listening on port 3031");
});