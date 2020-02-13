/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.payment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.sun.jna.NativeLong;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import ru.rutoken.demobank.Pkcs11CallerActivity;
import ru.rutoken.demobank.R;
import ru.rutoken.demobank.TokenManagerListener;
import ru.rutoken.pkcs11caller.Token;
import ru.rutoken.pkcs11caller.TokenManager;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.utils.Pkcs11ErrorTranslator;
import ru.rutoken.utils.TokenBatteryCharge;
import ru.rutoken.utils.TokenModelRecognizer;

public class PaymentsActivity extends Pkcs11CallerActivity {
    private class InfoDialog {
        final AlertDialog mDialog;
        final Button mConfirmButton;
        final TextView mPaymentTextView;

        InfoDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
            builder.setCancelable(true);
            mDialog = builder.create();
            View view = getLayoutInflater().inflate(R.layout.payment_info_layout, null);
            mDialog.setView(view);
            mConfirmButton = view.findViewById(R.id.sendB);
            mPaymentTextView = view.findViewById(R.id.dataTV);
        }

        void show(Spanned text) {
            mPaymentTextView.setText(text);
            mConfirmButton.setOnClickListener(view -> {
                mDialog.dismiss();
                signAction();
            });
            mDialog.show();
        }
    }

    // GUI
    private LinearLayout mPaymentsLayout;
    private TextView mTokenModelTextView;
    private TextView mTokenIDTextView;
    private TextView mTokenBatteryTextView;
    private ImageView mTokenBatteryImageView;
    private PopupWindow mPopupWindow;
    private InfoDialog mInfoDialog;
    private AlertDialog mSucceedDialog;
    private AlertDialog mProgressDialog;

    private String[] mPaymentTitles;
    private String[][] mPaymentValuesArray = null;
    //

    private String mSignData;

    // Activity input
    protected NativeLong mSlotId = TokenManagerListener.NO_SLOT;
    protected NativeLong mCertificate = TokenManagerListener.NO_CERTIFICATE;
    protected Token mToken = null;
    //

    // Logic
    private int mChecksCount = 0;
    //

    @Override
    public String getActivityClassIdentifier() {
        return getClass().getName();
    }

    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        setupActionBar();
        Intent intent = getIntent();
        mSlotId = (NativeLong) intent.getSerializableExtra("slotId");
        mCertificate = (NativeLong) intent.getSerializableExtra("certificate");
        mToken = TokenManager.getInstance().tokenForSlot(mSlotId);
        if (null == mToken) {
            finish();
        }
        TokenManagerListener.getInstance().setPaymentsCreated();
        setupUI();
    }

    private void setupActionBar() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.actionbar_layout, null);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

        /* Custom actionbar */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(v, params);
        }
    }

    private void setupUI() {
        mPaymentsLayout = findViewById(R.id.paymentsLayout);
        mTokenBatteryTextView = findViewById(R.id.percentageTV);
        mTokenIDTextView = findViewById(R.id.tokenIdTV);
        mTokenModelTextView = findViewById(R.id.modelTV);
        mTokenBatteryImageView = findViewById(R.id.batteryIV);

        mTokenModelTextView.setText(TokenModelRecognizer.getInstance(this).marketingNameForPkcs11Name(mToken.getModel())
                + " " + mToken.getShortDecSerialNumber());
        mTokenIDTextView.setText("");

        if (mToken.getModel().contains("ECP BT")) {
            int charge = TokenBatteryCharge.getBatteryPercentage(mToken.getCharge());
            mTokenBatteryTextView.setText(charge + "%");
            mTokenBatteryImageView.setImageResource(TokenBatteryCharge.getBatteryImageForVoltage(mToken.getCharge()));
        } else {
            mTokenBatteryTextView.setText("");
            mTokenBatteryImageView.setImageResource(android.R.color.transparent);
        }

        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_layout, null);
        mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button popupButton = popupView.findViewById(R.id.popupB);
        popupButton.setOnClickListener(view -> {
            int paymentsCount = 0;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mPaymentsLayout.getChildCount(); ++i) {
                View childView = mPaymentsLayout.getChildAt(i);
                if (Payment.class.isInstance(childView)) {
                    Payment payment = (Payment) childView;
                    CheckBox checkBox = payment.findViewById(R.id.checkBox);
                    if (checkBox.isChecked()) {
                        builder.append(getSignData(payment));
                        ++paymentsCount;
                    }
                }
            }
            mSignData = builder.toString();
            showBatchPaymentInfo(paymentsCount);
        });

        createSucceedDialog();
        mInfoDialog = new InfoDialog();
        createProgressDialog();

        createPayments();
    }

    protected void uncheckAllPayments() {
        for (int i = 0; i < mPaymentsLayout.getChildCount(); ++i) {
            View childView = mPaymentsLayout.getChildAt(i);
            if (Payment.class.isInstance(childView)) {
                Payment payment = (Payment) childView;
                CheckBox checkBox = payment.findViewById(R.id.checkBox);
                checkBox.setChecked(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        TokenManagerListener.getInstance().resetPaymentsCreated();
        logout(mToken);
        super.onBackPressed();
    }

    @Override
    protected void manageLoginError(@Nullable Pkcs11Exception exception) {
    }

    @Override
    protected void manageLoginSucceed() {
    }

    @Override
    protected void manageSignError(@Nullable Pkcs11Exception exception) {
        uncheckAllPayments();
        mProgressDialog.dismiss();
        String message = getString(R.string.error);
        if (exception != null) {
            message = Pkcs11ErrorTranslator.getInstance(this).messageForRV(exception.getErrorCode());
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void manageSignSucceed(byte[] data) {
        uncheckAllPayments();
        mProgressDialog.dismiss();
        mSucceedDialog.show();
    }

    @Override
    protected void manageLogoutError(@Nullable Pkcs11Exception exception) {
    }

    @Override
    protected void manageLogoutSucceed() {
    }

    private void createPayments() {
        Resources res = getResources();
        mPaymentTitles = res.getStringArray(R.array.payments_titles);
        TypedArray ta = res.obtainTypedArray(R.array.payments_values);
        int n = ta.length();
        mPaymentValuesArray = new String[n][];
        for (int i = 0; i < n; ++i) {
            int id = ta.getResourceId(i, 0);
            if (id > 0) {
                mPaymentValuesArray[i] = res.getStringArray(id);
            } else {
                // something wrong with the XML
            }
        }
        ta.recycle();

        int nRecipient = 0;
        int nPrice = 0;
        for (int i = 0; i < mPaymentTitles.length; ++i) {
            if (mPaymentTitles[i].equals(res.getString(R.string.recipient))) {
                nRecipient = i;
            }
            if (mPaymentTitles[i].equals(res.getString(R.string.price))) {
                nPrice = i;
            }
        }

        for (int i = 0; i < mPaymentValuesArray.length; ++i) {
            int price = Integer.valueOf(mPaymentValuesArray[i][nPrice]);
            Payment payment = new Payment(this, null, i, mPaymentValuesArray[i][nRecipient], price);
            payment.setOnClickListener(view -> {
                if (!Payment.class.isInstance(view)) return;
                showOnePaymentInfo((Payment) view);
            });
            CheckBox checkBox = payment.findViewById(R.id.checkBox);
            checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    ++mChecksCount;
                    mPopupWindow.showAtLocation(mPaymentsLayout, Gravity.BOTTOM, 0, 0);
                } else {
                    --mChecksCount;
                    if (mChecksCount == 0) {
                        mPopupWindow.dismiss();
                    }
                }
            });
            mPaymentsLayout.addView(payment);
        }
    }

    protected void signAction() {
        mProgressDialog.show();
        sign(mToken, mCertificate, Objects.requireNonNull(mSignData).getBytes());
    }

    protected String createFullPaymentHtml(int num) {
        StringBuilder result = new StringBuilder();
        DateFormat df = DateFormat.getDateInstance();
        String date = df.format(new Date());
        result.append("<h3>").append(getString(R.string.payment)).append(String.format("%d", num + Payment.FIRST_NUMBER)).append("</h3>");
        result.append("<font color=#CCCCCC>").append(getString(R.string.fromDate)).append(" ").append(date).append("</font>");
        result.append("<br/><br/>");
        for (int i = 0; i < mPaymentTitles.length; ++i) {
            result.append("<font color=#CCCCCC size=-1>").append(mPaymentTitles[i]).append("</font><br/>");
            result.append("<font color=#000000 size=-1>").append(mPaymentValuesArray[num][i]).append("</font><br/>");
        }
        return result.toString();
    }

    private void showBatchPaymentInfo(int count) {
        String message = String.format(getString(R.string.batch_sign_message), count) + "<br />" + getString(R.string.batch_require_proceed);
        mInfoDialog.show(Html.fromHtml(message));
    }

    private void showOnePaymentInfo(Payment payment) {
        if (null == payment) return;
        int number = payment.getNum();
        mSignData = getSignData(payment);

        mInfoDialog.show(Html.fromHtml(createFullPaymentHtml(number)));
    }

    private void createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
        builder.setCancelable(false);
        mProgressDialog = builder.create();
        View view = getLayoutInflater().inflate(R.layout.progress_dialog, null);
        mProgressDialog.setView(view);
    }

    private void createSucceedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentsActivity.this);
        builder.setCancelable(true);
        mSucceedDialog = builder.create();
        View successView = getLayoutInflater().inflate(R.layout.result_dialog_layout, null);
        successView.setOnClickListener(view -> mSucceedDialog.dismiss());
        mSucceedDialog.setView(successView);
    }

    private String getSignData(Payment payment) {
        StringBuilder result = new StringBuilder();
        result.append(payment.getAmount())
              .append(payment.getReceiver())
              .append(payment.getDate());
        for (String s : mPaymentValuesArray[payment.getNum()])
            result.append(s);
        return result.toString();
    }
}
