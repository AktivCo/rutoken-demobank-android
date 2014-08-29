package ru.rutoken.demobank;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Lashin on 29.08.2014.
 */
public class Payment extends RelativeLayout {
    private TextView mDateTextView;
    private TextView mRecieverTextView;
    private TextView mAmountTextView;

    public Payment(Context context) {
        super(context);
        initPayment();
    }

    public Payment(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPayment();
    }

    private void initPayment() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.payment_layout, this);

        mDateTextView = (TextView)findViewById(R.id.dateTV);
        mRecieverTextView = (TextView)findViewById(R.id.recieverTV);
        mAmountTextView = (TextView)findViewById(R.id.amountTV);
    }

    public void setDate(String value) {mDateTextView.setText(value);}
    public String getDate() {return mDateTextView.getText().toString();}
    public void setReciever(String value) {mRecieverTextView.setText(value);}
    public String getReciever() {return mRecieverTextView.getText().toString();}
    public void setAmount(String value) {mAmountTextView.setText(value);}
    public String getAmount() {return mAmountTextView.getText().toString();}
}
