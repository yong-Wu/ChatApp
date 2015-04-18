package yong.chatapp.activity;

import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.listener.SaveListener;
import yong.chatapp.R;
import yong.chatapp.model.UserInfo;
import yong.chatapp.util.NetworkUtils;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RegisterActivity extends BaseActivity {
	
	private TextView titleTextView;
	private EditText nameEditText;
	private EditText passwordEditText;
	private Button registerButton;
	private ToggleButton isShowPassword;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		
		initView();
	}

	private void initView() {
		titleTextView = (TextView)findViewById(R.id.title);
		titleTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		nameEditText = (EditText)findViewById(R.id.userName);
		passwordEditText = (EditText)findViewById(R.id.passwd);
		
		registerButton = (Button)findViewById(R.id.btn_register);
		registerButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				register();
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
	
	private void register() {
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
		
		
		final ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this);
		progressDialog.setMessage("正在注册...");
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
		
		final UserInfo user = new UserInfo();
		user.setUsername(nameString);
		user.setPassword(password);
		
		if (((RadioButton)findViewById(R.id.man)).isChecked()) {
			user.setSex(true);
		} else {
			user.setSex(false);
		}
		
		user.setDeviceType("android");
		user.setInstallId(BmobInstallation.getInstallationId(this));
		
		user.signUp(RegisterActivity.this, new SaveListener() {

			@Override
			public void onSuccess() {
				progressDialog.dismiss();
				ShowToast("注册成功");
				// 将设备与username进行绑定
				userManager.bindInstallationForRegister(user.getUsername());
				
				//发广播通知登陆页面关闭
				sendBroadcast(new Intent(LoginActivity.SUCCESS_REGISTER));
				
				//直接进入聊天主页面
				startActivity(MainActivity.class);
				
				finish();
			}

			@Override
			public void onFailure(int arg0, String arg1) {
				ShowToast("注册失败:" + arg1);
				progressDialog.dismiss();
			}
		});
	}
	
}
