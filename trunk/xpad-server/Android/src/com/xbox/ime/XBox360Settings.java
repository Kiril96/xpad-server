package com.xbox.ime;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

public class XBox360Settings extends PreferenceActivity
	implements SharedPreferences.OnSharedPreferenceChangeListener
{

	static final String TAG = "XBOXSettings";
	
	static final String KEY_SHOWIME = "key_showime";
	
	private SharedPreferences mSp;
	
	private InputMethodManager mIME;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		
		mSp = PreferenceManager.getDefaultSharedPreferences(this);
		mSp.registerOnSharedPreferenceChangeListener(this);

		mIME = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG,"Key=" + key);
		
		if (key.equals(KEY_SHOWIME)) {
			mIME.showInputMethodPicker();
		}
		
	}

}
