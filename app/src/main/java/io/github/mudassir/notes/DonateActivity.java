package io.github.mudassir.notes;

import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.mudassir.notes.billing.IabBroadcastReceiver;
import io.github.mudassir.notes.billing.IabHelper;
import io.github.mudassir.notes.billing.IabResult;
import io.github.mudassir.notes.billing.Inventory;
import io.github.mudassir.notes.billing.Purchase;
import io.github.mudassir.notes.billing.key.Gen;

public class DonateActivity extends AppCompatActivity
		implements IabHelper.OnIabPurchaseFinishedListener, IabHelper.OnIabSetupFinishedListener,
		IabHelper.QueryInventoryFinishedListener, View.OnClickListener {

	public static final int REQUEST_CODE = 7860;

	public static final String TAG = "DonateActivity";

	private IabHelper iabHelper;
	private TextView oneDollar;
	private TextView fiveDollars;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_donate);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		iabHelper = new IabHelper(this, Gen.decode());
		iabHelper.enableDebugLogging(true, TAG);
		iabHelper.startSetup(this);

		oneDollar = (TextView) findViewById(R.id.one_dollar);
		fiveDollars = (TextView) findViewById(R.id.five_dollars);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (iabHelper != null) {
			try {
				iabHelper.dispose();
			} catch (IabHelper.IabAsyncInProgressException e) {
				Log.e(TAG, "Error disposing of iabHelper", e);
			}
		}

		iabHelper = null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onClick(View view) {
		String sku = "";
		if (view.getId() == R.id.one_dollar) {
			sku = "one_dollar";
		} else if (view.getId() == R.id.five_dollars) {
			sku = "five_dollars";
		}

		if (!TextUtils.isEmpty(sku)) {
			try {
				iabHelper.launchPurchaseFlow(this, sku, REQUEST_CODE, this);
			} catch (IabHelper.IabAsyncInProgressException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onIabSetupFinished(IabResult result) {
		if (!result.isSuccess()) {
			Log.d(TAG, "There was a problem setting up In-app Billing: " + result);
			return;
		}

		List<String> sku = new ArrayList<>();
		sku.add("one_dollar");
		sku.add("five_dollars");

		try {
			iabHelper.queryInventoryAsync(true, sku, null,  this);
		} catch (IabHelper.IabAsyncInProgressException e) {
			Log.e(TAG, "Error fetching SKU items", e);
		}
	}

	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
		if (result.isFailure()) {
			// handle error
			return;
		}

		if (inventory.hasDetails("one_dollar")) {
			String oneDollarPrice = inventory.getSkuDetails("one_dollar").getPrice();
			oneDollar.setText(getString(R.string.donate) + " " + oneDollarPrice);
			oneDollar.setOnClickListener(this);
		}

		if (inventory.hasDetails("five_dollars")) {
			String fiveDollarsPrice = inventory.getSkuDetails("five_dollars").getPrice();
			fiveDollars.setText(getString(R.string.donate) + " " + fiveDollarsPrice);
			fiveDollars.setOnClickListener(this);
		}

	}

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		if (result.isFailure()) {
			return;
		}
	}
}
