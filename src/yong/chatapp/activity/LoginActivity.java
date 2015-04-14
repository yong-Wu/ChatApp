package yong.chatapp.activity;

import cn.bmob.v3.listener.SaveListener;
import yong.chatapp.R;
import yong.chatapp.model.UserInfo;
import yong.chatapp.util.NetworkUtils;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class LoginActivity extends BaseActivity {

	private Button loginButton;
	private EditText nameEditText;
	private EditText passwordEditText;
	private TextView registerTextView;
	private ToggleButton isShowPassword;
	
	//注册成功直接进入聊天主页面，发送广播关闭登录页面
	public static final String SUCCESS_REGISTER = "success_register";
	private final BroadcastReceiver mSuccessRegisterReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		initView();
		
		registerReceiver(mSuccessRegisterReceiver, new IntentFilter(SUCCESS_REGISTER));
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mSuccessRegisterReceiver);
	}
	
	private void initView() {
		loginButton = (Button)findViewById(R.id.btn_login);
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				login();
			}
		});		
		
		nameEditText = (EditText)findViewById(R.id.userName);
		passwordEditText = (EditText)findViewById(R.id.passwd);
		registerTextView = (TextView)findViewById(R.id.register);

		
		registerTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startActivity(RegisterActivity.class);
			}
		});
		
		isShowPassword=(ToggleButton)findViewById(R.id.isShowPassword);
		isShowPassword.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				}else{
					passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
			}
		});
	}


	private void login() {
		String nameString = nameEditText.getText().toString();
		String password = passwordEditText.getText().toString();
		
		if (TextUtils.isEmpty(nameString)) { 
			ShowToast(R.string.null_name);
			return;
		}
		
		if (TextUtils.isEmpty(password)) {
			ShowToast(R.string.null_password);
			return;
		}
		
		if (!NetworkUtils.isNetworkAvailable(this)){
			ShowToast(R.string.network_useless);
			return;
		}
		
		final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
		progressDialog.setMessage("正在登录...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
		
		UserInfo user = new UserInfo();
		user.setUsername(nameString);
		user.setPassword(password);
		
		userManager.login(user, new SaveListener() {
			
			@Override
			public void onSuccess() {
				updateUserInfos();
				progressDialog.dismiss();
				startActivity(MainActivity.class);
				finish();
			}
			
			@Override
			public void onFailure(int errorCode, String errorMessage) {
				progressDialog.dismiss();
				ShowToast(errorMessage);
			}
		});

	}
}
