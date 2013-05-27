package se761.bestgroup.vsmreceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie2;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LogActivity extends Activity {

	@SuppressWarnings("unused")
	private NfcAdapter mNfcAdapter;
	private ArrayAdapter<String> listAdapter;
	private BasicClientCookie2 _deptCookie;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		listAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		ListView lv = (ListView) findViewById(R.id.logListView);
		lv.setAdapter(listAdapter);
		
		_deptCookie = new BasicClientCookie2("department", "default");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(item.getItemId() == R.id.action_departments){
			startActivityForResult(new Intent(this, DepartmentsActivity.class), 1);
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		// Check to see that the Activity started due to an Android Beam
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
			processIntent(getIntent());
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String dept = data.getStringExtra("department");
		_deptCookie = new BasicClientCookie2("department", dept);
		setTitle(dept); 
	}
	@Override
	public void onNewIntent(Intent intent) {
		// onResume gets called after this to handle the intent
		setIntent(intent);
	}

	void processIntent(Intent intent) {

		Parcelable[] rawMsgs = intent
				.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		NdefMessage msg = (NdefMessage) rawMsgs[0];
		String patient = new String(msg.getRecords()[0].getPayload());
		Log.d("Receiver",patient);
		listAdapter.add(patient);
		// send the message somewhere
		SubmitVitalStats vitalStatsUpload = new SubmitVitalStats();
		vitalStatsUpload.execute(patient);
	}

	private class SubmitVitalStats extends AsyncTask<String, Void, Boolean> {

		private DefaultHttpClient httpclient;

		@Override
		protected Boolean doInBackground(String... params) {

			// instantiates httpclient to make request
			httpclient = new DefaultHttpClient();
			httpclient.getCookieStore().addCookie(_deptCookie);

			// passes the results to a string builder/entity
			StringEntity patientSE = null;
			String patientString;
			try {
				patientString = params[0].toString();
			} catch (IndexOutOfBoundsException e) {
				// incorrect msg
				return false;
			}
			
			Log.v("Receiver", patientString);
			try {
				patientSE = new StringEntity(patientString);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return false;
			}
			
			boolean result = true;
			Log.v("Receiver", "Patients Response");
			result = httpPost(patientSE, new HttpPost(getResources().getString(R.string.patients_endpoint)));
			Log.v("Receiver", "Vitals Response");	
			return result;
		}
		
		private boolean httpPost(StringEntity se, HttpPost httpost) {
			// sets the post request as the resulting string
			httpost.setEntity(se);
			// sets a request header so the page receiving the request
			// will know what to do with it
			httpost.setHeader("Accept", "application/json");
			httpost.setHeader("Content-type", "application/json");

			try {
				HttpResponse response = httpclient.execute(httpost);
				InputStream content = response.getEntity().getContent();
				BufferedReader br = new BufferedReader(new InputStreamReader(
						content));
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
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
