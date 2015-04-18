package yong.chatapp.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;

import yong.chatapp.ChatApplication;
import yong.chatapp.R;
import yong.chatapp.model.UserInfo;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends BaseAdapter {

	private Context context;
	private List<UserInfo> userList;
	
	public ContactAdapter(Context ct, List<UserInfo> datas){
		context = ct;
		userList = datas;
	}
	
	@Override
	public int getCount() {
		return userList.size();
	}

	@Override
	public Object getItem(int position) {
		return userList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_contact, null);
			
			viewHolder = new ViewHolder();
			viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			viewHolder.name = (TextView) convertView.findViewById(R.id.name);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		UserInfo userInfo = userList.get(position);
		viewHolder.name.setText(userInfo.getUsername());
		
		viewHolder.avatar.setImageResource(R.drawable.ic_default_avatar);
		if (!TextUtils.isEmpty(userInfo.getAvatar())){
			ImageLoader.getInstance().displayImage(userInfo.getAvatar(),
					viewHolder.avatar, 
					ChatApplication.getInstance().getAvatarDisplayOptions());
		}
		
		return convertView;
	}

	static class ViewHolder{
		ImageView avatar;
		TextView name;
	}
}
