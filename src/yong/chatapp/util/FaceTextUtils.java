package yong.chatapp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;

public class FaceTextUtils {

	public static List<String> faceTexts = new ArrayList<String>();
	static {
		faceTexts.add(new String("\\ue056"));
		faceTexts.add(new String("\\ue057"));
		faceTexts.add(new String("\\ue058"));
		faceTexts.add(new String("\\ue059"));
		faceTexts.add(new String("\\ue105"));
		faceTexts.add(new String("\\ue106"));
		faceTexts.add(new String("\\ue107"));
		faceTexts.add(new String("\\ue108"));
		faceTexts.add(new String("\\ue401"));
		faceTexts.add(new String("\\ue402"));
		faceTexts.add(new String("\\ue403"));
		faceTexts.add(new String("\\ue404"));
		faceTexts.add(new String("\\ue405"));
		faceTexts.add(new String("\\ue406"));
		faceTexts.add(new String("\\ue407"));
		faceTexts.add(new String("\\ue408"));
		faceTexts.add(new String("\\ue409"));
		faceTexts.add(new String("\\ue40a"));
		faceTexts.add(new String("\\ue40b"));
		faceTexts.add(new String("\\ue40d"));
		faceTexts.add(new String("\\ue40e"));
		faceTexts.add(new String("\\ue40f"));
		faceTexts.add(new String("\\ue410"));
		faceTexts.add(new String("\\ue411"));
		faceTexts.add(new String("\\ue412"));
		faceTexts.add(new String("\\ue413"));
		faceTexts.add(new String("\\ue414"));
		faceTexts.add(new String("\\ue415"));
		faceTexts.add(new String("\\ue416"));
		faceTexts.add(new String("\\ue417"));
		faceTexts.add(new String("\\ue418"));
		faceTexts.add(new String("\\ue41f"));
		faceTexts.add(new String("\\ue00e"));
		faceTexts.add(new String("\\ue421"));
	}

	public static String parse(String s) {
		for (String faceText : faceTexts) {
			s = s.replace("\\" + faceText, faceText);
			s = s.replace(faceText, "\\" + faceText);
		}
		return s;
	}

	/** 
	  * toSpannableString
	  * @return SpannableString
	  * @throws
	  */
	public static SpannableString toSpannableString(Context context, String text) {
		if (!TextUtils.isEmpty(text)) {
			SpannableString spannableString = new SpannableString(text);
			int start = 0;
			Pattern pattern = Pattern.compile("\\\\ue[a-z0-9]{3}", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				String faceText = matcher.group();
				String key = faceText.substring(1);
				BitmapFactory.Options options = new BitmapFactory.Options();
				Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
						context.getResources().getIdentifier(key, "drawable", context.getPackageName()), options);
				ImageSpan imageSpan = new ImageSpan(context, bitmap);
				int startIndex = text.indexOf(faceText, start);
				int endIndex = startIndex + faceText.length();
				if (startIndex >= 0)
					spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				start = (endIndex - 1);
			}

			return spannableString;
		} else {
			return new SpannableString("");
		}
	}

	public static SpannableString toSpannableString(Context context, String text, SpannableString spannableString) {

		int start = 0;
		Pattern pattern = Pattern.compile("\\\\ue[a-z0-9]{3}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String faceText = matcher.group();
			String key = faceText.substring(1);
			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.inSampleSize = 2;
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), context.getResources()
					.getIdentifier(key, "drawable", context.getPackageName()), options);
			ImageSpan imageSpan = new ImageSpan(context, bitmap);
			int startIndex = text.indexOf(faceText, start);
			int endIndex = startIndex + faceText.length();
			if (startIndex >= 0)
				spannableString.setSpan(imageSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			start = (endIndex - 1);
		}

		return spannableString;
	}

}
