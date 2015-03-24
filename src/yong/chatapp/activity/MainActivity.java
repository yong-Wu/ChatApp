package yong.chatapp.activity;

import yong.chatapp.R;
import yong.chatapp.fragment.ContactFragment;
import yong.chatapp.fragment.MessageFragment;
import yong.chatapp.fragment.SettingFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends ActivityBase {

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
		
		initView();
		initTabs();
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
			transaction.show(fragments[index]).commit();
			
			titleTextView.setText(titles[index]);
			tabButtons[index].setSelected(true);
			tabButtons[currentTabIndex].setSelected(false);
			currentTabIndex = index;
		}	
	}
}
