package yong.chatapp.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;

import yong.chatapp.ChatApplication;
import yong.chatapp.R;
import yong.chatapp.util.CollectionUtils;
import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobInvitation;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.db.BmobDB;
import cn.bmob.v3.listener.UpdateListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NewFriendAdapter extends BaseAdapter {

	Context context;
	List<BmobInvitation> list;
	
	public NewFriendAdapter(Context context, List<BmobInvitation> list) {
		this.context = context;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.item_newfriend, null);
			
			viewHolder = new ViewHolder();
			viewHolder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
			viewHolder.name = (TextView) convertView.findViewById(R.id.name);
			viewHolder.agree = (Button) convertView.findViewById(R.id.agree_btn);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder)convertView.getTag();
		}
		
		final BmobInvitation msg = list.get(position);
		
		int status = msg.getStatus();
		if(status==BmobConfig.INVITE_ADD_NO_VALIDATION||status==BmobConfig.INVITE_ADD_NO_VALI_RECEIVED){
			viewHolder.agree.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					final ProgressDialog progress = new ProgressDialog(context);
					progress.setMessage("正在添加...");
					progress.setCanceledOnTouchOutside(false);
					progress.show();
					
					try {
						BmobUserManager.getInstance(context).agreeAddContact(msg, new UpdateListener() {

							@Override
							public void onFailure(int errorCode, String errorMsg) {
								progress.dismiss();
								Toast.makeText(context, "添加失败: " + errorMsg, Toast.LENGTH_LONG).show();
							}

							@Override
							public void onSuccess() {
								progress.dismiss();
								viewHolder.agree.setText("已同意");
								viewHolder.agree.setEnabled(false);
								viewHolder.agree.setBackgroundColor(context.getResources().getColor(R.color.white));
								viewHolder.agree.setTextColor(context.getResources().getColor(R.color.gray));
								//保存到application中方便比较 
								ChatApplication.getInstance().setContactList(CollectionUtils.list2map(BmobDB.create(context).getContactList()));
							}
						});
					} catch (final Exception e) {
							progress.dismiss();
							Toast.makeText(context, "添加失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			});
		}else if(status==BmobConfig.INVITE_ADD_AGREE){
			viewHolder.agree.setText("已同意");
			viewHolder.agree.setEnabled(false);
			viewHolder.agree.setBackgroundColor(context.getResources().getColor(R.color.white));
			viewHolder.agree.setTextColor(context.getResources().getColor(R.color.gray));
		}
		
		viewHolder.name.setText(msg.getFromname());
		viewHolder.avatar.setImageResource(R.drawable.ic_default_avatar);
		if (!TextUtils.isEmpty(msg.getAvatar())){
			ImageLoader.getInstance().displayImage(msg.getAvatar(),
					viewHolder.avatar, 
					ChatApplication.getInstance().getAvatarDisplayOptions());
		}
		
		return convertView;
	}

	static class ViewHolder{
		ImageView avatar;
		TextView name;
		Button agree;
	}
}
