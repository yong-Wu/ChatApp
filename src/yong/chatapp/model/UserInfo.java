package yong.chatapp.model;

import cn.bmob.im.bean.BmobChatUser;

public class UserInfo extends BmobChatUser {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 性别-true-男
	 */
	private boolean sex;
	
	
	
	public boolean getSex() {
		return sex;
	}
	
	public void setSex(boolean sex) {
		this.sex = sex;
	}
}
