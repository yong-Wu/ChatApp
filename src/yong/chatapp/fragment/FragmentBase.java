package yong.chatapp.fragment;

import yong.chatapp.ChatApplication;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.widget.Toast;

public class FragmentBase extends Fragment {

	public BmobUserManager userManager;
	public BmobChatManager manager;
	
	public ChatApplication mApplication;
	public LayoutInflater mInflater;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mApplication = ChatApplication.getInstance();
		userManager = BmobUserManager.getInstance(getActivity());
		manager = BmobChatManager.getInstance(getActivity());
		mInflater = LayoutInflater.from(getActivity());
	}

	Toast mToast;

	public void ShowToast(String text) {
		if (mToast == null) {
			mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
		} else {
			mToast.setText(text);
		}
		mToast.show();
	}

	public void ShowToast(int text) {
		if (mToast == null) {
			mToast = Toast.makeText(getActivity(), text, Toast.LENGTH_LONG);
		} else {
			mToast.setText(text);
		}
		mToast.show();
	}

	public void startActivity(Class<?> cla) {
		getActivity().startActivity(new Intent(getActivity(), cla));
	}
}
