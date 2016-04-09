package org.alexiwilius.sehirsenin.domain;

import org.alexiwilius.sehirsenin.listener.OnListItemSelectedListener;

/**
 * Created by AlexiWilius on 29.1.2015.
 */
public abstract class ListViewController extends ViewController {
    abstract void setOnListItemSelectedListener(OnListItemSelectedListener onListItemSelectedListener);
}
