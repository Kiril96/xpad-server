package com.xbox.ime;


import java.util.HashMap;

import com.xbox.ime.XPadClient.ThumbStick;
import com.xbox.ime.XPadClient.XBUTTON;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

public class XBox360IME extends InputMethodService
	implements SharedPreferences.OnSharedPreferenceChangeListener, XPadClient.IXPadListener
		
{
	static final String TAG = "XBoxIME";

	// preferences keys
	static final String KEY_BTNA = "key_btna";
	static final String KEY_BTNB = "key_btnb";
	static final String KEY_BTNX = "key_btnx";
	static final String KEY_BTNY = "key_btny";
	
	// joystick buttons key bindins
	private String BTN_A = "SPACE"; // space (jump?)
	private String BTN_B = "@"; 	// defaults to fire
	private String BTN_X = "ENTER"; // defaults to fire
	private String BTN_Y = "ESC"; // defaults to fire
	
	private SharedPreferences mSp;
	private int mPort = 5555;
	private String mServer;

	private Handler mHandler = new Handler();
	private XPadClient mClient;
	
	// ASCII -> Android keycode
	private static final HashMap<String, Integer> KEYCODES = new HashMap<String, Integer>();
	
	static {
		// is there a better way?
		KEYCODES.put("0", KeyEvent.KEYCODE_0);
		KEYCODES.put("1", KeyEvent.KEYCODE_1);
		KEYCODES.put("2", KeyEvent.KEYCODE_2);
		KEYCODES.put("3", KeyEvent.KEYCODE_3);
		KEYCODES.put("4", KeyEvent.KEYCODE_4);
		KEYCODES.put("5", KeyEvent.KEYCODE_5);
		KEYCODES.put("6", KeyEvent.KEYCODE_6);
		KEYCODES.put("7", KeyEvent.KEYCODE_7);
		KEYCODES.put("8", KeyEvent.KEYCODE_8);
		KEYCODES.put("9", KeyEvent.KEYCODE_9);
		
		KEYCODES.put("A", KeyEvent.KEYCODE_A);
		KEYCODES.put("B", KeyEvent.KEYCODE_B);
		KEYCODES.put("C", KeyEvent.KEYCODE_C);
		KEYCODES.put("D", KeyEvent.KEYCODE_D);
		KEYCODES.put("E", KeyEvent.KEYCODE_E);
		KEYCODES.put("F", KeyEvent.KEYCODE_F);
		KEYCODES.put("G", KeyEvent.KEYCODE_G);
		KEYCODES.put("H", KeyEvent.KEYCODE_H);
		KEYCODES.put("I", KeyEvent.KEYCODE_I);
		KEYCODES.put("J", KeyEvent.KEYCODE_J);
		KEYCODES.put("K", KeyEvent.KEYCODE_K);
		KEYCODES.put("L", KeyEvent.KEYCODE_L);
		KEYCODES.put("M", KeyEvent.KEYCODE_M);
		KEYCODES.put("N", KeyEvent.KEYCODE_N);
		KEYCODES.put("O", KeyEvent.KEYCODE_O);
		KEYCODES.put("P", KeyEvent.KEYCODE_P);
		KEYCODES.put("Q", KeyEvent.KEYCODE_Q);
		KEYCODES.put("R", KeyEvent.KEYCODE_R);
		KEYCODES.put("S", KeyEvent.KEYCODE_S);
		KEYCODES.put("T", KeyEvent.KEYCODE_T);
		KEYCODES.put("U", KeyEvent.KEYCODE_U);
		KEYCODES.put("V", KeyEvent.KEYCODE_V);
		KEYCODES.put("W", KeyEvent.KEYCODE_W);
		KEYCODES.put("X", KeyEvent.KEYCODE_X);
		KEYCODES.put("Y", KeyEvent.KEYCODE_Y);
		KEYCODES.put("Z", KeyEvent.KEYCODE_Z);
		
		KEYCODES.put(" ", KeyEvent.KEYCODE_SPACE);
		KEYCODES.put("@", KeyEvent.KEYCODE_AT);
		
		KEYCODES.put("SPACE", KeyEvent.KEYCODE_SPACE);
		KEYCODES.put("SHIFT", KeyEvent.KEYCODE_SHIFT_LEFT);
		KEYCODES.put("ENTER", KeyEvent.KEYCODE_ENTER);
		KEYCODES.put("ESC", KeyEvent.KEYCODE_MENU);
		KEYCODES.put("MENU", KeyEvent.KEYCODE_MENU);
		KEYCODES.put("SEARCH", KeyEvent.KEYCODE_SEARCH);
		KEYCODES.put("ALT", KeyEvent.KEYCODE_ALT_LEFT);
		KEYCODES.put(".", KeyEvent.KEYCODE_PERIOD);
		KEYCODES.put("PERIOD", KeyEvent.KEYCODE_PERIOD);
		KEYCODES.put(",", KeyEvent.KEYCODE_COMMA);
		KEYCODES.put("COMMA", KeyEvent.KEYCODE_COMMA);
		KEYCODES.put("/", KeyEvent.KEYCODE_SLASH);
		KEYCODES.put("SLASH", KeyEvent.KEYCODE_SLASH);
		KEYCODES.put("BACK", KeyEvent.KEYCODE_BACK);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// Listen for prefs
		mSp = PreferenceManager.getDefaultSharedPreferences(this);
		mSp.registerOnSharedPreferenceChangeListener(this);
		
		mServer = mSp.getString("key_server", "localhost");
		mPort = Integer.parseInt(mSp.getString("key_port", "5555"));
		
		// load button key bindings
		BTN_A = mSp.getString(KEY_BTNA, BTN_A);
		BTN_B = mSp.getString(KEY_BTNB, BTN_B);
		BTN_X = mSp.getString(KEY_BTNX, BTN_X);
		BTN_Y = mSp.getString(KEY_BTNY, BTN_Y);
		
		Log.d(TAG, "onCreate: button key bindings A:" + BTN_A 
				+ " B:" + BTN_B + " X:" + BTN_X + " Y:" + BTN_Y);
		
		Log.d(TAG, "onCreate: Got server=" + mServer + ":" + mPort);	
		
		if ( mServer == null || mServer.equalsIgnoreCase("localhost")) {
			PostToast(mHandler, this, "Invalid server name. Add a server under Settings.");
			return;
		}
		//connectAsync();
		mClient = new XPadClient(mServer, mPort);
		mClient.setListener(this);
		mClient.connectAsync();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		
		if ( mClient == null ) return;
		
		mClient.disconnect();
		PostToast(mHandler, this, "Disconnected from XBox 360 Gamepad.");
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		//Log.d(TAG, "onSharedPreferenceChanged key:" + key);
		if ( key.equals("key_server")) {
			mServer = sp.getString(key, "localhost");
			Log.d(TAG, "Setting server to:" + mServer); 
		}
		else if ( key.equals("key_port")) {
			mPort = Integer.parseInt(mSp.getString("key_port", "5555"));
			Log.d(TAG, "Setting port to:" + mPort); 
		}
		// get buttons
		else if ( key.equals(KEY_BTNA)) {
			BTN_A = sp.getString(key, BTN_A);
			Log.d(TAG, "Setting Joystick Button A binding to:" + BTN_A); 
		}
		else if ( key.equals(KEY_BTNB)) {
			BTN_B = sp.getString(key, BTN_B);
			Log.d(TAG, "Setting Joystick Button B binding to:" + BTN_B);
		}
		else if ( key.equals(KEY_BTNX)) {
			BTN_X = sp.getString(key, BTN_X);
			Log.d(TAG, "Setting Joystick Button X binding to:" + BTN_X);
		}
		else if ( key.equals(KEY_BTNY)) {
			BTN_Y = sp.getString(key, BTN_Y);
			Log.d(TAG, "Setting Joystick Button Y binding to:" + BTN_Y);
		}
	}

/*    
//    private int lastKey = 0;
	private int[] keyCode = new int[3];
	private int keyCount = 0;

	// Works w/ 2 simultaneous key(s) only!
    private void processGamePad(XINPUT_GAMEPAD pad) {
    	if ( pad == null) return;
    	
    	if (  pad.wButtons == 0 ) { // &&  lastKey != 0 
    		sendKey(KeyEvent.ACTION_UP, keyCode[keyCount - 1]); // lastKey);
//    		lastKey = 0;
    		keyCount = 0;
    		return;
    	}
    	
    	if ( keyCount > 1) {
    		sendKey(KeyEvent.ACTION_UP, keyCode[keyCount - 1]); // lastKey);
    		keyCount--;
    		return;
    	}
    	
//    	int keyCode = 0;
    	if ( (pad.wButtons & XPadClient.XINPUT_GAMEPAD_DPAD_UP) != 0) {
//    		sendKey(KeyEvent.KEYCODE_DPAD_UP);
//    		sendKey(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP);
    		keyCode[keyCount] = KeyEvent.KEYCODE_DPAD_UP;
    	}
    	if ( (pad.wButtons & XPadClient.XINPUT_GAMEPAD_DPAD_DOWN) != 0) {
    		keyCode[keyCount] = KeyEvent.KEYCODE_DPAD_DOWN;
    	}
    	if ( (pad.wButtons & XPadClient.XINPUT_GAMEPAD_DPAD_LEFT) != 0) {
    		keyCode[keyCount] = KeyEvent.KEYCODE_DPAD_LEFT;
    	}
    	if ( (pad.wButtons & XPadClient.XINPUT_GAMEPAD_DPAD_RIGHT) != 0) {
    		keyCode[keyCount] = KeyEvent.KEYCODE_DPAD_RIGHT;
    	}
    	if ( (pad.wButtons & XPadClient.XINPUT_GAMEPAD_A) != 0) {
    		keyCode[keyCount] = KEYCODES.get(BTN_A);
    	}
    	if ( (pad.wButtons & XPadClient.XINPUT_GAMEPAD_B) != 0) {
    		keyCode[keyCount] = KEYCODES.get(BTN_B);
    	}
    	if ( (pad.wButtons & XPadClient.XINPUT_GAMEPAD_X) != 0) {
    		keyCode[keyCount] = KEYCODES.get(BTN_X);
    	}
    	if ( (pad.wButtons & XPadClient.XINPUT_GAMEPAD_Y) != 0) {
    		keyCode[keyCount] = KEYCODES.get(BTN_Y);
    	}
    	if ( keyCode[keyCount] != 0)
    		sendKey(KeyEvent.ACTION_DOWN, keyCode[keyCount]);
    	
    	keyCount++;
    }
*/
	
    /*
    
    private void POSTLOG(final String text) {
    	mHandler.post(new Runnable() {
			public void run() {
				LOG(text);
			}
		});
    } 
    
    private void LOG(String text) {
    	Log.d(TAG, text);
    } */

    public static void PostToast(Handler handler, final Context ctx, final String text) {
    	handler.post(new Runnable() {
			public void run() {
				Toast.makeText(ctx, text, Toast.LENGTH_LONG).show();
			}
		});
    }
    
/*    
    void sendKey(int code) {
//		sendKey(KeyEvent.ACTION_DOWN, code);
//		XInputUtil.sleep(300);
//		sendKey(KeyEvent.ACTION_UP, code);
    	Log.d(TAG, "Send Down/Up " + code);
    	sendDownUpKeyEvents(code);
    }
    */
    
	void sendKey(int action, int code) {
		KeyEvent event = new KeyEvent(action, code);
		InputConnection ic = getCurrentInputConnection();
		
		if ( ic == null) {
			Log.e(TAG, "NULL input connecion!");
			return;
		}
		if ( code == 0) {
			Log.e(TAG, "Invalid key code ZERO!");
			return;
		}
//		System.out.println("SendKey action=" + action + " code=" + code);
//		lastKey = code;
		ic.sendKeyEvent(event);
	}
	
	private int[][] BTN_MAPPINGS = {
			{ XBUTTON.DPAD_UP.ordinal(), KeyEvent.KEYCODE_DPAD_UP},
			{ XBUTTON.DPAD_DOWN.ordinal(), KeyEvent.KEYCODE_DPAD_DOWN},
			{ XBUTTON.DPAD_LEFT.ordinal(), KeyEvent.KEYCODE_DPAD_LEFT},
			{ XBUTTON.DPAD_RIGHT.ordinal(), KeyEvent.KEYCODE_DPAD_RIGHT},
			{ XBUTTON.START.ordinal(), KeyEvent.KEYCODE_ENTER},
			{ XBUTTON.BACK.ordinal(), KeyEvent.KEYCODE_BACK},
			{ XBUTTON.LEFT_THUMB.ordinal(), KeyEvent.KEYCODE_A},
			{ XBUTTON.RIGHT_THUMB.ordinal(), KeyEvent.KEYCODE_Z},
			{ XBUTTON.LEFT_SHOULDER.ordinal(), KeyEvent.KEYCODE_SPACE},
			{ XBUTTON.RIGHT_SHOULDER.ordinal(), KeyEvent.KEYCODE_AT},
			{ XBUTTON.A.ordinal(), KEYCODES.get(BTN_A)},
			{ XBUTTON.B.ordinal(), KEYCODES.get(BTN_B)},
			{ XBUTTON.X.ordinal(), KEYCODES.get(BTN_X)},
			{ XBUTTON.Y.ordinal(), KEYCODES.get(BTN_Y)}
	};
	
	@Override
	public void onError(String reason) {
		PostToast(mHandler, this, reason);
	}

//	@Override
//	public void onEvent(XINPUT_GAMEPAD pad) {
//		processGamePad(pad);
//	}

	@Override
	public void onConnected() {
		PostToast(mHandler, this, "Connected to " + mServer + ":" + mPort);		
	}

	@Override
	public void onButtonPressed(XBUTTON button) {
		int keyCode = BTN_MAPPINGS[button.ordinal()][1];

		Log.d(TAG, "Pressed: " + keyCode);
		sendKey(KeyEvent.ACTION_DOWN, keyCode);
	}

	@Override
	public void onButtonReleased(XBUTTON button) {
		int keyCode = BTN_MAPPINGS[button.ordinal()][1];

		Log.d(TAG, "Released: " + keyCode);
		sendKey(KeyEvent.ACTION_UP, keyCode);
	}

	@Override
	public void onThumbstickEvent(ThumbStick direction) {
		// TODO Auto-generated method stub
		Log.d(TAG, "Thumbstick: " + direction);
	}
	
}
