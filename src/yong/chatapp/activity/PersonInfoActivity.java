package yong.chatapp.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import com.nostra13.universalimageloader.core.ImageLoader;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import yong.chatapp.ChatApplication;
import yong.chatapp.R;
import yong.chatapp.model.UserInfo;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PersonInfoActivity extends ActivityBase {

	private ImageView avatarImageView;
	private TextView usernameTextView;
	private TextView genderTextView;
	private String username;
	private String source;
	private Button beginChat;
	private Button deleteFriend;
	
	private UserInfo userInfo;
	
	private static final int PHOTO_REQUEST_TAKEPHOTO = 1;	// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;	  	// 相册
	private static final int PHOTO_REQUEST_CUT = 3; 		//裁剪
	
	File tempFile = new File(Environment.getExternalStorageDirectory(), getPhotoFileName());
	String avatarDir = Environment.getExternalStorageDirectory() + "/avatar/";
	String avatarPath = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_person_info);
		source = getIntent().getStringExtra("from");
		
		initView();
		initData();
	}

	private void initView() {
		findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		avatarImageView = (ImageView)findViewById(R.id.avatar);
		usernameTextView = (TextView)findViewById(R.id.username);
		genderTextView = (TextView)findViewById(R.id.gender);
		
		beginChat = (Button)findViewById(R.id.begin_chat);
		deleteFriend = (Button)findViewById(R.id.delete_friend);
		
		if (source.equals("me")){
			avatarImageView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					showDialog();
				}
			});
		}
	}

	private void showDialog() {
		new AlertDialog.Builder(this)
		.setTitle("设置头像")
		.setPositiveButton("拍照", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
				startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
			}
		})
		.setNegativeButton("相册", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
			}
		}).show();
	}

	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}
	
	private void initData() {
		username = getIntent().getStringExtra("username");
		usernameTextView.setText(username);
		
		userManager.queryUser(username, new FindListener<UserInfo>() {

			@Override
			public void onError(int errorCode, String errorMsg) {
				ShowToast(errorMsg);
			}

			@Override
			public void onSuccess(List<UserInfo> users) {
				if (users != null && users.size() > 0){
					userInfo = users.get(0);
					updateUserInfo(userInfo);
					
					if (source.equals("other")){
						beginChat.setVisibility(View.VISIBLE);
						deleteFriend.setVisibility(View.VISIBLE);
						
						beginChat.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								Intent intent = new Intent(PersonInfoActivity.this, ChatActivity.class);
								intent.putExtra("user", userInfo);
								startActivity(intent);
								finish();
							}
						});
						
						deleteFriend.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View arg0) {
								new AlertDialog.Builder(PersonInfoActivity.this)
								.setMessage("确定要删除该好友吗？")
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										final ProgressDialog progress = new ProgressDialog(PersonInfoActivity.this);
										progress.setMessage("正在删除...");
										progress.setCanceledOnTouchOutside(false);
										progress.show();
										userManager.deleteContact(userInfo.getObjectId(), new UpdateListener() {

											@Override
											public void onFailure(int arg0, String arg1) {
												ShowToast("删除失败："+arg1);									
											}

											@Override
											public void onSuccess() {
												ShowToast("删除成功");
												//删除内存
												ChatApplication.getInstance().getContactList().remove(userInfo.getUsername());
												progress.dismiss();
												finish();
											}
											
										});
									}
								})
								.setNegativeButton("取消", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int arg1) {
										dialog.cancel();
									}
								}).show();
							}
						});
					}
				} 
			}
		});
	}

	private void updateUserInfo(UserInfo userInfo) {
		usernameTextView.setText(userInfo.getUsername());
		genderTextView.setText(userInfo.getSex() ? "男" : "女");
		refreshAvatar(userInfo.getAvatar());
	}
	
	private void refreshAvatar(String avatar) {
		if (!TextUtils.isEmpty(avatar)) {
			ImageLoader.getInstance().displayImage(avatar, avatarImageView, ChatApplication.getInstance().getAvatarDisplayOptions());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
		case PHOTO_REQUEST_TAKEPHOTO:
			startPhotoZoom(Uri.fromFile(tempFile), 150);
			break;
	
		case PHOTO_REQUEST_GALLERY:
			if (data != null)
				startPhotoZoom(data.getData(), 150);
			break;
	
		case PHOTO_REQUEST_CUT:
			if (data != null) 
				setPicToViewAndSave(data);
			
			// 上传头像
			uploadAvatar();
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private void uploadAvatar() {
		final BmobFile bmobFile = new BmobFile(new File(avatarPath));
		bmobFile.upload(this, new UploadFileListener() {

			@Override
			public void onFailure(int arg0, String msg) {
				ShowToast("头像上传失败：" + msg);
			}

			@Override
			public void onSuccess() {
				final String url = bmobFile.getFileUrl(PersonInfoActivity.this);
				// 更新用户头像信息
				UserInfo  user =new UserInfo();
				user.setAvatar(url);
				
				user.update(PersonInfoActivity.this, userManager.getCurrentUserObjectId(),
						new UpdateListener() {
					@Override
					public void onSuccess() {
						ShowToast("头像更新成功！");
						// 更新头像
						refreshAvatar(url);
					}

					@Override
					public void onFailure(int code, String msg) {
						ShowToast("头像更新失败：" + msg);
					}
				});
			}
		});
	}

	private void startPhotoZoom(Uri uri, int size) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
	
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
	
		intent.putExtra("outputX", size);
		intent.putExtra("outputY", size);
		intent.putExtra("return-data", true);
	
		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}
	
	private void setPicToViewAndSave(Intent picdata) {
		Bundle bundle = picdata.getExtras();
		if (bundle != null) {
			Bitmap photo = bundle.getParcelable("data");
			avatarImageView.setImageBitmap(photo);
			
			savePhoto(photo);
		}
	}

	private void savePhoto(Bitmap bitmap) {

		String filename = getPhotoFileName();
		avatarPath = avatarDir + filename;
		
		File dir = new File(avatarDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		File file = new File(avatarDir, filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
				out.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
