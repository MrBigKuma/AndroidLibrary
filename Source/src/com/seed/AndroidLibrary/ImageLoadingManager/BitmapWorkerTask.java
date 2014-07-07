package com.seed.AndroidLibrary.ImageLoadingManager;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import java.lang.ref.WeakReference;

abstract class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
	private final WeakReference<ImageView> imageViewReference;
	public String imagePath;
	public ScaleType scaleType = null;
    private AsyncBitmapCallback mCallBack = null;

	public BitmapWorkerTask(ImageView imageView, AsyncBitmapCallback callBack) {
		// Use a WeakReference to ensure the ImageView can be garbage collected
		imageViewReference = new WeakReference<ImageView>(imageView);
        mCallBack = callBack;
	}

	// Once complete, see if ImageView is still around and set bitmap.
	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (imageViewReference != null && bitmap != null) {
			final ImageView imageView = imageViewReference.get();
			if (imageView != null) {
				if (scaleType != null) {
					imageView.setScaleType(scaleType);
				}
				imageView.clearAnimation();
				imageView.setImageBitmap(bitmap);
			}
		}
        if(mCallBack!=null){
            mCallBack.callBack();
        }
	}
}
