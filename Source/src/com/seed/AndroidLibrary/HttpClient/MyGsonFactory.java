package com.seed.AndroidLibrary.HttpClient;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.seed.AndroidLibrary.Object.MyObject;

public class MyGsonFactory {
	public static Gson getInstance() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(MyObject.class,
				new MyObjectDeserializer());
		return gsonBuilder.create();
	}
	
	private static class MyObjectDeserializer implements JsonDeserializer<MyObject>{

		@Override
		public MyObject deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObj = json.getAsJsonObject();
			MyObject pamphlet = new MyObject();
			pamphlet.param1 = jsonObj.get("id").getAsInt();
			pamphlet.param2 = jsonObj.get("title").getAsString();
			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.JAPAN);
			try {
				pamphlet.param3 = formater.parse(jsonObj.get("lastUpdatedAt").getAsString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return pamphlet;
		}
		
	}
}
