/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.ui.payment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import ru.rutoken.demobank.R;

public class Payment extends RelativeLayout {
    public static final int FIRST_NUMBER = 746;
    private final int mAmount;
    private TextView mNumTextView;
    private TextView mDateTextView;
    private TextView mReceiverTextView;
    private TextView mAmountTextView;

    public Payment(Context context, AttributeSet attrs, int num, String recipient, int amount) {
        super(context, attrs);
        initPayment();
        DateFormat df = DateFormat.getDateInstance();
        mDateTextView.setText(df.format(new Date()));
        mNumTextView.setText(String.format("%d", num + FIRST_NUMBER));
        mReceiverTextView.setText(recipient);
        mAmount = amount;
        String amountString = String.format("%d", mAmount).replaceAll("\\d\\d\\d$", " $0 руб");
        mAmountTextView.setText(amountString);
    }

    private void initPayment() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.payment_layout, this);

        mNumTextView = findViewById(R.id.numTV);
        mDateTextView = findViewById(R.id.dateTV);
        mReceiverTextView = findViewById(R.id.receiverTV);
        mAmountTextView = findViewById(R.id.amountTV);
    }

    public int getNum() {
        return Integer.parseInt(mNumTextView.getText().toString()) - FIRST_NUMBER;
    }

    public String getDate() {
        return mDateTextView.getText().toString();
    }

    public String getReceiver() {
        return mReceiverTextView.getText().toString();
    }

    public int getAmount() {
        return mAmount;
    }
}
