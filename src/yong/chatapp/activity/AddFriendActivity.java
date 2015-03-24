package yong.chatapp.activity;

import java.util.List;

import cn.bmob.im.BmobChatManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.PushListener;
import yong.chatapp.R;
import yong.chatapp.model.UserInfo;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddFriendActivity extends ActivityBase {

	private EditText searchNameEditText;
	private String userName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addfriend);
		initView();
	}

	private void initView() {
		searchNameEditText = (EditText)findViewById(R.id.search_name);
		
		findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				userName = searchNameEditText.getText().toString();
				
				if (TextUtils.isEmpty(userName)) {
					ShowToast(R.string.null_name);
				} else {
					searchFriend();
				}
			}
		});
	}
	
	private void searchFriend() {
		final ProgressDialog progress = new ProgressDialog(AddFriendActivity.this);
		progress.setMessage("正在搜索...");
		progress.setCanceledOnTouchOutside(true);
		progress.show();
		
		userManager.queryUser(userName, new FindListener<UserInfo>() {
			
			@Override
			public void onSuccess(List<UserInfo> users) {
				if (users != null && users.size() > 0){
					final UserInfo user = users.get(0);
					
					findViewById(R.id.new_friend).setVisibility(View.VISIBLE);
					((TextView)findViewById(R.id.name)).setText(user.getUsername());
					ShowToast("搜索成功");
					
					findViewById(R.id.add_btn).setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							final ProgressDialog progressInner = new ProgressDialog(AddFriendActivity.this);
							progressInner.setMessage("正在添加...");
							progressInner.setCanceledOnTouchOutside(false);
							progressInner.show();
							
							BmobChatManager.getInstance(AddFriendActivity.this).sendTagMessage(BmobConfig.TAG_ADD_CONTACT, 
									user.getObjectId(), new PushListener() {

										@Override
										public void onFailure(int errorCode, String errorMsg) {
											ShowToast("发送请求失败，请重新添加!");
											progressInner.dismiss();
										}

										@Override
										public void onSuccess() {
											ShowToast("发送请求成功，等待对方验证!");
											((Button)findViewById(R.id.add_btn)).setText("已请求");
											((Button)findViewById(R.id.add_btn)).setEnabled(false);
											((Button)findViewById(R.id.add_btn)).setBackgroundColor(getResources().getColor(R.color.white));
											((Button)findViewById(R.id.add_btn)).setTextColor(getResources().getColor(R.color.gray));
											progressInner.dismiss();
										}
								
							});
						}
					});
				} else {
					ShowToast("用户不存在");
				}
				
				progress.dismiss();
			}
			
			@Override
			public void onError(int errorCode, String errorMsg) {
				ShowToast(errorMsg);
				progress.dismiss();
			}
		});
	}
}
