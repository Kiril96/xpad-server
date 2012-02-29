package com.xbox.ime;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class XBox360Activity extends Activity
{

	static final String TAG  = "XBOXActivity";
	

	private Handler mHandler = new Handler();
	
	EditText mLog;
	
	private InputMethodManager mIME;
	

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mLog = (EditText)findViewById(R.id.etLog);
        mIME = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        
        ((Button)findViewById(R.id.btnSelectIME)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				List<InputMethodInfo> list = mIME.getInputMethodList();
//				for (InputMethodInfo imeInfo : list) {
//					Log.d(TAG, imeInfo.getServiceName());
//				}
				mIME.showInputMethodPicker();
				
			}
		});
        
        ((Button)findViewById(R.id.btnClear)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mLog.setText("");
			}
		});

        ((Button)findViewById(R.id.btnSettings)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(XBox360Activity.this, XBox360Settings.class));
			}
		});
        
        /*
        ((Button)findViewById(R.id.btnConnect)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				connectAsync();
			}
		});

        ((Button)findViewById(R.id.btnDisconnect)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disconnect();
			}
		});
        */
        ((Button)findViewById(R.id.btnQuit)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				System.exit(0);
			}
		});
        
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	LOG("Key Down: " + event.getKeyCode() + "\n"); // + " " + event.getDisplayLabel() + "\n");
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	LOG("Key Up: " + event.getKeyCode() + "\n"); // + " " + event.getDisplayLabel() + "\n");
    	return super.onKeyUp(keyCode, event);
    }
    
//    private void dumpBytes(String pref, byte[] buffer, int size) {
//    	POSTLOG(pref + ":");
//    	for (int i = 0; i < size; i++) {
//    		POSTLOG(String.format("0x%2X ", buffer[i]));
//		}
//    	POSTLOG("\n");
//    }
    
    private void POSTLOG(final String text) {
    	mHandler.post(new Runnable() {
			public void run() {
				LOG(text);
			}
		});
    }
    private void LOG(String text) {
    	if ( mLog != null) {
    		mLog.append(text);
    		Log.d(TAG, text);
    	}
    }

}