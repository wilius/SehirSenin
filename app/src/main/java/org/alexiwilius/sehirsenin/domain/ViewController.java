package org.alexiwilius.sehirsenin.domain;

import android.content.Context;
import android.view.View;

import org.alexiwilius.sehirsenin.view.BaseView;

/**
 * Created by AlexiWilius on 17.4.2015.
 */
public abstract class ViewController {
    abstract void start();

    abstract void stop();

    abstract void refresh(Object data);

    abstract void setView(BaseView view);

    abstract View getView();

    protected Context getContext() {
        return getView().getContext();
    }

}
