package yong.chatapp.activity;

import cn.bmob.im.BmobChat;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import yong.chatapp.ChatApplication;
import yong.chatapp.MessageReceiver;
import yong.chatapp.R;
import yong.chatapp.fragment.ContactFragment;
import yong.chatapp.fragment.MessageFragment;
import yong.chatapp.fragment.SettingFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActivityBase implements EventListener {

	private Button[] tabButtons;
	private MessageFragment messageFragment;
	private ContactFragment contactFragment;
	private SettingFragment settingFragment;
	private Fragment[] fragments;
	
	private TextView titleTextView;
	private int[] titles = new int[]{R.string.tab_message, R.string.tab_contact, R.string.tab_setting};
	
	private int index = 0;
	private int currentTabIndex = 0;
	
	private ImageView messageTip;
	private ImageView contactTip;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//开启定时检测服务（单位为秒）-在这里检测后台是否还有未读的消息，有的话就取出来
		//检测服务比较耗流量和电量
		BmobChat.getInstance(this).startPollService(30);
		
		initView();
		initTabs();
		//开启广播接收器
		initNewMessageBroadCast();
		initNewContactBroadCast();
	}

	private BroadcastReceiver mNewMessageReceiver;
	private BroadcastReceiver mNewContactReceiver;
	
	private void initNewMessageBroadCast() {
		mNewMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				refreshNewMsg(null);
				abortBroadcast();
			}
		};
		
		IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_NEW_MESSAGE);
		//优先级要低于ChatActivity
		intentFilter.setPriority(3);
		registerReceiver(mNewMessageReceiver, intentFilter);
	}

	private void initNewContactBroadCast(){
		mNewContactReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent){
				BmobInvitation message = (BmobInvitation) intent.getSerializableExtra("invite");
				refreshNewContact(message);
				abortBroadcast();
			}
		};
		
		IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_ADD_USER_MESSAGE);
		//优先级要低于ChatActivity
		intentFilter.setPriority(3);
		registerReceiver(mNewContactReceiver, intentFilter);
	}
	
	private void initView() {
		tabButtons = new Button[3];
		tabButtons[0] = (Button)findViewById(R.id.message_btn);
		tabButtons[1] = (Button)findViewById(R.id.contact_btn);
		tabButtons[2] = (Button)findViewById(R.id.setting_btn);
		
		titleTextView = (TextView)findViewById(R.id.title);
		
		messageTip = (ImageView)findViewById(R.id.message_tips);
		contactTip = (ImageView)findViewById(R.id.contact_tips);
		
		tabButtons[0].setSelected(true);
	}

	private void initTabs() {
		messageFragment = new MessageFragment();
		contactFragment = new ContactFragment();
		settingFragment = new SettingFragment();
		
		fragments = new Fragment[]{messageFragment, contactFragment, settingFragment};
		
		getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, messageFragment)
		.add(R.id.fragment_container, contactFragment).hide(contactFragment).show(messageFragment).commit();
	}

	/**
	 * 底部button点击事件
	 * @param view
	 */
	public void onTabSelect(View view) {
		switch (view.getId()) {
		case R.id.message_btn:
			index = 0;
			break;
		case R.id.contact_btn:
			index = 1;
			break;
		case R.id.setting_btn:
			index = 2;
			break;
		}
		
		if (currentTabIndex != index) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.hide(fragments[currentTabIndex]);
			if (!fragments[index].isAdded()){
				transaction.add(R.id.fragment_container, fragments[index]);
			}
			transaction.hide(fragments[currentTabIndex]).show(fragments[index]).commit();
			
			titleTextView.setText(titles[index]);
			tabButtons[index].setSelected(true);
			tabButtons[currentTabIndex].setSelected(false);
			currentTabIndex = index;
		}	
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//小圆点提示
		if(BmobDB.create(this).hasUnReadMsg()){
			messageTip.setVisibility(View.VISIBLE);
		}else {
			messageTip.setVisibility(View.GONE);
		}
		
		if(BmobDB.create(this).hasNewInvite()){
			contactTip.setVisibility(View.VISIBLE);
		}else {
			contactTip.setVisibility(View.GONE);
		}
		
		MessageReceiver.eventListenerList.add(this); // 监听推送的消息
		MessageReceiver.mPushMsgNum = 0;
	}

	@Override
	protected void onPause() {
		super.onPause();
		MessageReceiver.eventListenerList.remove(this); // 取消监听推送的消息
	}

	@Override
	public void onMessage(BmobMsg message) {
		refreshNewMsg(message);
	}
	
	private void refreshNewMsg(BmobMsg message) {
		boolean isAllowVoice = ChatApplication.getInstance().getSharePreferenceUtils().isAllowVoice();
		if (isAllowVoice) {
			ChatApplication.getInstance().getMediaPlayer().start();
		}
		
		messageTip.setVisibility(View.VISIBLE);
		
		if (message != null){
			BmobChatManager.getInstance(this).saveReceiveMessage(true, message);
		}
		
		if (currentTabIndex == 0){
			if (messageFragment != null){
				messageFragment.refresh();
			}
		}
	}

	@Override
	public void onAddUser(BmobInvitation message) {
		refreshNewContact(message);
	}
	
	private void refreshNewContact(BmobInvitation message) {
		boolean isAllowVoice = ChatApplication.getInstance().getSharePreferenceUtils().isAllowVoice();
		if (isAllowVoice) {
			ChatApplication.getInstance().getMediaPlayer().start();
		}
		
		contactTip.setVisibility(View.VISIBLE);
		if (currentTabIndex == 1){
			if (contactFragment != null){
				contactFragment.refresh();
			}
		} else {
			//同时提醒通知
			String tickerText = message.getFromname()+"请求添加好友";
			boolean isAllowVibrate = ChatApplication.getInstance().getSharePreferenceUtils().isAllowVibrate();
			BmobNotifyManager.getInstance(this).showNotify(isAllowVoice, isAllowVibrate,
					R.drawable.ic_launcher, tickerText, message.getFromname(), tickerText.toString(), NewFriendsActivity.class);
		}
	}

	@Override
	public void onNetChange(boolean isNetConnected) {
		if(isNetConnected){
			ShowToast(R.string.network_useless);
		}		
	}

	@Override
	public void onOffline() {
		ShowToast("您的账号已在其他设备上登录！请重新登录");
		startActivity(LoginActivity.class);
		finish();
	}

	@Override
	public void onReaded(String arg0, String arg1) {
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(mNewMessageReceiver);
			unregisterReceiver(mNewContactReceiver);
		} catch (Exception e) {
		}
		//取消定时检测服务
		BmobChat.getInstance(this).stopPollService();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle bundle) {
	    // Empty
	}
}
