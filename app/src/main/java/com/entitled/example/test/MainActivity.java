package com.entitled.example.test;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler{
    BillingProcessor bp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bp = new BillingProcessor(this, "YOUR_LICENSE_KEY", this);
    }

    public void purchase(View view) {
        bp.purchase(MainActivity.this, "purchase");
    }

    public void subscribeMonthly(View view) {
        bp.subscribe(MainActivity.this, "subscription_monthly");
    }

    public void subscribeYearly(View view) {
        bp.subscribe(MainActivity.this, "subscription_yearly");
    }

    public void loadPurchase(View view) {
        bp.loadOwnedPurchasesFromGoogle();
        List<String> products = bp.listOwnedProducts();
        List<String> subscriptions = bp.listOwnedSubscriptions();

        if (!products.isEmpty() || !subscriptions.isEmpty()) {
            List<String> responses = new ArrayList<String>();


            for (String sku : products) {
                TransactionDetails details = bp.getPurchaseTransactionDetails(sku);
                String json = extractJsonResponse(details);
                responses.add(json);
            }

            for (String sku : subscriptions) {
                TransactionDetails details = bp.getSubscriptionTransactionDetails(sku);
                String json = extractJsonResponse(details);
                responses.add(json);
            }

            String allPurchases = TextUtils.join(", \n", responses);
            updateText(allPurchases);
        } else {
            updateText("No purchases or subscriptions found.");
        }

    }

    public void updateDetails(TransactionDetails details) {
        String response = extractJsonResponse(details);
        updateText(response);
    }

    public String extractJsonResponse(TransactionDetails details) {
        String response = null;
        try {
            response = new JSONObject(details.purchaseInfo.responseData).toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;
    }

    public void updateText(String text) {
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(text);
    }

    // IBillingHandler implementation

    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
         */
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        updateDetails(details);
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        /*
         * Called when some error occurred. See Constants class for more details
         *
         * Note - this includes handling the case where the user canceled the buy dialog:
         * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
         */
    }

    @Override
    public void onPurchaseHistoryRestored() {
        /*
         * Called when purchase history was restored and the list of all owned PRODUCT ID's
         * was loaded from Google Play
         */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }
}
