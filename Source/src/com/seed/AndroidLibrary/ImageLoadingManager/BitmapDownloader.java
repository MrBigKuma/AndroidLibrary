package com.seed.AndroidLibrary.ImageLoadingManager;

/**
 * Created by huy on 7/3/14.
 */

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.IllegalFormatException;

public interface BitmapDownloader {
    Bitmap getBitmap(String url) throws IllegalFormatException, IOException;
}
