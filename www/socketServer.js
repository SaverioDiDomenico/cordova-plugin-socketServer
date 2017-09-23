var exec = require('cordova/exec');

exports.startServer = function(arg0, success, error) {
    exec(success, error, "socketServer", "startServer", [arg0]);
};
