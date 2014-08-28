package ru.rutoken.demobank;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class PaymentsActivity extends ExternallyDismissableActivity {
    private LinearLayout mPaymentsLayout;

    private static final String ACTIVITY_CLASS_IDENTIFIER = ExternallyDismissableActivity.class.getName();

    public String getActivityClassIdentifier() {
        return ACTIVITY_CLASS_IDENTIFIER;
    }


    public static String PAYMENTS_CREATED = PaymentsActivity.class.getName() + "PAYMENTS_CREATED";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setupActionBar();
        setupUI();
    }

    private void setupActionBar() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_layout, null);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

        /*Custom actionbar*/
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setBackgroundDrawable(
                    this.getResources().getDrawable(R.drawable.ab_bg));
            actionBar.setCustomView(v, params);
        }
    }

    private void setupUI() {
        mPaymentsLayout = (LinearLayout)findViewById(R.id.paymentsLayout);

        int[] paymentsIds = new int[2];
        paymentsIds[0] = R.array.bashneft_payment;
        paymentsIds[1] = R.array.lukoil_payment;
        createPayments(paymentsIds);
    }

    private void createPayments(int[] IDs) {
        for (int i = 0; i < IDs.length; ++i) {
            PaymentView view = new PaymentView(PaymentsActivity.this);

            String[] data = getResources().getStringArray(IDs[i]);
            view.setNum(getString(R.string.number) + data[0]);
            view.setDate(data[1]);
            view.setReciever(data[2]);
            view.setAmount(data[3]);

            mPaymentsLayout.addView(view);
        }
    }
}
