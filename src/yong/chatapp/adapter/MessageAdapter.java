package yong.chatapp.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;

import yong.chatapp.ChatApplication;
import yong.chatapp.R;
import yong.chatapp.model.UserInfo;
import yong.chatapp.util.FaceTextUtils;
import yong.chatapp.util.TimeUtil;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.im.bean.BmobRecent;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.GetListener;
import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<BmobRecent> implements Filterable{

	private LayoutInflater inflater;
	private List<BmobRecent> mData;
	private Context mContext;
	
	public MessageAdapter(Context context, int resource, List<BmobRecent> objects) {
		super(context, resource, objects);
		inflater = LayoutInflater.from(context);
		this.mContext = context;
		mData = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final BmobRecent item = mData.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_conversation, parent, false);
		}
		final ImageView iv_recent_avatar = ViewHolder.get(convertView, R.id.iv_recent_avatar);
		TextView tv_recent_name = ViewHolder.get(convertView, R.id.tv_recent_name);
		TextView tv_recent_msg = ViewHolder.get(convertView, R.id.tv_recent_msg);
		TextView tv_recent_time = ViewHolder.get(convertView, R.id.tv_recent_time);
		TextView tv_recent_unread = ViewHolder.get(convertView, R.id.tv_recent_unread);
		
		tv_recent_name.setText(item.getUserName());
		tv_recent_time.setText(TimeUtil.getChatTime(item.getTime()));
		
		//显示内容
		if(item.getType()==BmobConfig.TYPE_TEXT){
			SpannableString spannableString = FaceTextUtils.toSpannableString(mContext, item.getMessage());
			tv_recent_msg.setText(spannableString);
		} else if(item.getType()==BmobConfig.TYPE_IMAGE){
			tv_recent_msg.setText("[图片]");
		}
		
		int num = BmobDB.create(mContext).getUnreadCount(item.getTargetid());
		if (num > 0) {
			tv_recent_unread.setVisibility(View.VISIBLE);
			tv_recent_unread.setText(num + "");
		} else {
			tv_recent_unread.setVisibility(View.GONE);
		}
		
		BmobChatUser user = ChatApplication.getInstance().getContactList().get(item.getUserName());
		if (!TextUtils.isEmpty(user.getAvatar())){
			ImageLoader.getInstance().displayImage(user.getAvatar(),
					iv_recent_avatar, 
					ChatApplication.getInstance().getAvatarDisplayOptions());
		} else {
			iv_recent_avatar.setImageResource(R.drawable.ic_default_avatar);
		}
		
//		new BmobQuery<UserInfo>().getObject(mContext, item.getTargetid(), new GetListener<UserInfo>() {
//			
//			public void onSuccess(UserInfo user) {
//				if (!TextUtils.isEmpty(user.getAvatar())){
//					ImageLoader.getInstance().displayImage(user.getAvatar(),
//							iv_recent_avatar, 
//							ChatApplication.getInstance().getAvatarDisplayOptions());
//				} else {
//					iv_recent_avatar.setImageResource(R.drawable.ic_default_avatar);
//				}
//			}
//			
//			@Override
//			public void onFailure(int arg0, String msg) {
//				
//			}
//		});
		
		return convertView;
	}
}
