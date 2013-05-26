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

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

public class DepartmentsActivity extends ListActivity {

	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ListView listView = getListView();
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String department = (String) listView
						.getItemAtPosition(position);
				
				Intent data = new Intent();
				data.putExtra("department", department);
				setResult(RESULT_OK, data);
				finish();
			}
		});
		Log.v("Departments", "Starting task");
		new HttpTask().execute();
	}

	private class HttpTask extends AsyncTask<Void, Void, List<String>> {

		@Override
		protected List<String> doInBackground(Void... params) {
			Log.v("Departments", "Starting getDeps");
			List<String> departments = new ArrayList<String>();
			Log.v("Departments", getStr(R.string.departments_endpoint));
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
				Log.v("Departments", sb.toString());
				JSONArray arr = new JSONArray(sb.toString());
				for (int i = 0; i < arr.length(); i++) {
					Log.v("Departments",
							arr.getJSONObject(i).getString(
									getStr(R.string.department_name)));
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

		@Override
		protected void onPostExecute(List<String> departments) {
			for (String s : departments) {
				adapter.add(s);
			}
		}
	}

	private String getStr(int id) {
		return getResources().getString(id);
	}
}
