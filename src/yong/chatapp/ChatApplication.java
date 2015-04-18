package yong.chatapp;

import java.util.HashMap;
import java.util.Map;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import yong.chatapp.util.SharePreferenceUtils;
import android.app.Application;
import android.graphics.Bitmap.Config;
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
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
		// 线程池内加载的数量
		.threadPoolSize(3).threadPriority(Thread.NORM_PRIORITY - 2)
		.memoryCache(new WeakMemoryCache())
		.denyCacheImageMultipleSizesInMemory()
		.tasksProcessingOrder(QueueProcessingType.LIFO)
		.writeDebugLogs() 
		.build();
	
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
	
	public DisplayImageOptions getAvatarDisplayOptions(){
		int defaultRes =R.drawable.ic_default_avatar;
		return new DisplayImageOptions.Builder()
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.showImageOnLoading(defaultRes)
					.showImageForEmptyUri(defaultRes)
					.showImageOnFail(defaultRes)
					.bitmapConfig(Config.RGB_565).build();
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
