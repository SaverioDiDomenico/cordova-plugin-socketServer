package socketServer;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
// Cordova
import android.util.Log;
import java.util.UUID;
import java.util.HashMap;

public class socketServer extends CordovaPlugin {
	ServerSocket myServerSocket;
	boolean ServerActivated = false;
	HashMap<String, Socket> socketHashMap;

	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		try {
			if ("startServer".equals(action)) {
				final int port = args.getInt(0);  
				if(ServerActivated){
					callbackContext.error("has been startServer");
					return false;
				}
				this.startServer(port,callbackContext);
				return true;
			} else if("write".equals(action)){
				final String uuid = args.getString(0); 
				String data = args.getString(1);
				if(socketHashMap.containsKey(uuid)){
					Socket socket=socketHashMap.get(uuid);
					OutputStream output=socket.getOutputStream();
					output.write(data.getBytes());
					return true;
				} else {
					return false;
				}
				
			}
 			else if("disconnect".equals(action)){
				final String uuid = args.getString(0); 
				String data = args.getString(1);
				if(socketHashMap.containsKey(uuid)){
					socketHashMap.get(uuid).close();
					return true;
				} else {
					return false;
				}
			}
 			else if("close".equals(action)){
				myServerSocket.close();
				return true;
			}
			else {
				callbackContext.error(action + "not supported");
				return false;
			}
		} catch (Exception e) {
			callbackContext.error(e.getMessage());
			return false;
		}
	}

	public void startServer(final int port, final CallbackContext callbackContext){
		socketHashMap = new HashMap<String, Socket>();
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try{ 
					myServerSocket = new ServerSocket(port); 
					ServerActivated = true;
					while(true) {
						try{ 
							Socket clientSocket = myServerSocket.accept();
							String uuid = UUID.randomUUID().toString();
							socketHashMap.put(uuid,clientSocket);
							ClientServiceThread ClientThread = new ClientServiceThread(uuid, clientSocket, callbackContext);
							ClientThread.start();
						} catch(IOException ioe) {
							ioe.printStackTrace();
							callbackContext.error(ioe.getMessage());
						}
					}
				} catch(IOException ioe) {
					callbackContext.error(ioe.getMessage());
					ioe.printStackTrace();
					//System.exit(-1); 
				}
			}
		});
	}

    public void sendPluginResult(CallbackContext callbackContext,JSONObject obj){
		PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
		pluginResult.setKeepCallback(true);
		callbackContext.sendPluginResult(pluginResult);
    }
	
	class ClientServiceThread extends Thread {
		Socket myClientSocket;
		boolean m_bRunThread = true; 
		CallbackContext myCallbackContext;
		String myuuid;

		public ClientServiceThread() {
			super();
		}

		ClientServiceThread(String uuid, Socket socket, CallbackContext callbackContext) {
			myClientSocket = socket; 
			myCallbackContext=callbackContext;
			myuuid=uuid;
		}

		public void run() {
			try{
				InputStream input=myClientSocket.getInputStream();
				OutputStream output = myClientSocket.getOutputStream();
				byte buffer[] = new byte[1024 * 4];
				int count=0;
				JSONObject eventObject = new JSONObject();
				eventObject.put("type", "connect");
				eventObject.put("socketId", myuuid);
				socketServer.this.sendPluginResult(myCallbackContext,eventObject);
				int len =-1;
				while((len = input.read(buffer))!= -1) {
					JSONObject dataObject = new JSONObject();
					dataObject.put("type", "data");
					dataObject.put("buffer", buffer);
					dataObject.put("socketId", myuuid);
					dataObject.put("HostName", myClientSocket.getInetAddress().getHostName());
					dataObject.put("HostAddress", myClientSocket.getInetAddress().getHostAddress());
					//PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, buffer);
					socketServer.this.sendPluginResult(myCallbackContext,dataObject);
				}

				JSONObject closeObject = new JSONObject();
				closeObject.put("type", "close");
				closeObject.put("socketId", myuuid);
				socketServer.this.sendPluginResult(myCallbackContext,closeObject);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			finally{
				try{
					//input.close(); 
					myClientSocket.close();
				} 
				catch(IOException ioe) {
					ioe.printStackTrace();
				} 
			} 
		} 
	}   

}