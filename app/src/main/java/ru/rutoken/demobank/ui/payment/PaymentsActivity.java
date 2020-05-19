/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.ui.payment;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import ru.rutoken.demobank.R;
import ru.rutoken.demobank.pkcs11caller.Token;
import ru.rutoken.demobank.pkcs11caller.TokenManager;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.demobank.ui.Pkcs11CallerActivity;
import ru.rutoken.demobank.ui.TokenManagerListener;
import ru.rutoken.demobank.ui.main.MainActivity;
import ru.rutoken.demobank.utils.Pkcs11ErrorTranslator;
import ru.rutoken.demobank.utils.TokenBatteryCharge;
import ru.rutoken.demobank.utils.TokenModelRecognizer;

public class PaymentsActivity extends Pkcs11CallerActivity {
    // GUI
    private LinearLayout mPaymentsLayout;
    private PopupWindow mPopupWindow;
    private InfoDialog mInfoDialog;
    private AlertDialog mSucceedDialog;
    private AlertDialog mProgressDialog;

    private String[] mPaymentTitles;
    private String[][] mPaymentValuesArray = null;
    private String mSignData;
    // Activity input
    private String mCertificateFingerprint = TokenManagerListener.NO_FINGERPRINT;
    private Token mToken = null;
    // Logic
    private int mChecksCount = 0;

    @Override
    public String getActivityClassIdentifier() {
        return getClass().getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setupActionBar();
        Intent intent = getIntent();

        String tokenSerial = intent.getStringExtra(MainActivity.EXTRA_TOKEN_SERIAL);
        mCertificateFingerprint = intent.getStringExtra(MainActivity.EXTRA_CERTIFICATE_FINGERPRINT);
        mToken = TokenManager.getInstance().getTokenBySerial(tokenSerial);
        if (null == mToken) {
            Toast.makeText(this, R.string.rutoken_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        TokenManagerListener.getInstance(this).setPaymentsCreated();
        setupUI();
    }

    private void setupActionBar() {
        View view = getLayoutInflater().inflate(R.layout.actionbar_layout, null);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

        /* Custom actionbar */
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setCustomView(view, params);
        }
    }

    private void setupUI() {
        mPaymentsLayout = findViewById(R.id.paymentsLayout);
        TextView tokenBatteryTextView = findViewById(R.id.percentageTV);
        TextView tokenIDTextView = findViewById(R.id.tokenIdTV);
        TextView tokenModelTextView = findViewById(R.id.modelTV);
        ImageView tokenBatteryImageView = findViewById(R.id.batteryIV);

        tokenModelTextView.setText(TokenModelRecognizer.getInstance(this).marketingNameForPkcs11Name(mToken.getModel())
                + " " + mToken.getShortDecSerialNumber());
        tokenIDTextView.setText("");

        if (mToken.getModel().contains("ECP BT")) {
            int charge = TokenBatteryCharge.getBatteryPercentage(mToken.getCharge());
            tokenBatteryTextView.setText(charge + "%");
            tokenBatteryImageView.setImageResource(TokenBatteryCharge.getBatteryImageForVoltage(mToken.getCharge()));
        } else {
            tokenBatteryTextView.setText("");
            tokenBatteryImageView.setImageResource(android.R.color.transparent);
        }

        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);
        mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        Button popupButton = popupView.findViewById(R.id.popupB);
        popupButton.setOnClickListener(view -> {
            int paymentsCount = 0;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mPaymentsLayout.getChildCount(); ++i) {
                View childView = mPaymentsLayout.getChildAt(i);
                if (childView instanceof Payment) {
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

    private void uncheckAllPayments() {
        for (int i = 0; i < mPaymentsLayout.getChildCount(); ++i) {
            View childView = mPaymentsLayout.getChildAt(i);
            if (childView instanceof Payment) {
                Payment payment = (Payment) childView;
                CheckBox checkBox = payment.findViewById(R.id.checkBox);
                checkBox.setChecked(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        TokenManagerListener.getInstance(this).resetPaymentsCreated();
        mToken.clearPin();
        super.onBackPressed();
    }

    @Override
    protected void manageTokenOperationError(@Nullable Pkcs11Exception exception) {
        uncheckAllPayments();
        mProgressDialog.dismiss();
        String message = getString(R.string.error);
        if (exception != null) {
            message = Pkcs11ErrorTranslator.getInstance(this).messageForRV(exception.getErrorCode());
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void manageTokenOperationCanceled() {
        uncheckAllPayments();
        mProgressDialog.dismiss();
    }

    @Override
    protected void manageTokenOperationSucceed() {
        uncheckAllPayments();
        mProgressDialog.dismiss();
        mSucceedDialog.show();
    }

    private void createPayments() {
        Resources res = getResources();
        mPaymentTitles = res.getStringArray(R.array.payments_titles);
        TypedArray ta = res.obtainTypedArray(R.array.payments_values);
        int n = ta.length();
        mPaymentValuesArray = new String[n][];
        for (int i = 0; i < n; ++i) {
            int id = ta.getResourceId(i, 0);
            if (id > 0)
                mPaymentValuesArray[i] = res.getStringArray(id);
            else
                throw new RuntimeException("something wrong with the XML");
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
            int price = Integer.parseInt(mPaymentValuesArray[i][nPrice]);
            Payment payment = new Payment(this, null, i, mPaymentValuesArray[i][nRecipient], price);
            payment.setOnClickListener(view -> {
                if (!(view instanceof Payment)) return;
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

    private void signAction() {
        mProgressDialog.show();
        sign(mToken, mCertificateFingerprint, Objects.requireNonNull(mSignData).getBytes());
    }

    private String createFullPaymentHtml(int num) {
        StringBuilder result = new StringBuilder();
        DateFormat df = DateFormat.getDateInstance();
        String date = df.format(new Date());
        result.append("<h3>").append(getString(R.string.payment)).append(
                String.format(Locale.getDefault(), "%d", num + Payment.FIRST_NUMBER)).append("</h3>");
        result.append("<font color=#CCCCCC>").append(getString(R.string.fromDate)).append(" ").append(date)
                .append("</font>");
        result.append("<br/><br/>");
        for (int i = 0; i < mPaymentTitles.length; ++i) {
            result.append("<font color=#CCCCCC size=-1>").append(mPaymentTitles[i]).append("</font><br/>");
            result.append("<font color=#000000 size=-1>").append(mPaymentValuesArray[num][i]).append("</font><br/>");
        }
        return result.toString();
    }

    private void showBatchPaymentInfo(int count) {
        String message = getString(R.string.batch_sign_message, count)
                + "<br />" + getString(R.string.batch_require_proceed);
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
}
