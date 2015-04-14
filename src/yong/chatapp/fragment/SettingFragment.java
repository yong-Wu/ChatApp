package yong.chatapp.fragment;

import cn.bmob.im.BmobUserManager;
import yong.chatapp.ChatApplication;
import yong.chatapp.R;
import yong.chatapp.activity.LoginActivity;
import yong.chatapp.activity.PersonInfoActivity;
import yong.chatapp.util.SharePreferenceUtils;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingFragment extends FragmentBase {
	
	private SharePreferenceUtils mSharePreference;
	private Button logoutButton;
	private RelativeLayout personInfoRelativeLayout;
	private TextView personNameTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSharePreference = mApplication.getSharePreferenceUtils();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_setting, container, false);
		initView(view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	}
	
	private void initView(View view) {
		logoutButton = (Button)view.findViewById(R.id.btn_logout);
		personInfoRelativeLayout = (RelativeLayout)view.findViewById(R.id.person_info);
		personNameTextView =(TextView)view.findViewById(R.id.person_name);
		
		logoutButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ChatApplication.getInstance().logout();
				getActivity().finish();
				startActivity(LoginActivity.class);
			}
		});
		
		personInfoRelativeLayout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SettingFragment.this.getActivity(), PersonInfoActivity.class);
				intent.putExtra("username", BmobUserManager.getInstance(getActivity())
						.getCurrentUser().getUsername());
				intent.putExtra("from", "me");
				startActivity(intent);
			}
		});
		
		personNameTextView.setText(BmobUserManager.getInstance(getActivity())
				.getCurrentUser().getUsername());
	}
}
