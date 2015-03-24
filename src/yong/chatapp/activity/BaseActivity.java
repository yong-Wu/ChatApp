package yong.chatapp.activity;

import java.util.List;
import yong.chatapp.ChatApplication;
import yong.chatapp.util.CollectionUtils;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.v3.listener.FindListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class BaseActivity extends FragmentActivity {
	
	public BmobUserManager userManager;
	public BmobChatManager chatManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		userManager = BmobUserManager.getInstance(this);
		chatManager = BmobChatManager.getInstance(this);
	}
	
	public void updateUserInfos(){
		userManager.queryCurrentContactList(new FindListener<BmobChatUser>() {

			@Override
			public void onError(int errorCode, String errorMsg) {
				if(errorCode == BmobConfig.CODE_COMMON_NONE){
					Log.i("userList", errorMsg);
				}else{
					Log.i("userList", "查询好友列表失败："+errorMsg);
				}
			}

			@Override
			public void onSuccess(List<BmobChatUser> list) {
				ChatApplication.getInstance().setContactList(CollectionUtils.list2map(list));
			}
			
		});
	}
	
	Toast mToast;

	public void ShowToast(final String text) {
		if (!TextUtils.isEmpty(text)) {
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if (mToast == null) {
						mToast = Toast.makeText(getApplicationContext(), text,
								Toast.LENGTH_LONG);
					} else {
						mToast.setText(text);
					}
					mToast.show();
				}
			});
			
		}
	}

	public void ShowToast(final int resId) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (mToast == null) {
					mToast = Toast.makeText(getApplicationContext(), resId,
							Toast.LENGTH_LONG);
				} else {
					mToast.setText(resId);
				}
				mToast.show();
			}
		});
	}
	
	public void startActivity(Class<?> _class) {
		startActivity(new Intent(this, _class));
	}

}
