package com.seed.AndroidLibrary.HttpClient;

import java.io.IOException;
import java.util.List;

import android.app.DownloadManager.Request;
import android.graphics.Bitmap;

import com.seed.AndroidLibrary.Object.MyObject;

public interface MyApiClient {
	public boolean postObject(MyObject gcmToken)throws IOException,IllegalArgumentException;
	public List<MyObject> getphotoIds()throws IOException,IllegalArgumentException;
	public Bitmap getBitmap(String url)throws IOException,IllegalArgumentException;
	public Bitmap getPhoto(int photoId)throws IOException,IllegalArgumentException;
	public Request makeObjectRequest(MyObject pamphlet);
	public String getObjectUrl(int objId);
}
