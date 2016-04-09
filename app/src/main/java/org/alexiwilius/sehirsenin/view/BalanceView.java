package org.alexiwilius.sehirsenin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import org.alexiwilius.sehirsenin.R;

/**
 * Created by AlexiWilius on 19.4.2015.
 */
public class BalanceView extends BaseView {

    public BalanceView(Context context) {
        super(context);
    }

    public BalanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.balance, this, true);
    }

    @Override
    public void enableLoading() {
        ((TextView) findViewById(R.id.balance)).setText(R.string.loading);
    }

    @Override
    public void disableLoading() {

    }

    public void setBalance(String balance) {
        disableLoading();
        findViewById(R.id.balanceLabel).setVisibility(VISIBLE);
        findViewById(R.id.add_card_button).setVisibility(VISIBLE);
        ((TextView) findViewById(R.id.balance)).setText(balance + " Lira");
    }

    public void setNoCardTitle() {
        findViewById(R.id.balanceLabel).setVisibility(GONE);
        findViewById(R.id.add_card_button).setVisibility(GONE);
        disableLoading();
        ((TextView) findViewById(R.id.balance)).setText(getContext().getString(R.string.no_defined_card));
    }

    public void setCardName(String name) {
        ((TextView) findViewById(R.id.balanceLabel)).setText(name + "   :");
    }
}
