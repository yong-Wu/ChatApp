package yong.chatapp.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import yong.chatapp.ChatApplication;
import yong.chatapp.R;
import yong.chatapp.model.UserInfo;
import yong.chatapp.util.FaceTextUtils;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;
import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter {

	Context context;
	List<BmobMsg> msgList;
	
	private final int TYPE_RECEIVER_TXT = 0;
	private final int TYPE_SEND_TXT = 1;
	private final int TYPE_RECEIVE_IMAGE = 2;
	private final int TYPE_SEND_IMAGE = 3;
	
	String currentUserId;
	
	DisplayImageOptions imageOptions;
	
	public ChatListAdapter(Context context,List<BmobMsg> msgList) {
		this.context = context;
		this.msgList = msgList;
		
		currentUserId = BmobUserManager.getInstance(context).getCurrentUserObjectId();
		
		imageOptions = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.ic_launcher)
		.showImageOnFail(R.drawable.ic_launcher)
		.resetViewBeforeLoading(true)
		.cacheOnDisk(true)
		.cacheInMemory(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(300))
		.build();
	}
	
	@Override
	public int getItemViewType(int position) {
		BmobMsg msg = msgList.get(position);
		if (msg.getMsgType() == BmobConfig.TYPE_TEXT) {
			return msg.getBelongId().equals(currentUserId) ? TYPE_SEND_TXT : TYPE_RECEIVER_TXT;
		} else if (msg.getMsgType() == BmobConfig.TYPE_IMAGE) {
			return msg.getBelongId().equals(currentUserId) ? TYPE_SEND_IMAGE : TYPE_RECEIVE_IMAGE; 
		}{
			return -1;
		}
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	private View createViewByType(BmobMsg message, int position) {
		int type = message.getMsgType();
		if (type == BmobConfig.TYPE_TEXT){
			return getItemViewType(position) == TYPE_RECEIVER_TXT ? 
					LayoutInflater.from(context).inflate(R.layout.item_chat_received_text, null) 
					:
					LayoutInflater.from(context).inflate(R.layout.item_chat_sent_text, null);
		}else if (type == BmobConfig.TYPE_IMAGE) {
			return getItemViewType(position) == TYPE_RECEIVE_IMAGE ?
					LayoutInflater.from(context).inflate(R.layout.item_chat_received_image, null)
					:
					LayoutInflater.from(context).inflate(R.layout.item_chat_send_image, null);
		}{
			return null;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BmobMsg message = msgList.get(position);
		
		if (convertView == null) {
			convertView = createViewByType(message, position);
		}
		
		final ImageView iv_avatar = ViewHolder.get(convertView, R.id.iv_avatar);
		ImageView iv_fail_resend = ViewHolder.get(convertView, R.id.iv_fail_resend);//失败重发
		TextView tv_send_status = ViewHolder.get(convertView, R.id.tv_send_status);//发送状态
		TextView tv_time = ViewHolder.get(convertView, R.id.tv_time);
		TextView tv_message = ViewHolder.get(convertView, R.id.tv_message);
		ImageView iv_picture = ViewHolder.get(convertView, R.id.iv_picture);
		final ProgressBar progress_load = ViewHolder.get(convertView, R.id.progress_load);//进度条

		if (message.getBelongId().equals(currentUserId)) {
			iv_avatar.setImageResource(R.drawable.ic_default_avatar);
			BmobChatUser user = BmobUserManager.getInstance(context).getCurrentUser();
			if (!TextUtils.isEmpty(user.getAvatar())){
				ImageLoader.getInstance().displayImage(user.getAvatar(),
						iv_avatar, 
						ChatApplication.getInstance().getAvatarDisplayOptions());
			}
		}else {
			BmobChatUser user = ChatApplication.getInstance().getContactList().get(message.getBelongUsername());
			if (!TextUtils.isEmpty(user.getAvatar())){
				ImageLoader.getInstance().displayImage(user.getAvatar(),
						iv_avatar, 
						ChatApplication.getInstance().getAvatarDisplayOptions());
			}
			
//			new BmobQuery<UserInfo>().getObject(context, message.getBelongId(), new GetListener<UserInfo>() {
//				
//				@Override
//				public void onFailure(int arg0, String arg1) {
//					
//				}
//	
//				@Override
//				public void onSuccess(UserInfo user) {
//					iv_avatar.setImageResource(R.drawable.ic_default_avatar);
//					if (!TextUtils.isEmpty(user.getAvatar())){
//						ImageLoader.getInstance().displayImage(user.getAvatar(),
//								iv_avatar, 
//								ChatApplication.getInstance().getAvatarDisplayOptions());
//					}
//				}
//			});
		}
			
		if(getItemViewType(position)==TYPE_SEND_TXT){
			if(message.getStatus()==BmobConfig.STATUS_SEND_SUCCESS){
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("已发送");
			}else if(message.getStatus()==BmobConfig.STATUS_SEND_FAIL){
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.VISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
			}else if(message.getStatus()==BmobConfig.STATUS_SEND_RECEIVERED){
				progress_load.setVisibility(View.INVISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.VISIBLE);
				tv_send_status.setText("已阅读");
			}else if(message.getStatus()==BmobConfig.STATUS_SEND_START){
				progress_load.setVisibility(View.VISIBLE);
				iv_fail_resend.setVisibility(View.INVISIBLE);
				tv_send_status.setVisibility(View.INVISIBLE);
			}
		}
		
		final String text = message.getContent();
		switch (message.getMsgType()) {
		case BmobConfig.TYPE_TEXT:
			try {
				SpannableString spannableString = FaceTextUtils
						.toSpannableString(context, text);
				tv_message.setText(spannableString);
			} catch (Exception e) {
			}
			break;
			
		case BmobConfig.TYPE_IMAGE:
			try {
				if (!TextUtils.isEmpty(text)){
					if(getItemViewType(position)==TYPE_SEND_IMAGE){//发送的消息
						if(message.getStatus()==BmobConfig.STATUS_SEND_START){
							progress_load.setVisibility(View.VISIBLE);
							iv_fail_resend.setVisibility(View.INVISIBLE);
							tv_send_status.setVisibility(View.INVISIBLE);
						}else if(message.getStatus()==BmobConfig.STATUS_SEND_SUCCESS){
							progress_load.setVisibility(View.INVISIBLE);
							iv_fail_resend.setVisibility(View.INVISIBLE);
							tv_send_status.setVisibility(View.VISIBLE);
							tv_send_status.setText("已发送");
						}else if(message.getStatus()==BmobConfig.STATUS_SEND_FAIL){
							progress_load.setVisibility(View.INVISIBLE);
							iv_fail_resend.setVisibility(View.VISIBLE);
							tv_send_status.setVisibility(View.INVISIBLE);
						}else if(message.getStatus()==BmobConfig.STATUS_SEND_RECEIVERED){
							progress_load.setVisibility(View.INVISIBLE);
							iv_fail_resend.setVisibility(View.INVISIBLE);
							tv_send_status.setVisibility(View.VISIBLE);
							tv_send_status.setText("已阅读");
						}
//						如果是发送的图片的话，因为开始发送存储的地址是本地地址，发送成功之后存储的是本地地址+"&"+网络地址，因此需要判断下
						String showUrl = "";
						if(text.contains("&")){
							showUrl = text.split("&")[0];
						}else{
							showUrl = text;
						}
						//为了方便每次都是取本地图片显示
						ImageLoader.getInstance().displayImage(showUrl, iv_picture);
					}else{
						ImageLoader.getInstance().displayImage(text, iv_picture, imageOptions, new ImageLoadingListener() {
							
							@Override
							public void onLoadingStarted(String imageUri, View view) {
								progress_load.setVisibility(View.VISIBLE);
							}
							
							@Override
							public void onLoadingFailed(String imageUri, View view,
									FailReason failReason) {
								progress_load.setVisibility(View.INVISIBLE);
							}
							
							@Override
							public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
								progress_load.setVisibility(View.INVISIBLE);
							}
							
							@Override
							public void onLoadingCancelled(String imageUri, View view) {
								progress_load.setVisibility(View.INVISIBLE);
							}
						});
					}
				}
			} catch (Exception e) {
			}
			break;
		}
		
		return convertView;
	}
	
	public void add(BmobMsg msg) {
		msgList.add(msg);
		notifyDataSetChanged();
	}
	
	public void setList(List<BmobMsg> list) {
		this.msgList = list;
		notifyDataSetChanged();
	}
	
	public List<BmobMsg> getList(){
		return msgList;
	}
	
	@Override
	public int getCount() {
		return msgList.size();
	}

	@Override
	public Object getItem(int position) {
		return msgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


}
