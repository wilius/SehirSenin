package org.alexiwilius.sehirsenin.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.res.Database;

/**
 * Created by AlexiWilius on 16.10.2015.
 */
public class AddCard extends Dialog {

    public AddCard(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.add_card_title);
        setContentView(R.layout.add_card);
        findViewById(R.id.ok_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((TextView) findViewById(R.id.card_name)).getText().toString(),
                        number = ((TextView) findViewById(R.id.card_number)).getText().toString();
                if (name.length() == 0)
                    console.showErrorMessage(getContext(), R.string.card_name_null);
                else if (number.length() == 0)
                    console.showErrorMessage(getContext(), R.string.card_number_not_null);
                else {
                    try {
                        Database.addCard(number, name);
                        dismiss();
                    } catch (Exception e) {
                        console.showErrorMessage(getContext(), e.getMessage());
                    }
                }
            }
        });

        EditText cardName = (EditText) findViewById(R.id.card_name);
        cardName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        cardName.setSelection(cardName.getText().length());
        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    
}