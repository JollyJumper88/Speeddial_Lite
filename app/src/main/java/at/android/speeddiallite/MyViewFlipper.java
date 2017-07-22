package at.android.speeddiallite;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class MyViewFlipper extends ViewFlipper {

	public MyViewFlipper(Context context) {
		super(context);
	}

	public MyViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDetachedFromWindow() {
		try {
			super.onDetachedFromWindow();

		} catch (IllegalArgumentException e) {
			// System.out
			// .println("MyViewFlipper ignoring IllegalArgumentException");
			stopFlipping();

		} catch (Exception e) {
			// System.out
			// .println("MyViewFlipper ignoring Exception");
			stopFlipping();
		}
	}
}
