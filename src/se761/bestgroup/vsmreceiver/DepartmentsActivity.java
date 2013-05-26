package se761.bestgroup.vsmreceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DepartmentsActivity extends Activity {

	private List<String> departments;
	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_departments);

		final ListView listView = (ListView) findViewById(R.id.listDepartments);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		final DepartmentsActivity activity = this;
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String department = (String) listView
						.getItemAtPosition(position);

				Toast.makeText(getApplicationContext(),
						"Department set to: " + department, Toast.LENGTH_SHORT)
						.show();
				new HttpTask(true).execute(department);
			}
		});
		new HttpTask(false).execute("");
	}

	private List<String> getDepartments() {
		List<String> departments = new ArrayList<String>();
		HttpGet get = new HttpGet(getStr(R.string.departments_endpoint));
		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpResponse response = httpclient.execute(get);
			InputStream ins = response.getEntity().getContent();
			BufferedReader buff = new BufferedReader(new InputStreamReader(ins));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = buff.readLine()) != null) {
				sb.append(line);
			}
			JSONObject json = new JSONObject(sb.toString());
			JSONArray arr = json.getJSONArray(getStr(R.string.departments));
			for (int i = 0; i < arr.length(); i++) {
				departments.add(arr.getJSONObject(i).getString(
						getStr(R.string.department_name)));
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return departments;
	}

	private Cookie getCookieFromSubway(String department) {

		HttpClient httpclient;
		HttpPost httppost = new HttpPost();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(getStr(R.string.department), department));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		// sets a request header so the page receving the request
		// will know what to do with it
		httppost.setHeader("Accept", "application/json");
		// httppost.setHeader("Content-type", "application/json");

		try {
			httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(httppost);

			InputStream content = response.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					content));
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			List<Cookie> cookies = ((DefaultHttpClient) httpclient)
					.getCookieStore().getCookies();
			return cookies.get(0);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private class HttpTask extends AsyncTask<String, Void, Cookie> {

		private List<String> _departments;
		private boolean imhungry;

		public HttpTask(boolean getACookie) {
			this.imhungry = getACookie;
		}

		@Override
		protected Cookie doInBackground(String... departments) {
			if (imhungry)
				return getCookieFromSubway(departments[0]);
			_departments = getDepartments();
			return null;
		}

		@Override
		protected void onPostExecute(Cookie result) {
			if (!imhungry) {
				for (String s : _departments) {
					adapter.add(s);
				}
			} else {
				Editor edit = getSharedPreferences(getStr(R.string.cookie), MODE_PRIVATE)
						.edit();
				Gson gson = new Gson();
				edit.putString(getStr(R.string.department), gson.toJson(result));
				edit.commit();
				finish();
			}

		}
	}
	
	private String getStr(int id){
		return getResources().getString(id);
	}
}
