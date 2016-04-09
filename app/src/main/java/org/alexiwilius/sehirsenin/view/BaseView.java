package org.alexiwilius.sehirsenin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.ranti_app.util.UIThread;
import org.alexiwilius.sehirsenin.domain.ViewController;

/**
 * Created by AlexiWilius on 19.4.2015.
 */
public abstract class BaseView extends LinearLayout {
    private Toast mToast;

    public BaseView(Context context) {
        super(context);
        if (!isInEditMode())
            init(context);
    }

    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode())
            init(context);
    }

    protected abstract void init(Context context);

    public abstract void enableLoading();

    public abstract void disableLoading();

    public void showErrorMessage(Exception e) {
        console.showErrorMessage(getContext(), e.getMessage());
        e.printStackTrace();
    }

    public void showInfoMessage(String message) {
        if (mToast != null)
            mToast.cancel();

        mToast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
