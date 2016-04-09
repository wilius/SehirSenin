package org.alexiwilius.sehirsenin.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.alexiwilius.ranti_app.network.RequestTask;
import org.alexiwilius.ranti_app.network.request.PostRequest;
import org.alexiwilius.ranti_app.network.request.Request;
import org.alexiwilius.ranti_app.network.request.SOAPContent;
import org.alexiwilius.ranti_app.network.response.ErrorResponse;
import org.alexiwilius.ranti_app.network.response.JSONResponse;
import org.alexiwilius.ranti_app.network.response.Response;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.res.Database;
import org.alexiwilius.sehirsenin.res.Param;
import org.alexiwilius.sehirsenin.res.SOAPTemplate;
import org.alexiwilius.ranti_app.util.UIThread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import static org.alexiwilius.sehirsenin.R.id.*;
import static org.alexiwilius.sehirsenin.R.id.balance;

/**
 * Created by AlexiWilius on 18.10.2015.
 */
public class CardList extends Dialog {
    public CardList(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_list);
        ((ListView) findViewById(list_view)).setAdapter(mAdapter);
        findViewById(add_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddCard card = new AddCard(getContext());
                card.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mList = Database.getCardList();
                        mAdapter.notifyDataSetChanged();
                    }
                });
                card.show();
            }
        });
    }

    private void notifyError(Exception e) {
        console.showErrorMessage(getContext(), e.getMessage());
        e.printStackTrace();
    }

    JSONArray mList = Database.getCardList();
    JSONObject balanceList = new JSONObject();

    BaseAdapter mAdapter = new BaseAdapter() {
        JSONObject starredRec = null;

        @Override
        public int getCount() {
            return mList.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return mList.get(position);
            } catch (JSONException e) {
                notifyError(e);
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View v, ViewGroup parent) {
            if (v == null) {
                LayoutInflater inflater = UIThread.getActivity().getLayoutInflater();
                v = inflater.inflate(R.layout.card_list_row, parent, false);
            }

            try {
                final JSONObject rec = mList.getJSONObject(position);
                getTV(v, oval).setText(String.valueOf(position + 1));
                getTV(v, card_name).setText(rec.getString("name"));
                getTV(v, card_number).setText(rec.getString("number"));
                ImageView i = (ImageView) v.findViewById(starred);
                i.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Database.markCardStarred(rec.getString("number"));
                            refresh();
                        } catch (JSONException e) {
                            notifyError(e);
                        }
                    }
                });

                if (rec.getInt("starred") == 1) {
                    i.setImageResource(R.drawable.ic_check_black_24dp);
                    starredRec = rec;
                } else
                    i.setImageResource(R.drawable.ic_check_grey600_24dp);

                v.findViewById(remove).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Database.deleteCard(rec.getString("number"));
                            refresh();
                        } catch (JSONException e) {
                            notifyError(e);
                        }
                    }
                });

                final TextView textView = (TextView) v.findViewById(balance);
                if (balanceList.has(rec.getString("number"))) {
                    textView.setText(balanceList.getString(rec.getString("number")));
                    return v;
                }
                textView.setText(getContext().getString(R.string.balance_querying));
                RequestTask<Request, JSONResponse> mTask = new RequestTask<>(new RequestTask.ResponseReady() {
                    private String balanceText;

                    @Override
                    protected void onSuccess(Response responses) {
                        balanceText = getContext().getString(R.string.your_balance) + ((Document) responses.getData()).getElementsByTagName("UlasimKartiBakiyesiGetirResult").item(0).getTextContent();
                        try {
                            balanceList.put(rec.getString("number"), balanceText);
                        } catch (Exception e) {
                            notifyError(e);
                        }
                    }

                    @Override
                    protected void onError(ErrorResponse responses) {
                        balanceText = getContext().getString(R.string.error);
                    }

                    @Override
                    protected void onComplete(Response responses) {
                        textView.setText(balanceText);
                    }
                });

                mTask.execute(
                        new PostRequest(Param.SERVICE_URL,
                                new SOAPContent(SOAPTemplate.balance(rec.getString("number")),
                                        Param.SOAPActions.BALANCE)));
            } catch (Exception e) {
                notifyError(e);
            }
            return v;
        }

        private TextView getTV(View view, int resId) {
            return (TextView) view.findViewById(resId);
        }

        protected void refresh() {
            mList = Database.getCardList();
            notifyDataSetChanged();
        }
    };
}


