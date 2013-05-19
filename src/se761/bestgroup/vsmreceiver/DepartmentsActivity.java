package se761.bestgroup.vsmreceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DepartmentsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_departments);

		String[] values = new String[] { "Pediatrics", "Gynocology" };
		List<String> departments = new ArrayList<String>();
		for (String v : values)
			departments.add(v);

		final ListView listView = (ListView) findViewById(R.id.listDepartments);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
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
				new Post().execute(department);
				startActivity(new Intent(activity, LogActivity.class));
			}
		});
		adapter.addAll(departments);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.departments, menu);
		return true;
	}

	private class Post extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... departments) {

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httpost = new HttpPost("http://vsm.herokuapp.com/login/");

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("department", departments[0]));

			try {
				httpost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			// sets a request header so the page receving the request
			// will know what to do with it
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");

			try {
				httpclient = new DefaultHttpClient();
				HttpResponse response = httpclient.execute(httpost);
				InputStream content = response.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						content));
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				Header[] mCookies = response.getHeaders("department");
				Log.v("Cookie", "Cookies: "+mCookies.length);
				for(Header h : mCookies){
					Log.v("Cookie", h.toString());
				}
				Log.v("Receiver Response", sb.toString());
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}
	

}
