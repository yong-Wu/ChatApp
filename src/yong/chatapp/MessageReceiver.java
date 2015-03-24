package yong.chatapp;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import yong.chatapp.activity.MainActivity;
import yong.chatapp.activity.NewFriendsActivity;
import yong.chatapp.util.CollectionUtils;
import yong.chatapp.util.NetworkUtils;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobNotifyManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.config.BmobConstant;
import cn.bmob.im.db.BmobDB;
import cn.bmob.im.inteface.EventListener;
import cn.bmob.im.inteface.OnReceiveListener;
import cn.bmob.im.util.BmobJsonUtil;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.listener.FindListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class MessageReceiver extends BroadcastReceiver {
	// 事件监听
	public static ArrayList<EventListener> eventListenerList = new ArrayList<EventListener>();
		
	public static int mPushMsgNum = 0;
	public static final int NOTIFY_ID = 0;
	
	BmobUserManager userManager;
	BmobChatUser currentUser;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		String jsonString = intent.getStringExtra("msg");
		
		userManager = BmobUserManager.getInstance(context);
		currentUser = userManager.getCurrentUser();
		
		if (NetworkUtils.isNetworkAvailable(context)) {
			parseMsg(context, jsonString);
		}else{
			for (int i = 0; i < eventListenerList.size(); i++)
				((EventListener) eventListenerList.get(i)).onNetChange(false);
		}
	}

	private void parseMsg(final Context context, String jsonString) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonString);
			String tag = BmobJsonUtil.getString(jsonObject, BmobConstant.PUSH_KEY_TAG);
			
			if (tag.equals(BmobConfig.TAG_OFFLINE)){
				if (currentUser != null){
					if (eventListenerList.size() > 0){
						for (EventListener eventListener : eventListenerList){
							eventListener.onOffline();
						}
					} else {
						ChatApplication.getInstance().logout();
					}
				}
			} else {
				String fromId = BmobJsonUtil.getString(jsonObject, BmobConstant.PUSH_KEY_TARGETID);
				final String toId = BmobJsonUtil.getString(jsonObject, BmobConstant.PUSH_KEY_TOID);
				String msgTime = BmobJsonUtil.getString(jsonObject, BmobConstant.PUSH_READED_MSGTIME);
				
				if(fromId!=null && !BmobDB.create(context,toId).isBlackUser(fromId)){//该消息发送方不为黑名单用户
					if(TextUtils.isEmpty(tag)){
						
						BmobChatManager.getInstance(context).createReceiveMsg(jsonString, new OnReceiveListener() {
							
							@Override
							public void onSuccess(BmobMsg msg) {
								if (eventListenerList.size() > 0){
									for (int i = 0; i < eventListenerList.size(); i++) {
										((EventListener) eventListenerList.get(i)).onMessage(msg);
									}
								} else {
									boolean isAllowPushNotify = ChatApplication.getInstance().getSharePreferenceUtils().isAllowPushNotify();
									if(isAllowPushNotify && currentUser != null && currentUser.getObjectId().equals(toId)){
										mPushMsgNum++;
										showMsgNotify(context,msg);
									}
								}
							}
							
							@Override
							public void onFailure(int errorCode, String errorString) {
								BmobLog.i("获取接收的消息失败：" + errorString);
							}
						});
						
					} else {  //Tag 不为空
						
						if(tag.equals(BmobConfig.TAG_ADD_CONTACT)){ // 添加好友的请求
							
							BmobInvitation message = BmobChatManager.getInstance(context).saveReceiveInvite(jsonString, toId);
							if(currentUser!=null){
								if(toId.equals(currentUser.getObjectId())){
									if (eventListenerList.size() > 0) {
										for (EventListener eventListener : eventListenerList)
											eventListener.onAddUser(message);
									} else {
										showOtherNotify(context, message.getFromname(), toId,  message.getFromname()+"请求添加好友", NewFriendsActivity.class);
									}
								}
							}
							
						} else if (tag.equals(BmobConfig.TAG_ADD_AGREE)) { //同意添加好友
							
							String username = BmobJsonUtil.getString(jsonObject, BmobConstant.PUSH_KEY_TARGETUSERNAME);
							BmobUserManager.getInstance(context).addContactAfterAgree(username, new FindListener<BmobChatUser>() {

								@Override
								public void onError(int arg0, String arg1) {
									
								}

								@Override
								public void onSuccess(List<BmobChatUser> arg0) {
									//新增的好友加入内存里的好友列表
									ChatApplication.getInstance().setContactList(CollectionUtils.list2map(BmobDB.create(context).getContactList()));
								}
							});
							
							//通知栏通知，并在聊天界面创建一个临时验证会话
							showOtherNotify(context, username, toId,  username+"同意添加您为好友", MainActivity.class);
							BmobMsg.createAndSaveRecentAfterAgree(context, jsonString);
						
						} else if (tag.equals(BmobConfig.TAG_READED)) { //消息已读
							
							String conversionId = BmobJsonUtil.getString(jsonObject, BmobConstant.PUSH_READED_CONVERSIONID);
							if (currentUser != null){
								BmobChatManager.getInstance(context).updateMsgStatus(conversionId, msgTime);
								if(toId.equals(currentUser.getObjectId())){
									if (eventListenerList.size() > 0) {
										for (EventListener eventListener : eventListenerList)
											eventListener.onReaded(conversionId, msgTime);
									}
								}
							}
							
						}
						
					}
				} else{ //黑名单用户，所有的消息都应该置为已读
					BmobChatManager.getInstance(context).updateMsgReaded(true, fromId, msgTime);
					BmobLog.i("该消息发送方为黑名单用户");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showMsgNotify(Context context, BmobMsg msg) {
		// 更新通知栏
		int icon = R.drawable.ic_launcher;
		String trueMsg = "";
		if(msg.getMsgType()==BmobConfig.TYPE_TEXT && msg.getContent().contains("\\ue")){
			trueMsg = "[表情]";
		}else if(msg.getMsgType()==BmobConfig.TYPE_IMAGE){
			trueMsg = "[图片]";
		}else if(msg.getMsgType()==BmobConfig.TYPE_VOICE){
			trueMsg = "[语音]";
		}else if(msg.getMsgType()==BmobConfig.TYPE_LOCATION){
			trueMsg = "[位置]";
		}else{
			trueMsg = msg.getContent();
		}
		CharSequence tickerText = msg.getBelongUsername() + ":" + trueMsg;
		String contentTitle = msg.getBelongUsername()+ " (" + mPushMsgNum + "条新消息)";
		
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		boolean isAllowVoice = ChatApplication.getInstance().getSharePreferenceUtils().isAllowVoice();
		boolean isAllowVibrate = ChatApplication.getInstance().getSharePreferenceUtils().isAllowVibrate();
		
		BmobNotifyManager.getInstance(context).showNotifyWithExtras(isAllowVoice, isAllowVibrate,
				icon, tickerText.toString(), contentTitle, tickerText.toString(), intent);
	}

	public void showOtherNotify(Context context,String username,String toId,String ticker,Class<?> cls){
		boolean isAllow = ChatApplication.getInstance().getSharePreferenceUtils().isAllowPushNotify();
		boolean isAllowVoice = ChatApplication.getInstance().getSharePreferenceUtils().isAllowVoice();
		boolean isAllowVibrate = ChatApplication.getInstance().getSharePreferenceUtils().isAllowVibrate();
		if(isAllow && currentUser!=null && currentUser.getObjectId().equals(toId)){
			//同时提醒通知
			BmobNotifyManager.getInstance(context).showNotify(isAllowVoice, isAllowVibrate, 
					R.drawable.ic_launcher, ticker,username, ticker.toString(), NewFriendsActivity.class);
		}
	}
}
