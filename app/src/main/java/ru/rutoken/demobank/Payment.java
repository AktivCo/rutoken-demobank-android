/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class Payment extends RelativeLayout {
    private TextView mNumTextView;
    private TextView mDateTextView;
    private TextView mRecieverTextView;
    private TextView mAmountTextView;
    private int mAmount;
    public static final int FIRST_NUMBER = 746;
    public static final int THRESHOLD_PRICE = 50000;

    public Payment(Context context, AttributeSet attrs, int num, String recipient, int amount) {
        super(context, attrs);
        initPayment();
        DateFormat df = DateFormat.getDateInstance();
        mDateTextView.setText(df.format(new Date()));
        mNumTextView.setText(String.format("%d", num + FIRST_NUMBER));
        mRecieverTextView.setText(recipient);
        mAmount = amount;
        String amountString = String.format("%d", mAmount).replaceAll("\\d\\d\\d$", " $0 руб");
        mAmountTextView.setText(amountString);
    }

    private void initPayment() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.payment_layout, this);

        mNumTextView = (TextView) findViewById(R.id.numTV);
        mDateTextView = (TextView) findViewById(R.id.dateTV);
        mRecieverTextView = (TextView) findViewById(R.id.recieverTV);
        mAmountTextView = (TextView) findViewById(R.id.amountTV);
    }

    public boolean needAskPIN() {
        return mAmount >= Payment.THRESHOLD_PRICE;
    }

    public int getNum() {
        return Integer.parseInt(mNumTextView.getText().toString()) - FIRST_NUMBER;
    }

    public String getDate() {
        return mDateTextView.getText().toString();
    }

    public String getReciever() {
        return mRecieverTextView.getText().toString();
    }

    public int getAmount() {
        return mAmount;
    }
}
