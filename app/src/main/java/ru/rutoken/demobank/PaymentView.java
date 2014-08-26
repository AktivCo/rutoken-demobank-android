package ru.rutoken.demobank;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Lashin on 26.08.2014.
 */

public class PaymentView extends LinearLayout {
    private TextView mNumTextView;
    private TextView mDateTextView;
    private TextView mRecieverTextView;
    private TextView mAmountTextView;

    public PaymentView(Context context) {
        super(context);
        initComponent();
    }

    public PaymentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponent();
    }

    private void initComponent() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.payment_layout, this);

        mNumTextView = (TextView)findViewById(R.id.numTV);
        mDateTextView = (TextView)findViewById(R.id.dateTV);
        mRecieverTextView = (TextView)findViewById(R.id.recieverTV);
        mAmountTextView = (TextView)findViewById(R.id.amountTV);
    }

    public void setNum(String value) {
        mNumTextView.setText(value);
    }

    public void setDate(String value) {
        mDateTextView.setText(value);
    }

    public void setReciever(String value) {
        mRecieverTextView.setText(value);
    }

    public void setAmount(String value) {
        mAmountTextView.setText(value);
    }
}
