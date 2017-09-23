package socketServer;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

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

/*
import java.util.ArrayList;
import java.net.URLEncoder;  

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
*/


public class socketServer extends CordovaPlugin {
	private String OutString;
	ServerSocket myServerSocket;
	boolean ServerOn = true;
	boolean ServerActivated = false;
	
	@Override
	public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
		try {
			if ("startServer".equals(action)) {
				//final int port = Integer.valueOf(args.getString(0));  
				final int port = args.getInt(0);  
				if(ServerActivated){
					callbackContext.error("has been startServer");
					return false;
				}
				cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						try{ 
							myServerSocket = new ServerSocket(port); 
							ServerActivated = true;
						} catch(IOException ioe) { 
							System.exit(-1); 
						}

						while(ServerOn) {
							try{ 
								Socket clientSocket = myServerSocket.accept();
								ClientServiceThread ClientThread = new ClientServiceThread(clientSocket, callbackContext);
								ClientThread.start();
							} catch(IOException ioe) {
								ioe.printStackTrace();
							}
						}
						try {
							myServerSocket.close();
						} catch(Exception e) {
							System.exit(-1); 
						}
						//callbackContext.success();
					}
				});
				return true;
			} else {
				callbackContext.error(action + "not supported");
				return false;
			}
		} catch (Exception e) {
			callbackContext.error(e.getMessage());
			return false;
		}
	}
	
	class ClientServiceThread extends Thread { 
		Socket myClientSocket;
		boolean m_bRunThread = true; 
		CallbackContext myCallbackContext;

		public ClientServiceThread() { 
			super(); 
		} 

		ClientServiceThread(Socket socket, CallbackContext callbackContext) {
			myClientSocket = socket; 
			myCallbackContext=callbackContext;
		}

		public void run() {
			try{
				InputStream input=myClientSocket.getInputStream();
				OutputStream output = myClientSocket.getOutputStream();
				byte buffer[] = new byte[1024 * 10];
				int count=0;
				while(m_bRunThread) {
					if(!ServerOn){ 
						m_bRunThread = false;
					}
					else{
						count = input.read(buffer);
						if(count==-1){break;}
						else{
							myCallbackContext.success(buffer);
						}
					}
				}
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