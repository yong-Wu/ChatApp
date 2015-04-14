package yong.chatapp.activity;

import java.util.List;

import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import yong.chatapp.ChatApplication;
import yong.chatapp.R;
import yong.chatapp.model.UserInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PersonInfoActivity extends ActivityBase {

	private TextView usernameTextView;
	private TextView genderTextView;
	private String username;
	private String source;
	private Button beginChat;
	private Button deleteFriend;
	
	private UserInfo userInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_person_info);
		source = getIntent().getStringExtra("from");
		
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
		
		beginChat = (Button)findViewById(R.id.begin_chat);
		deleteFriend = (Button)findViewById(R.id.delete_friend);
		
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
					userInfo = users.get(0);
					updateUserInfo(userInfo);
					
					if (source.equals("other")){
						beginChat.setVisibility(View.VISIBLE);
						deleteFriend.setVisibility(View.VISIBLE);
						
						beginChat.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								Intent intent = new Intent(PersonInfoActivity.this, ChatActivity.class);
								intent.putExtra("user", userInfo);
								startActivity(intent);
								finish();
							}
						});
						
						deleteFriend.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								new AlertDialog.Builder(PersonInfoActivity.this)
								.setMessage("确定要删除该好友吗？")
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										final ProgressDialog progress = new ProgressDialog(PersonInfoActivity.this);
										progress.setMessage("正在删除...");
										progress.setCanceledOnTouchOutside(false);
										progress.show();
										userManager.deleteContact(userInfo.getObjectId(), new UpdateListener() {

											@Override
											public void onFailure(int arg0, String arg1) {
												ShowToast("删除失败："+arg1);									
											}

											@Override
											public void onSuccess() {
												ShowToast("删除成功");
												//删除内存
												ChatApplication.getInstance().getContactList().remove(userInfo.getUsername());
												//更新界面
												progress.dismiss();
												finish();
											}
											
										});
									}
								})
								.setNegativeButton("取消", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int arg1) {
										dialog.cancel();
									}
								}).show();
							}
						});
					}
				} 
			}
		});
	}

	private void updateUserInfo(UserInfo userInfo) {
		usernameTextView.setText(userInfo.getUsername());
		genderTextView.setText(userInfo.getSex() ? "男" : "女");
	}
}
