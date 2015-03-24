package yong.chatapp.activity;

import java.util.List;

import cn.bmob.v3.listener.FindListener;
import yong.chatapp.R;
import yong.chatapp.model.UserInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PersonInfoActivity extends ActivityBase {

	private TextView usernameTextView;
	private TextView genderTextView;
	private String username;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_person_info);
		initView();
		initData();
	}

	private void initView() {
		findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		usernameTextView = (TextView)findViewById(R.id.username);
		genderTextView = (TextView)findViewById(R.id.gender);
	}

	private void initData() {
		username = getIntent().getStringExtra("username");
		usernameTextView.setText(username);
		
		userManager.queryUser(username, new FindListener<UserInfo>() {

			@Override
			public void onError(int errorCode, String errorMsg) {
				ShowToast(errorMsg);
			}

			@Override
			public void onSuccess(List<UserInfo> users) {
				if (users != null && users.size() > 0){
					updateUserInfo(users.get(0));
				} 
			}
		});
	}

	private void updateUserInfo(UserInfo userInfo) {
		usernameTextView.setText(userInfo.getUsername());
		genderTextView.setText(userInfo.getSex() ? "男" : "女");
	}
}
