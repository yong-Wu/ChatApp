package yong.chatapp.activity;

import cn.bmob.im.BmobUserManager;
import android.os.Bundle;

public class ActivityBase extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//登陆状态下,检测当前用户是否在其他设备登陆
		checkLogin();
	}
	
	private void checkLogin() {
		BmobUserManager userManager = BmobUserManager.getInstance(this);
		if (userManager.getCurrentUser() == null) {
			ShowToast("您的账号已在其他设备上登录!");
			startActivity(LoginActivity.class);
			finish();
		}
	}
}
