package org.alexiwilius.sehirsenin.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.alexiwilius.ranti_app.network.RequestTask;
import org.alexiwilius.ranti_app.network.request.PostRequest;
import org.alexiwilius.ranti_app.network.request.SOAPContent;
import org.alexiwilius.ranti_app.network.response.ErrorResponse;
import org.alexiwilius.ranti_app.network.response.Response;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.model.Station;
import org.alexiwilius.sehirsenin.res.Database;
import org.alexiwilius.sehirsenin.res.Param;
import org.alexiwilius.sehirsenin.res.SOAPTemplate;
import org.alexiwilius.ranti_app.util.UIThread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

/**
 * Created by AlexiWilius on 29.10.2015.
 */
public class BusStopBusList extends Dialog {

    private Station station;
    Adapter mAdapter = new Adapter();

    public BusStopBusList(Context context) {
        super(context);
    }

    @Override
    public void show() {
        if (station == null)
            throw new RuntimeException("Cannot call show method without setting stationId");
        super.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_stop_bus_list);
        ((TextView) findViewById(R.id.station_name))
                .setText(String.format("%s - %s ", station.getId(), station.getName()));
        ((ListView) findViewById(R.id.list_view)).setAdapter(mAdapter);
        mAdapter.load();
    }

    public void setStation(Station station) {
        this.station = station;
    }

    private class Adapter extends BaseAdapter {

        JSONArray busList = new JSONArray();

        @Override
        public int getCount() {
            return busList.length();
        }

        @Override
        public Object getItem(int position) {
            try {
                return busList.getJSONObject(position);
            } catch (Exception e) {
                console.showErrorMessage(getContext(), e.getMessage());
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    LayoutInflater inflater = UIThread.getActivity().getLayoutInflater();
                    convertView = inflater.inflate(R.layout.bus_stop_bus_list_row, parent, false);
                }
                JSONObject obj = busList.getJSONObject(position);
                final String id = obj.getString("id");

                ((TextView) convertView.findViewById(R.id.oval)).setText(String.valueOf(position + 1));
                ((TextView) convertView.findViewById(R.id.bus_name))
                        .setText(String.format("%s -> %s", id, obj.getString("name")));

                convertView.findViewById(R.id.progress).setVisibility(View.GONE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((MainActivity) UIThread.getActivity()).showLine(id);
                    }
                });
            } catch (Exception e) {
                console.showErrorMessage(getContext(), e.getMessage());
            }
            return convertView;
        }

        public void load() {
            RequestTask mTask = new RequestTask(new RequestTask.ResponseReady() {
                @Override
                protected void onSuccess(Response responses) {
                    try {
                        String buses = ((Document) responses.getData()).getElementsByTagName("a:GecenHatNumaralari").item(0).getTextContent();
                        if (buses != null && !buses.isEmpty())
                            busList = Database.getLineList(buses.split(";"));
                    } catch (Exception e) {
                        console.log(getContext(), e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                protected void onError(ErrorResponse responses) {
                    console.log(getContext(), responses.getData().toString());
                }

                @Override
                protected void onComplete(Response responses) {
                    disableLoading();
                    notifyDataSetChanged();
                }
            });

            try {
                mTask.execute(
                        new PostRequest(Param.SERVICE_URL,
                                new SOAPContent(
                                        SOAPTemplate.busList(station.getId()),
                                        Param.SOAPActions.BUS_STOP_BUS_LIST)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void disableLoading() {
        findViewById(R.id.progress).setVisibility(View.GONE);
    }
}
