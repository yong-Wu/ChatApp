package yong.chatapp.adapter;

import java.util.List;
import yong.chatapp.R;
import yong.chatapp.util.FaceTextUtils;
import cn.bmob.im.BmobChatManager;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import android.content.Context;
import android.text.SpannableString;
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
	
	String currentUserId;
	
	public ChatListAdapter(Context context,List<BmobMsg> msgList) {
		this.context = context;
		this.msgList = msgList;
		
		currentUserId = BmobUserManager.getInstance(context).getCurrentUserObjectId();
	}
	
	@Override
	public int getItemViewType(int position) {
		BmobMsg msg = msgList.get(position);
		if (msg.getMsgType() == BmobConfig.TYPE_TEXT) {
			return msg.getBelongId().equals(currentUserId) ? TYPE_SEND_TXT: TYPE_RECEIVER_TXT;
		} else {
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
		}else {
			return null;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BmobMsg message = msgList.get(position);
		
		if (convertView == null) {
			convertView = createViewByType(message, position);
		}
		
		ImageView iv_avatar = ViewHolder.get(convertView, R.id.iv_avatar);
		ImageView iv_fail_resend = ViewHolder.get(convertView, R.id.iv_fail_resend);//失败重发
		TextView tv_send_status = ViewHolder.get(convertView, R.id.tv_send_status);//发送状态
		TextView tv_time = ViewHolder.get(convertView, R.id.tv_time);
		TextView tv_message = ViewHolder.get(convertView, R.id.tv_message);
		ProgressBar progress_load = ViewHolder.get(convertView, R.id.progress_load);//进度条

		
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
