package yong.chatapp.adapter;

import java.util.List;

import yong.chatapp.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class EmotionAdapter extends BaseAdapter {

	Context context;
	List<String> datas;
	
	public EmotionAdapter(Context context, List<String> datas) {
		this.context = context;
		this.datas = datas;
	}
	
	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_emotion, null);
			holder = new ViewHolder();
			holder.mImage = (ImageView) convertView.findViewById(R.id.emotion);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String faceText = (String) getItem(position);
		String key = faceText.substring(1);
		Drawable drawable =context.getResources().getDrawable(context.getResources().getIdentifier(key, "drawable", context.getPackageName()));
		holder.mImage.setImageDrawable(drawable);
		
		return  convertView;
	}

	class ViewHolder {
		ImageView mImage;
	}
}
