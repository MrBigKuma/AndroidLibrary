package com.seed.AndroidLibrary.HttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.DownloadManager.Request;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.util.Log;
import com.google.gson.Gson;
import com.seed.AndroidLibrary.R;
import com.seed.AndroidLibrary.Object.MyObject;

public class MyApiClientImpl implements MyApiClient {
	public static final String TAG = "MyApiClientImpl";
	public static final boolean D = true;
	private static int mConnectionTimeout;
	private static int mReadTimeout;
	private static String mApiRootUrl;
	private static String mAppName;

	private Gson gson = MyGsonFactory.getInstance();
	private static MyApiClient sApiClient = null;

	public static MyApiClient getInstance(Context context) {
		return sApiClient == null ? sApiClient = new MyApiClientImpl(
				context) : sApiClient;
	}

	private MyApiClientImpl(Context context) {
		mApiRootUrl = context.getString(R.string.api_root_url);
		mConnectionTimeout = context.getResources().getInteger(
				R.integer.connection_timeout);
		mReadTimeout = context.getResources()
				.getInteger(R.integer.read_timeout);
		mAppName = context.getString(R.string.app_name);

		try {
			File httpCacheDir = new File(context.getCacheDir(), "http");
			long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
			HttpResponseCache.install(httpCacheDir, httpCacheSize);
		} catch (IOException e) {
			Log.i(TAG, "HTTP response cache installation failed:" + e);
		}
	}

	private HttpURLConnection createConnectionFromFullPath(String fullPath)
			throws IOException {
		URL url = new URL(fullPath);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(mConnectionTimeout);
		conn.setReadTimeout(mReadTimeout);
		conn.setUseCaches(true);
		return conn;
	}

	private HttpURLConnection createConnection(String path) throws IOException {
		return createConnectionFromFullPath(mApiRootUrl + path);
	}

	/**
	 * 
	 * @param url
	 * @param clazz
	 *            resource type
	 * @return Depends on the returned status code, 200: an instance of clazz,
	 *         404: null, other 4xx: IllegalArgumentException, 5xx: IOException
	 *         and the others: RuntimeException.
	 * @throws IOException
	 *             when having error in connection
	 */
	private <T> T getResource(String url, Class<T> clazz) throws IOException,
			IllegalArgumentException {
		HttpURLConnection conn = createConnection(url);
		Date startReq = new Date();
		conn.setRequestMethod("GET");
		BufferedReader br = null;
		try {
			br = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			int responseCode = conn.getResponseCode();
			if (responseCode >= 200 && responseCode <= 299) {
				// if(url.contains("2013"))
				T resource = gson.fromJson(br, clazz);
				return resource;
			} else if (responseCode >= 400 && responseCode <= 499) {
				if (responseCode == 404) {
					return null;
				}
				throw new IllegalArgumentException(conn.getResponseMessage());
			} else if (responseCode >= 500 && responseCode <= 599) {
				throw new IOException(conn.getResponseMessage());
			} else {
				throw new RuntimeException(conn.getResponseMessage());
			}
		} finally {
			if (br != null) {
				br.close();
			}

			Log.d(TAG,
					"getResource: " + conn.getURL() + ":"
							+ (new Date().getTime() - startReq.getTime())
							+ " ms");
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private boolean postResource(String url, Object data) throws IOException,
			IllegalArgumentException {
		if (D)
			Log.d(TAG, "postResource()");
		HttpURLConnection conn = createConnection(url);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");

		PrintWriter wr = new PrintWriter(conn.getOutputStream());
		String json = gson.toJson(data);
		if (D)
			Log.d(TAG, "postResource(): " + json);
		wr.write(json);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		if (D)
			Log.d(TAG,
					"postResource: " + responseCode + " - "
							+ conn.getResponseMessage());
		try {
			if (responseCode >= 200 && responseCode <= 299) {
				return true;
			} else if (responseCode >= 400 && responseCode <= 499) {
				throw new IllegalArgumentException(conn.getResponseMessage());
			} else if (responseCode >= 500 && responseCode <= 599) {
				throw new IOException(conn.getResponseMessage());
			} else {
				throw new RuntimeException(conn.getResponseMessage());
			}
		} finally {
			conn.disconnect();
		}
	}

	@Override
	public List<MyObject> getphotoIds() throws IOException,
			IllegalArgumentException {
		MyObject[] photoIdArr = getResource("/photos", MyObject[].class);
		List<MyObject> photoIds = new ArrayList<MyObject>();
		for (MyObject photoId : photoIdArr) {
			photoIds.add(photoId);
		}
		return photoIds;
	}

	@Override
	public Bitmap getBitmap(String url) throws IOException,
			IllegalArgumentException {
		HttpURLConnection connection = createConnectionFromFullPath(url);
		Date startReq = new Date();
		connection.setDoInput(true);
		connection.connect();
		int status = connection.getResponseCode();
		Log.d(TAG,
				"getBitmap: " + "Not directed "+connection.getURL() + ":"
						+ (new Date().getTime() - startReq.getTime()) + " ms");
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER) {
				// get redirect url from "location" header field
				String newUrl = connection.getHeaderField("Location");
				Log.d("Redirect URL", newUrl);
				// open the new connnection again
				connection = (HttpURLConnection) new URL(newUrl)
						.openConnection();
				connection.addRequestProperty("Cache-Control", "max-stale="
						+ (60 * 60));
				connection.setConnectTimeout(mConnectionTimeout);
				connection.setReadTimeout(mReadTimeout);
			}
		}
		Bitmap bm = BitmapFactory.decodeStream(connection.getInputStream());
		Log.d(TAG,
				"getBitmap: " + connection.getURL() + ":"
						+ (new Date().getTime() - startReq.getTime()) + " ms");
		connection.disconnect();
		return bm;
	}

	@Override
	public Bitmap getPhoto(int photoId) throws IOException,
			IllegalArgumentException {
		return getBitmap(getObjectUrl(photoId));
	}

	@Override
	public boolean postObject(MyObject object) throws IOException,
			IllegalArgumentException {
		// TODO Auto-generated method stub
		return postResource("/post_url", object);
	}

	@Override
	public Request makeObjectRequest(MyObject mObject) {
		// TODO Auto-generated method stub
		String uriString = mApiRootUrl + "/objects/" + mObject.param1
				+ "/thumbnail";
		Request req = new Request(Uri.parse(uriString));
		req.setTitle("" + mObject.param2);

		// Create directory pamphlet if it does not exist
		String folder = "/" + mAppName + "/Thumbnail";
		File dir = new File(folder);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// Download pamphlet file
		req.setDestinationInExternalPublicDir(folder, mObject.param1 + "."
				+ MyObject.THUMBNAIL_FORMAT);
		return req;
	}

	@Override
	public String getObjectUrl(int objId) {
		// TODO Auto-generated method stub
		return mApiRootUrl + "/objects/" + objId;
	}
}
