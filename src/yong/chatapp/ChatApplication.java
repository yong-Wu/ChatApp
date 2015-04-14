package yong.chatapp;

import java.util.HashMap;
import java.util.Map;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import yong.chatapp.util.SharePreferenceUtils;
import android.app.Application;
import android.media.MediaPlayer;

public class ChatApplication extends Application {

	public static ChatApplication mInstance;
	
	public SharePreferenceUtils mSharePreferenceUtils;
	public final static String PREFERENCE_NAME = "_share_preference_info";
	
	private Map<String, BmobChatUser> contactList = new HashMap<String, BmobChatUser>();
	
	private MediaPlayer mediaPlayer;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		Init();
	}

	private void Init() {
		
	}
	
	public static ChatApplication getInstance() {
		return mInstance;
	}
	
	public synchronized SharePreferenceUtils getSharePreferenceUtils(){
		if (mSharePreferenceUtils == null){
			String currentUserId = BmobUserManager.getInstance(
					this).getCurrentUserObjectId();
			String shareName = currentUserId + PREFERENCE_NAME;
			mSharePreferenceUtils = new SharePreferenceUtils(this, shareName);
		}
		return mSharePreferenceUtils;
	}
	
	public void logout(){
		BmobUserManager.getInstance(getApplicationContext()).logout();
		setContactList(null);
	}
	
	public Map<String, BmobChatUser> getContactList() {
		return contactList;
	}
	
	public void setContactList(Map<String, BmobChatUser> contactList) {
		if (this.contactList != null) {
			this.contactList.clear();
		}
		this.contactList = contactList;
	}
	
	public synchronized MediaPlayer getMediaPlayer(){
		if (mediaPlayer == null)
			mediaPlayer = MediaPlayer.create(this, R.raw.notify);
		return mediaPlayer;
	}
}
