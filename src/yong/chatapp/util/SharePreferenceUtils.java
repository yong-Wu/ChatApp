package yong.chatapp.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtils {

	private SharedPreferences mSharedPreferences;
	private static SharedPreferences.Editor mEditor;
	
	@SuppressLint("CommitPrefEdits")
	public SharePreferenceUtils(Context context, String name){
		mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
	}

	private String SHARED_KEY_NOTIFY = "shared_key_notify";
	private String SHARED_KEY_VOICE = "shared_key_sound";
	private String SHARED_KEY_VIBRATE = "shared_key_vibrate";
	
	// 是否允许推送通知
	public boolean isAllowPushNotify() {
		return mSharedPreferences.getBoolean(SHARED_KEY_NOTIFY, true);
	}

	public void setPushNotifyEnable(boolean isChecked) {
		mEditor.putBoolean(SHARED_KEY_NOTIFY, isChecked);
		mEditor.commit();
	}
	
	// 是否允许声音
	public boolean isAllowVoice() {
		return mSharedPreferences.getBoolean(SHARED_KEY_VOICE, true);
	}

	public void setAllowVoiceEnable(boolean isChecked) {
		mEditor.putBoolean(SHARED_KEY_VOICE, isChecked);
		mEditor.commit();
	}

	// 是否允许震动
	public boolean isAllowVibrate() {
		return mSharedPreferences.getBoolean(SHARED_KEY_VIBRATE, true);
	}

	public void setAllowVibrateEnable(boolean isChecked) {
		mEditor.putBoolean(SHARED_KEY_VIBRATE, isChecked);
		mEditor.commit();
	}	
}
