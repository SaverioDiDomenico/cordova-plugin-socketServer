# create a tcp server on android
应用场景：物联网设备，安卓系统上位机与下位机通讯，无显示器的板子通过热点配网等<br/>
已实现功能：tcp server，<br/>
支持事件：onconnect，ondata，onclose，<br/>
支持方法：write(socketId,data)<br/>

近期准备实现的功能：<br/>
1、tcpclient <br/>
2、（传感器）串口 read write<br/>
 <br/>
日后计划实现的功能：<br/>
5、（传感器）串口转tcp server<br/>
6、（传感器）串口映射（虚拟串口）<br/>
7、（传感器）串口跨内网映射（虚拟串口）<br/>

只有一个.java文件文件和一个.js文件，不依赖其他第三方模块，修改维护简单。

#安装方法: 

``````
cordova plugin add https://github.com/huge818/socketServer.git
````````

#使用示例
```````````
document.addEventListener("deviceready", function(){
	var socketServer=cordova.plugins.socketServer;
	socketServer.startServer(8080,function(data){
		if(data.type=="data"){
			var socketId=data.socketId;
			console.log("length: "+data.length);
			console.log("socketId: "+data.socketId);
			console.log("HostAddress: "+data.HostAddress);
			console.log("HostName: "+data.HostName);
			socketServer.write(socketId,"server reply: ok:"+ data.buffer); //base64
		}
		else if(data.type=="connect"){
			console.log("connect");
			console.log("socketId: "+data.socketId);
		}
		else if(data.type=="close"){
			console.log("close");
			console.log("socketId: "+data.socketId);
		}
	  	var str=JSON.stringify(data);
	  	console.log(str);
	},function(){
		console.log("error");
	});

}, false);

```````````
<br/>
如果您喜欢nodejs格式的事件写法，可以自行在业务层封装。<br/>

如果想使用websocket，请使用另外一个项目 https://github.com/huge818/cordova-plugin-websocke

#API文档
接口很少，见示例


