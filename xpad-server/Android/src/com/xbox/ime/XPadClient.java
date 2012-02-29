package com.xbox.ime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

import android.util.Log;

public class XPadClient {
	static final String TAG = "XPadClient";
	
	/* Size of the buffer sent by XPad server
	 * BYTE (1 byte - controller #)
	 * WORD wButtons; (2 bytes)
	 * BYTE bLeftTrigger; (1)
	 * BYTE bRightTrigger; (1)
	 * SHORT sThumbLX; (2)
	 * SHORT sThumbLY; (2)
	 * SHORT sThumbRX; (2)
	 * SHORT sThumbRY (2)
	 */
	public static final int XINPUT_BUFFER_SIZE = 13;
	
	/*
	 * XINPUT_GAMEPAD Structure see MSDN
	 * http://msdn.microsoft.com/en-us/library/windows/desktop/microsoft.directx_sdk.reference.xinput_gamepad(v=vs.85).aspx
	 */
	public static final short XINPUT_GAMEPAD_DPAD_UP          = 0x0001;
	public static final short XINPUT_GAMEPAD_DPAD_DOWN        = 0x0002;
	public static final short XINPUT_GAMEPAD_DPAD_LEFT        = 0x0004;
	public static final short XINPUT_GAMEPAD_DPAD_RIGHT       = 0x0008;
	public static final short XINPUT_GAMEPAD_START            = 0x0010;
	public static final short XINPUT_GAMEPAD_BACK             = 0x0020;
	public static final short XINPUT_GAMEPAD_LEFT_THUMB       = 0x0040;
	public static final short XINPUT_GAMEPAD_RIGHT_THUMB      = 0x0080;
	public static final short XINPUT_GAMEPAD_LEFT_SHOULDER    = 0x0100;
	public static final short XINPUT_GAMEPAD_RIGHT_SHOULDER   = 0x0200;
	public static final short XINPUT_GAMEPAD_A                = 0x1000;
	public static final short XINPUT_GAMEPAD_B                = 0x2000;
	public static final short XINPUT_GAMEPAD_X                = 0x4000;
	public static final int XINPUT_GAMEPAD_Y                = 0x8000;

	//
	// Gamepad thresholds
	//
	public static final int XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE  = 7849;
	public static final int XINPUT_GAMEPAD_RIGHT_THUMB_DEADZONE = 8689;
	public static final int XINPUT_GAMEPAD_TRIGGER_THRESHOLD    = 30;
	
	/**
	 * C++ - 
	 *  typedef struct _XINPUT_GAMEPAD {
	 *      WORD wButtons;
	 *      BYTE bLeftTrigger;
	 *      BYTE bRightTrigger;
	 *      SHORT sThumbLX;
	 *      SHORT sThumbLY;
	 *      SHORT sThumbRX;
	 *      SHORT sThumbRY;
	 *  } XINPUT_GAMEPAD, *PXINPUT_GAMEPAD;
	 */
	static class XINPUT_GAMEPAD {
		public byte bController;
		public short wButtons;
		public byte bLeftTrigger;
		public byte bRightTrigger;
		public short sThumbLX;
		public short sThumbLY;
		public short sThumbRX;
		public short sThumbRY;
		@Override
		public String toString() {
			return String.format("BTNS: 0x%X LT:0x%X RT:0x%X LX:%d LY:%d RX:%d RY:%d" 
					,wButtons
					,bLeftTrigger, bRightTrigger
					,sThumbLX, sThumbLY 
					,sThumbRX, sThumbRY);
		}
	}

	/*
	 *  Thumbstick direction vector (LX,LY) and magnitude
	 */
	public static class ThumbStick {
		int idx;
		
		// Normalized values
		float normalizedMagnitude;
		float normalizedLX;
		float normalizedLY;
		@Override
		public String toString() {
			return idx + ": [" + normalizedMagnitude + " (" + normalizedLX + "," + normalizedLY + ")]";
		}
	}
	
	private Socket mSocket;
	private BufferedInputStream bis;
	private BufferedOutputStream bos;
	
	private int mPort = 5555;
	private String mServer;
	private boolean mDone = false;
	private IXPadListener mListener;

	/**
	 * Interface used to send messages to a client
	 */
	public static interface IXPadListener {
		public void onConnected();
		public void onError (String reason);
		//public void onEvent (XINPUT_GAMEPAD pad);
		public void onButtonPressed (XBUTTON button);
		public void onButtonReleased (XBUTTON button);
		public void onThumbstickEvent (ThumbStick direction);
	}
	
	public XPadClient(String server, int port) {
		mServer = server;
		mPort = port;
	}

