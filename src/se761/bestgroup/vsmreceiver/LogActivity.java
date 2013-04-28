package se761.bestgroup.vsmreceiver;

import se761.bestgroup.vsmreceiver.R;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class LogActivity extends Activity {

	private NfcAdapter mNfcAdapter;
	private ArrayAdapter<String> listAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
		listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		ListView lv = (ListView) findViewById(R.id.logListView);
		lv.setAdapter(listAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.log, menu);
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
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }
	
	void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        listAdapter.add(new String(msg.getRecords()[0].getPayload()));
        // send the message somewhere
    }

}
