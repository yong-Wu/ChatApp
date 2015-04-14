package yong.chatapp.activity;

import java.util.ArrayList;
import java.util.List;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.util.BmobLog;
import yong.chatapp.MessageReceiver;
import yong.chatapp.R;
import yong.chatapp.adapter.ChatListAdapter;
import yong.chatapp.adapter.EmoViewPagerAdapter;
import yong.chatapp.adapter.EmotionAdapter;
import yong.chatapp.model.UserInfo;
import yong.chatapp.util.FaceTextUtils;
import yong.chatapp.util.NetworkUtils;
import yong.chatapp.view.EmotionEditText;
import yong.chatapp.xlistview.XListView;
import yong.chatapp.xlistview.XListView.IXListViewListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ChatActivity extends ActivityBase implements View.OnClickListener, 
	IXListViewListener, EventListener{
	
	UserInfo userInfo;
	
	ImageView sendEmotion;
	ImageView sendText;
	EmotionEditText editContent;
	
	LinearLayout layoutEmotion;
	ViewPager pagerEmotion;
	
	XListView mListView;
	ChatListAdapter mAdapter;
	
	public static final int NEW_MESSAGE = 0x001;// 收到消息
	
	private static int MsgPagerNum;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		MsgPagerNum = 0;
		userInfo = (UserInfo)getIntent().getSerializableExtra("user");

		initView();
		
		//注册广播接收器
		initNewMessageBroadCast();
	}

	BroadcastReceiver mNewMessageReceiver;
	private void initNewMessageBroadCast() {
		mNewMessageReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				String fromId = intent.getStringExtra("fromId");
				String msgId = intent.getStringExtra("msgId");
				String msgTime = intent.getStringExtra("msgTime");
				// 收到这个广播的时候，message已经在消息表中，可直接获取
				BmobMsg msg = BmobChatManager.getInstance(ChatActivity.this).getMessage(msgId, msgTime);
				if (!fromId.equals(userInfo.getObjectId())) {// 如果不是当前正在聊天对象的消息，不处理
					return;
				}
				//添加到当前页面
				mAdapter.add(msg);
				// 定位
				mListView.setSelection(mAdapter.getCount() - 1);
				//取消当前聊天对象的未读标示
				BmobDB.create(ChatActivity.this).resetUnread(userInfo.getObjectId());
				// 记得把广播给终结掉
				abortBroadcast();
			}
		};
		
		IntentFilter intentFilter = new IntentFilter(BmobConfig.BROADCAST_NEW_MESSAGE);
		//设置广播的优先级别大于Mainacitivity,这样如果消息来的时候正好在chat页面，直接显示消息，而不是提示消息未读
		intentFilter.setPriority(5);
		registerReceiver(mNewMessageReceiver, intentFilter);
	}

	private void initView() {
		((TextView)findViewById(R.id.title)).setText(userInfo.getUsername());
		findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		sendEmotion = (ImageView)findViewById(R.id.btn_emotion);
		sendText = (ImageView)findViewById(R.id.btn_send);
		editContent = (EmotionEditText)findViewById(R.id.edit_content);
		layoutEmotion = (LinearLayout)findViewById(R.id.layout_emotion);
		mListView = (XListView)findViewById(R.id.chat_list);
		
		sendEmotion.setOnClickListener(this);
		sendText.setOnClickListener(this);
		editContent.setOnClickListener(this);
		
		initEmotionView();
		initXListView();
	}

	private void initXListView() {
		// 首先不允许加载更多
		mListView.setPullLoadEnable(false);
		// 允许下拉
		mListView.setPullRefreshEnable(true);
		// 设置监听器
		mListView.setXListViewListener(this);
		mListView.pullRefreshing();
		mListView.setDividerHeight(0);
		// 加载数据
		initOrRefresh();
		mListView.setSelection(mAdapter.getCount() - 1);
		mListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				hideSoftInputView();
				layoutEmotion.setVisibility(View.GONE);
				return false;
			}
		});
	}

	List<String> emotions;
	
	private void initEmotionView() {
		pagerEmotion = (ViewPager)findViewById(R.id.pager_emotion);

		emotions = FaceTextUtils.faceTexts;
		
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < 2; ++i) {
			views.add(getGridView(i));
		}
		
		pagerEmotion.setAdapter(new EmoViewPagerAdapter(views));
	}

	private View getGridView(final int i) {
		View view = View.inflate(this, R.layout.include_emo_gridview, null);
		GridView gridview = (GridView) view.findViewById(R.id.gridview);
		List<String> list = new ArrayList<String>();
		if (i == 0) {
			list.addAll(emotions.subList(0, 21));
		} else if (i == 1) {
			list.addAll(emotions.subList(21, emotions.size()));
		}
		final EmotionAdapter gridAdapter = new EmotionAdapter(ChatActivity.this,
				list);
		gridview.setAdapter(gridAdapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				String key = (String) gridAdapter.getItem(position);
				
				try {
					if (editContent != null && !TextUtils.isEmpty(key)) {
						int start = editContent.getSelectionStart();
						CharSequence content = editContent.getText()
								.insert(start, key);
						editContent.setText(content);
						// 定位光标位置
						CharSequence info = editContent.getText();
						if (info instanceof Spannable) {
							Spannable spanText = (Spannable) info;
							Selection.setSelection(spanText,
									start + key.length());
						}
					}
				} catch (Exception e) {

				}

			}
		});
		return view;
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_send:
			final String msg = editContent.getText().toString();
			if (msg.equals("")) {
				ShowToast("请输入发送消息!");
				return;
			}
			boolean isNetConnected = NetworkUtils.isNetworkAvailable(this);
			if (!isNetConnected) {
				ShowToast(R.string.network_useless);
			}
			// 组装BmobMessage对象
			BmobMsg message = BmobMsg.createTextSendMsg(this, userInfo.getObjectId(), msg);
			// 默认发送完成，将数据保存到本地消息表和最近会话表中
			chatManager.sendTextMessage(userInfo, message);
			// 刷新界面
			refreshMessage(message);
			break;

		case R.id.btn_emotion:
			if (layoutEmotion.getVisibility() == View.VISIBLE) {
				layoutEmotion.setVisibility(View.GONE);
			} else {
				layoutEmotion.setVisibility(View.VISIBLE);
				hideSoftInputView();
			}
			break;
			
		case R.id.edit_content:
			if (layoutEmotion.getVisibility() == View.VISIBLE) {
				layoutEmotion.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}
	}

	private void refreshMessage(BmobMsg msg) {
		// 更新界面
		mAdapter.add(msg);
		mListView.setSelection(mAdapter.getCount() - 1);
		editContent.setText("");
	}	
	
	public void hideSoftInputView() {
		InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (getCurrentFocus() != null)
				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == NEW_MESSAGE) {
				BmobMsg message = (BmobMsg) msg.obj;
				String uid = message.getBelongId();
				BmobMsg m = BmobChatManager.getInstance(ChatActivity.this).getMessage(message.getConversationId(), message.getMsgTime());
				if (!uid.equals(userInfo.getObjectId()))// 如果不是当前正在聊天对象的消息，不处理
					return;
				mAdapter.add(m);
				// 定位
				mListView.setSelection(mAdapter.getCount() - 1);
				//取消当前聊天对象的未读标示
				BmobDB.create(ChatActivity.this).resetUnread(userInfo.getObjectId());
			}
		}
	};
	
	@Override
	public void onRefresh() {
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				MsgPagerNum++;
				int total = BmobDB.create(ChatActivity.this).queryChatTotalCount(userInfo.getObjectId());
				BmobLog.i("记录总数：" + total);
				int currents = mAdapter.getCount();
				if (total <= currents) {
					ShowToast("聊天记录加载完了哦!");
				} else {
					List<BmobMsg> msgList = initMsgData();
					mAdapter.setList(msgList);
					mListView.setSelection(mAdapter.getCount() - currents - 1);
				}
				mListView.stopRefresh();
			}
		}, 1000);
	}

	@Override
	public void onLoadMore() {
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// 新消息到达，重新刷新界面
		initOrRefresh();
		MessageReceiver.eventListenerList.add(this);// 监听推送的消息
		// 有可能锁屏期间，在聊天界面出现通知栏，这时候需要清除通知和清空未读消息数
		BmobNotifyManager.getInstance(this).cancelNotify();
		BmobDB.create(this).resetUnread(userInfo.getObjectId());
		//清空消息未读数-这个要在刷新之后
		MessageReceiver.mPushMsgNum = 0;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		MessageReceiver.eventListenerList.remove(this);
	}
	
	private void initOrRefresh() {
		if (mAdapter != null) {
			if (MessageReceiver.mPushMsgNum != 0) {// 用于更新当在聊天界面锁屏期间来了消息，这时再回到聊天页面的时候需要显示新来的消息
				int news =  MessageReceiver.mPushMsgNum;//有可能锁屏期间，来了N条消息,因此需要倒叙显示在界面上
				int size = initMsgData().size();
				for(int i=(news-1);i>=0;i--){
					mAdapter.add(initMsgData().get(size-(i+1)));// 添加最后一条消息到界面显示
				}
				mListView.setSelection(mAdapter.getCount() - 1);
			} else {
				mAdapter.notifyDataSetChanged();
			}
		} else {
			mAdapter = new ChatListAdapter(this, initMsgData());
			mListView.setAdapter(mAdapter);
		}
	}
	
	private List<BmobMsg> initMsgData() {
		List<BmobMsg> list = BmobDB.create(this).queryMessages(userInfo.getObjectId(), MsgPagerNum);
		return list;
	}

	@Override
	public void onAddUser(BmobInvitation arg0) {
		
	}

	@Override
	public void onMessage(BmobMsg message) {
		Message handlerMsg = handler.obtainMessage(NEW_MESSAGE);
		handlerMsg.obj = message;
		handler.sendMessage(handlerMsg);
	}

	@Override
	public void onNetChange(boolean isNetConnected) {
		if (!isNetConnected) {
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
	public void onReaded(String conversionId, String msgTime) {
		// 此处应该过滤掉不是和当前用户的聊天的回执消息界面的刷新
		if (conversionId.split("&")[1].equals(userInfo.getObjectId())) {
			// 修改界面上指定消息的阅读状态
			for (BmobMsg msg : mAdapter.getList()) {
				if (msg.getConversationId().equals(conversionId)
						&& msg.getMsgTime().equals(msgTime)) {
					msg.setStatus(BmobConfig.STATUS_SEND_RECEIVERED);
				}
				mAdapter.notifyDataSetChanged();
			}
		}
	}
}
