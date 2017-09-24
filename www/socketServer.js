var exec = require('cordova/exec');

exports.startServer = function(port, success, error) {
    exec(success, error, "socketServer", "startServer", [port]);
};

exports.write = function(socketId, data, success, error) {
    exec(success, error, "socketServer", "write", [socketId,data]);
};
