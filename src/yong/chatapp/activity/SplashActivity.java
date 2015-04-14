package yong.chatapp.activity;

import cn.bmob.im.BmobChat;
import yong.chatapp.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SplashActivity extends BaseActivity {

	private static final String APPID = "7b4eebee5084647cc1bebf60a9f9510e";
	
	private static final int GO_HOME = 1;
	private static final int GO_LOGIN = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		//调试模式为true时，可在logcat查看日志
		BmobChat.DEBUG_MODE = true;
		
		//初始化APPID;
		BmobChat.getInstance(this).init(APPID);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (userManager.getCurrentUser() != null) {
			updateUserInfos();
			mHandler.sendEmptyMessageDelayed(GO_HOME, 2000);
		} else {
			mHandler.sendEmptyMessageDelayed(GO_LOGIN, 2000);
		}
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_HOME:
				startActivity(MainActivity.class);
				finish();
				break;
			case GO_LOGIN:
				startActivity(LoginActivity.class);
				finish();
				break;
			}
		}
	};
}
