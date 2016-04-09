package org.alexiwilius.sehirsenin.view;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.listener.OnListItemSelectedListener;
import org.alexiwilius.ranti_app.util.UIThread;
import org.alexiwilius.sehirsenin.res.Database;
import org.w3c.dom.Element;

public class BusListView extends BaseView {
    private TextView mHeader;
    private TextView mEmptyView;
    private ProgressBar mProgress;
    private ImageView mMenuIcon;

    private OnListItemSelectedListener onListItemSelectedListener;
    private ImageView mFavorite;
    private View mNameContainer;

    public BusListView(Context context) {
        super(context);
    }

    public BusListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    
    @Override
    protected void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.bus_list, this, true);

        mHeader = (TextView) findViewById(R.id.bus_stop_name);
        mProgress = (ProgressBar) findViewById(R.id.mini_progress);
        mMenuIcon = (ImageView) findViewById(R.id.menu_icon);
        mEmptyView = (TextView) findViewById(R.id.emptyNearBuses);
        mFavorite = (ImageView) findViewById(R.id.favorite);
        mNameContainer = findViewById(R.id.bus_name_wrapper);
    }

    @Override
    public void enableLoading() {
        mProgress.setVisibility(VISIBLE);
        mMenuIcon.setVisibility(GONE);
        mEmptyView.setText(R.string.loading);
    }

    @Override
    public void disableLoading() {
        mProgress.setVisibility(GONE);
        mMenuIcon.setVisibility(VISIBLE);
        mEmptyView.setText(R.string.no_bus);
    }

    public ImageView getFavoriteButton() {
        return mFavorite;
    }

    public View onItemCreate(Element data, int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = UIThread.getActivity().getLayoutInflater();
            convertView = inflater.inflate(R.layout.bus_list_row, parent, false);
        }

        TextView busNumber = (TextView) convertView.findViewById(R.id.bus_info),
                minute = (TextView) convertView.findViewById(R.id.minute);

        try {
            busNumber.setText(
                    String.format("%s > %s",
                            getTextContent(data, "a:HatNumarasi"),
                            getTextContent(data, "a:HatAdi")));

            minute.setText(String.format(getContext().getString(R.string.bus_list_row_item_min), getTextContent(data, "a:KalanDakika")));
        } catch (Exception e) {
            console.showErrorMessage(getContext(), e.getMessage());
        }

        return convertView;
    }

    private String getTextContent(Element data, String s) {
        return data.getElementsByTagName(s).item(0).getTextContent();
    }


    public void setTitle(String title) {
        mHeader.setText(title);
    }

    public void setAdapter(final BaseAdapter adapter) {
        ListView listView = (ListView) findViewById(R.id.nearBuses);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                fireOnListItemClickEvent((Element) adapter.getItem(i));
            }
        });

        listView.setAdapter(adapter);
        listView.setEmptyView(findViewById(R.id.emptyNearBuses));
    }

    private void fireOnListItemClickEvent(Element item) {
        if (onListItemSelectedListener == null) return;

        onListItemSelectedListener.onListItemSelected(item);
    }

    public void setOnListItemSelectedListener(OnListItemSelectedListener onListItemSelectedListener) {
        this.onListItemSelectedListener = onListItemSelectedListener;
    }

    public void changeFavoriteStatus(Boolean isFavorite) {
        mFavorite.setImageResource(isFavorite ? R.drawable.ic_star_grey600_36dp : R.drawable.ic_star_outline_grey600_36dp);
    }

    public View getNameContainer() {
        return mNameContainer;
    }
}
