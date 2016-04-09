package org.alexiwilius.sehirsenin.view;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.alexiwilius.ranti_app.util.UIThread;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.res.Database;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by AlexiWilius on 3.11.2015.
 */
public class SearchView extends RelativeLayout {
    private EditText mSearchView;
    private ListView mListView;

    private final Handler h = new Handler();
    JSONArray result = new JSONArray();
    private Runnable mCallback;
    private LayoutInflater mInflater;
    private TextView mEmptyView;
    private ImageView mClear;
    private ImageView mIcon;

    public SearchView(Context context) {
        super(context);
        init();
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mInflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.header, this, true);

        mSearchView = (EditText) findViewById(R.id.search_text);
        mClear = (ImageView) findViewById(R.id.clear_text);
        mIcon = (ImageView) findViewById(R.id.search_text_icon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mClear.setAlpha(.5f);
            mIcon.setAlpha(.5f);
        } else {
            mClear.setAlpha(200);
            mIcon.setAlpha(200);
        }
        mClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void handle(final String newText) {
        h.removeCallbacks(mCallback);
        if (newText == null || newText.isEmpty()) {
            getListView().setVisibility(GONE);
            mClear.setVisibility(INVISIBLE);
        } else {
            mClear.setVisibility(VISIBLE);
            mCallback = new Runnable() {
                @Override
                public void run() {
                    result = Database.findLine(newText);
                    getListView().setVisibility(VISIBLE);
                    mEmptyView.setText(String.format(getContext().getString(R.string.no_matched_line), newText));
                    mAdapter.notifyDataSetChanged();
                }
            };
            h.postDelayed(mCallback, 500);
        }
    }

    private View getListView() {
        ViewGroup parent = (ViewGroup) getParent();
        if (mListView == null) {
            mListView = (ListView) parent.findViewById(R.id.search_result);
            mEmptyView = (TextView) parent.findViewById(R.id.search_empty_view);
            mListView.setAdapter(mAdapter);
            mListView.setEmptyView(mEmptyView);
        }
        return parent.findViewById(R.id.search_result_wrapper);
    }

    boolean clear() {
        if ("".equals(mSearchView.getText().toString()))
            return false;
        mSearchView.setText("");
        return true;
    }


    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return result.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return result.getJSONObject(position);
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = UIThread.getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.bus_list_row, parent, false);
            }
            try {
                ((TextView) convertView.findViewById(R.id.bus_info)).setText(result.getJSONObject(position).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity a = (MainActivity) getContext();
                    try {
                        a.showLine(result.getJSONObject(position).getString("id"));
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                clear();
                            }
                        }, 500);
                    } catch (Exception e) {
                        console.notifyAndClose(a, e.getMessage());
                    }
                }
            });
            return convertView;
        }
    };

}