	public void setListener(IXPadListener l) {
		mListener = l;
	}
	
    public void disconnect() {
		mDone = true;
		try {
			bis.close();
			bos.close();
			mSocket.close();
			mSocket = null;
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
    }
    
    /**
     * Connect using a thread
     */
    public void connectAsync() {
    	new Thread(new Runnable() {
			public void run() {
				connect();
			}
		}).start();
    }
    
    private void connect() {
		if ( mServer == null || mServer.length() < 1) {
			sendError("A Server is required.");
			return;
		}
		Log.d(TAG, "Connecting to " + mServer + ":" + mPort);
	
		try {
			mSocket = new Socket(mServer, mPort);
			
			Log.d(TAG, "Connected.");
			
			sendConnected();
			
			// connected
			mDone = false;
			bis = new BufferedInputStream(mSocket.getInputStream());
			bos = new BufferedOutputStream(mSocket.getOutputStream());

			// msgs are 13 bytes
			byte[] buffer = new byte[XPadClient.XINPUT_BUFFER_SIZE];
			int read;
			XINPUT_GAMEPAD pad;
			
			while (! mDone) {
				read = bis.read(buffer);
				
				if ( read != -1) {
					//dumpBytes("BUF", buffer, read);
					
					pad = unpackBytes(buffer);
					//Log.d(TAG, "XPAD: " + pad);
					
					//sendEvent(pad);
					parsePacket(pad);
				}
			}
			
			Log.d(TAG, "Connect thread done");
			
		} catch (Exception e) {
			sendError("Connection Error: " + e.getMessage());
		}
    }

    private void sendConnected() {
    	if ( mListener != null) mListener.onConnected();
    }
    
    private void sendError(String reason) {
    	if ( mListener != null) mListener.onError(reason);
    }
    
    private void sendButtonPressed(XBUTTON button) {
    	if ( mListener != null) 
    		mListener.onButtonPressed(button);
    }

    private void sendButtonReleased(XBUTTON button) {
    	if ( mListener != null) 
    		mListener.onButtonReleased(button);
    }

    private void sendThumbstick(ThumbStick thumb) {
    	if ( mListener != null) 
    		mListener.onThumbstickEvent(thumb);
    }
    
/*
    private void sendEvent(XINPUT_GAMEPAD pad) {
    	if ( mListener != null) mListener.onEvent(pad);
    }
    */
	/*
	 * Utilities
	 */
	short UNPACK_SHORT(byte hi, byte lo) {
		short n = (short) ((short)((hi & 0xFF) << 8) | (lo & 0xFF));
		//Log.d(TAG, "UNPACK SHORT hi:" + hi + " lo:" + lo + " S:" + n);
		return n;
	}

	/**
	 * Unpack a byte buffer sent by the server. The buffer size is
	 * XINPUT_BUFFER_SIZE (13) as follows:
	 * BYTE (1 byte - controller #)
	 * WORD wButtons; (2 bytes)
	 * BYTE bLeftTrigger; (1)
	 * BYTE bRightTrigger; (1)
	 * SHORT sThumbLX; (2)
	 * SHORT sThumbLY; (2)
	 * SHORT sThumbRX; (2)
	 * SHORT sThumbRY (2)
	 * @param buffer
	 * @return
	 */
	private XINPUT_GAMEPAD unpackBytes(byte[] buffer) {
		if ( buffer.length < XINPUT_BUFFER_SIZE) {
			Log.e(TAG, "Invalid Xinput buffer size " + buffer.length);
			return  null;
		}
		if ( buffer.length > XINPUT_BUFFER_SIZE) {
			Log.w(TAG, "Xinput buffer size NOT 13: " + buffer.length);
		}
		
		XINPUT_GAMEPAD pad = new XINPUT_GAMEPAD();
		pad.bController = buffer[0];
		pad.wButtons = UNPACK_SHORT(buffer[1], buffer[2]); 
		pad.bLeftTrigger = buffer[3];
		pad.bRightTrigger = buffer[4];
		
		// Thumbsticks
		pad.sThumbLX = UNPACK_SHORT(buffer[5], buffer[6]);
		pad.sThumbLY = UNPACK_SHORT(buffer[7], buffer[8]);
		pad.sThumbRX = UNPACK_SHORT(buffer[9], buffer[10]);
		pad.sThumbRY = UNPACK_SHORT(buffer[11], buffer[12]);
		
		//Log.d(TAG, "Xpad: LX=" + pad.sThumbLX + " LY=" + pad.sThumbLY 
		//		+ " RX=" + pad.sThumbRX + " RY=" + pad.sThumbRY);
		return pad;
	}
	
	private void parsePacket (XINPUT_GAMEPAD pad) {
		parseButtons(pad.wButtons);
		
		// parse thumb sticks
		ThumbStick thumbL = getThumbStick(0, pad.sThumbLX, pad.sThumbLY);
		if ( thumbL.normalizedMagnitude > 0) {
			sendThumbstick(thumbL);
		}
		
		ThumbStick thumbR = getThumbStick(1, pad.sThumbRX, pad.sThumbRY);
		if ( thumbR.normalizedMagnitude > 0) {
			sendThumbstick(thumbR);
		}
	}
	
	// Button states
	private static enum BSTATE { IDLE, PRESSED, RELEASED};
	
	// All buttons
	public static enum XBUTTON { 
		DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT
		, START, BACK, LEFT_THUMB, RIGHT_THUMB
		, LEFT_SHOULDER, RIGHT_SHOULDER
		, A ,B ,X, Y;
	};
	
	// All button constants
	private static final short[] BCONSTANTS = {
		XINPUT_GAMEPAD_DPAD_UP          
		,XINPUT_GAMEPAD_DPAD_DOWN       
		,XINPUT_GAMEPAD_DPAD_LEFT       
		,XINPUT_GAMEPAD_DPAD_RIGHT      
		,XINPUT_GAMEPAD_START           
		,XINPUT_GAMEPAD_BACK             
		,XINPUT_GAMEPAD_LEFT_THUMB       
		,XINPUT_GAMEPAD_RIGHT_THUMB      
		,XINPUT_GAMEPAD_LEFT_SHOULDER    
		,XINPUT_GAMEPAD_RIGHT_SHOULDER   
		,XINPUT_GAMEPAD_A                
		,XINPUT_GAMEPAD_B                
		,XINPUT_GAMEPAD_X                
		,(short)XINPUT_GAMEPAD_Y                
	};
	
	// track states for all btns
	BSTATE[] btnStates = new BSTATE[BCONSTANTS.length];
	
	/*
	 * Send an event accordinf to the state of the btn
	 */
	private void parseButtons(short buttons) {
		XBUTTON[] all = XBUTTON.values();
		
		for (int i = 0; i < BCONSTANTS.length; i++) {
			if ( (buttons & BCONSTANTS[i]) != 0) {
				// send  PRESSED
				sendButtonPressed(all[i]);
				btnStates[i] = BSTATE.PRESSED;
			}
			else {
				if ( btnStates[i] == BSTATE.PRESSED ) {
					// send REL
					sendButtonReleased(all[i]);
					btnStates[i] = BSTATE.IDLE;
				}
			}
		}
	}
	
	/**
	 * calculates the controller's direction vector and how far along the vector the controller has been pushed (magnitude).
	 * http://msdn.microsoft.com/en-us/library/windows/desktop/ee417001(v=vs.85).aspx
	 * @param pad
	 * @return
	 */
	private static ThumbStick getThumbStick (int idx, float LX, float LY) { 
		ThumbStick thumb = new ThumbStick();
		
		thumb.idx = idx;
		
		//determine how far the controller is pushed
		float magnitude = (float)Math.sqrt(LX*LX + LY*LY);

		//determine the direction the controller is pushed
		thumb.normalizedLX = LX / magnitude;
		thumb.normalizedLY = LY / magnitude;

		thumb.normalizedMagnitude = 0;

		//check if the controller is outside a circular dead zone
		if (magnitude > XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE)
		{
		  //clip the magnitude at its expected maximum value
		  if (magnitude > 32767) magnitude = 32767;
		  
		  //adjust magnitude relative to the end of the dead zone
		  magnitude -= XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE;

		  //optionally normalize the magnitude with respect to its expected range
		  //giving a magnitude value of 0.0 to 1.0
		  thumb.normalizedMagnitude = magnitude / (32767 - XINPUT_GAMEPAD_LEFT_THUMB_DEADZONE);
		}
		else //if the controller is in the deadzone zero out the magnitude
		{
			//magnitude = 0.0f;
			thumb.normalizedMagnitude = 0.0f;
		}
		return thumb;
	}
	
	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
		}
	}
	
    private void dumpBytes(String pref, byte[] buffer, int size) {
    	String s = pref + "[" + size + "]: ";
    	for (int i = 0; i < size; i++) {
    		//s += String.format("0x%2X ", buffer[i]);
    		s += String.format("0x%X ", (buffer[i] & 0xFF) );
		}
    	Log.d(TAG, s); 
    } 
	
}
