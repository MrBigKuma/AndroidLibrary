package com.seed.AndroidLibrary.ImageLoadingManager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.seed.AndroidLibrary.R;

public class AsyncBitmapLoader {
    /**
     * @param context
     * @param res
     * @param imagePath
     * @param imageView
     * @param bmDownloader
     */
    public static void loadBitmapInternet(Resources res,
                                          String imagePath, ImageView imageView, BitmapDownloader bmDownloader, AsyncBitmapCallback callBack) {
        if (cancelPotentialWork(imagePath, imageView)) {
            final BitmapWorkerTaskInternet task = new BitmapWorkerTaskInternet(
                    bmDownloader, imageView, callBack);
            task.scaleType = imageView.getScaleType();
            Bitmap bm = BitmapFactory.decodeResource(res,
                    R.drawable.ic_loading);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(res, bm,
                    task);
            imageView.setImageDrawable(asyncDrawable);
            startLoadingSpinner(imageView);
            task.execute(imagePath);
        }
    }

    public static void loadBitmapStorage(Resources res, String imagePath,
                                         ImageView imageView, AsyncBitmapCallback callBack) {
        if (cancelPotentialWork(imagePath, imageView)) {
            final BitmapWorkerTaskStorage task = new BitmapWorkerTaskStorage(
                    imageView, callBack);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(res, null,
                    task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(imagePath);
        }
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private static boolean cancelPotentialWork(String imagePath,
                                               ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapWorkerTask.imagePath == null
                    || !imagePath.equals(bitmapWorkerTask.imagePath)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    private static void startLoadingSpinner(ImageView imageView) {
        Animation rotate = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setDuration(1000);
        imageView.setScaleType(ScaleType.CENTER);
        imageView.startAnimation(rotate);
    }
}
