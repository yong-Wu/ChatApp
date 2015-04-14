package yong.chatapp.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.db.BmobDB;
import yong.chatapp.ChatApplication;
import yong.chatapp.R;
import yong.chatapp.activity.NewFriendsActivity;
import yong.chatapp.activity.PersonInfoActivity;
import yong.chatapp.adapter.ContactAdapter;
import yong.chatapp.model.UserInfo;
import yong.chatapp.util.CollectionUtils;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class ContactFragment extends FragmentBase {

	private ListView friendsListView;
	private LinearLayout headview; 
	private List<UserInfo> userList = new ArrayList<UserInfo>();
	private ContactAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contact, container, false);
		initView(view);
		return view;
	}

	private void initView(View view) {
		friendsListView = (ListView)view.findViewById(R.id.list_friends);
		headview = (LinearLayout)mInflater.inflate(R.layout.item_contact, null);
		((ImageView)headview.findViewById(R.id.avatar)).setImageResource(R.drawable.ic_new_friend);
		headview.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(), NewFriendsActivity.class);
				intent.putExtra("from", "contact");
				startActivity(intent);
			}
		});
		friendsListView.addHeaderView(headview);
		
		friendsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				UserInfo userInfo = (UserInfo) parent.getAdapter().getItem(position);
				Intent intent = new Intent(getActivity(), PersonInfoActivity.class);
				intent.putExtra("from", "other");
				intent.putExtra("username", userInfo.getUsername());
				startActivity(intent);
			}
		});
		
		queryFriends();
	}

	private void queryFriends() {
		
		//是否有新的好友请求
		if(BmobDB.create(getActivity()).hasNewInvite()){
			((TextView)headview.findViewById(R.id.name)).setTextColor(getResources().getColor(R.color.red));
		}else{
			((TextView)headview.findViewById(R.id.name)).setTextColor(getResources().getColor(R.color.black));
		}		
		
		ChatApplication.getInstance().setContactList(CollectionUtils.list2map(BmobDB.create(getActivity()).getContactList()));
		
		Map<String,BmobChatUser> users = ChatApplication.getInstance().getContactList();
		
		fillData(CollectionUtils.map2list(users));
		
		if (adapter == null) {
			adapter = new ContactAdapter(getActivity(), userList);
			friendsListView.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
	}

	private void fillData(List<BmobChatUser> datas) {
		userList.clear();
		int total = datas.size();
		for (int i = 0; i < total; i++) {
			BmobChatUser user = datas.get(i);
			UserInfo userInfo = new UserInfo();
			userInfo.setAvatar(user.getAvatar());
			userInfo.setUsername(user.getUsername());
			userInfo.setObjectId(user.getObjectId());
			userInfo.setContacts(user.getContacts());
			
			userList.add(userInfo);
		}
	}
	
	private boolean hidden;
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		this.hidden = hidden;
		if(!hidden){
			refresh();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(!hidden){
			refresh();
		}
	}
	
	public void refresh(){
		try {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					queryFriends();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
