package yong.chatapp.activity;

import cn.bmob.im.db.BmobDB;
import yong.chatapp.R;
import yong.chatapp.adapter.NewFriendAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class NewFriendsActivity extends ActivityBase {

	private ListView friendsListView;
	private NewFriendAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_newfriend);
		initView();
	}

	private void initView() {
		findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		findViewById(R.id.add_friend).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				startActivity(AddFriendActivity.class);
			}
		});
		
		friendsListView = (ListView)findViewById(R.id.list_friends);
		adapter = new NewFriendAdapter(this, BmobDB.create(this).queryBmobInviteList());
		friendsListView.setAdapter(adapter);
	}
}
