package org.alexiwilius.sehirsenin.domain;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import org.alexiwilius.ranti_app.network.RequestTask;
import org.alexiwilius.ranti_app.network.request.PostRequest;
import org.alexiwilius.ranti_app.network.request.Request;
import org.alexiwilius.ranti_app.network.request.SOAPContent;
import org.alexiwilius.ranti_app.network.response.ErrorResponse;
import org.alexiwilius.ranti_app.network.response.Response;
import org.alexiwilius.ranti_app.util.Timer;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.listener.OnListItemSelectedListener;
import org.alexiwilius.sehirsenin.model.Station;
import org.alexiwilius.sehirsenin.res.Database;
import org.alexiwilius.sehirsenin.res.Param;
import org.alexiwilius.sehirsenin.res.ReachedMaxStarredCardException;
import org.alexiwilius.sehirsenin.res.SOAPTemplate;
import org.alexiwilius.sehirsenin.view.BaseView;
import org.alexiwilius.sehirsenin.view.BusListView;
import org.alexiwilius.sehirsenin.view.BusStopBusList;
import org.alexiwilius.sehirsenin.view.MainActivity;
import org.alexiwilius.sehirsenin.view.StationsMap;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BusListController extends ListViewController {

    private Station station;

    private NodeList data;
    private Timer mTimer = new Timer();
    private BusListView mView;

    @Override
    public void start() {
        mView.setAdapter(adapter);
        mView.getNameContainer().setOnClickListener(busStopBusListListener);
        mView.getFavoriteButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (station == null) {
                    busStopBusListListener.onClick(v);
                    return;
                }

                try {
                    int messageId = R.string.remove_favorite;
                    if (!station.isFavorite())
                        messageId = R.string.mark_as_favorited;

                    station.setFavorite(!station.isFavorite());
                    mView.changeFavoriteStatus(station.isFavorite());
                    mView.showInfoMessage(String.format(mView.getContext().getString(messageId), station.getName()));
                } catch (ReachedMaxStarredCardException e) {
                    mView.showErrorMessage(
                            new ReachedMaxStarredCardException(
                                    String.format(mView.getContext().getString(R.string.reached_max_starred_station), Database.MAX_STARRED_STATION_AMOUNT), e));
                }
            }
        });
        refresh(station);
    }

    @Override
    public void stop() {
        if (mTask != null)
            mTask.cancel();
        mTimer.stop();
    }

    @Override
    public void refresh(final Object newStation) {
        if (newStation == null) return;

        try {
            mView.enableLoading();

            // this is just a trick to show empty view
            if (station != null && !station.equals(newStation)) {
                data = null;
                adapter.notifyDataSetChanged();
            }

            station = (Station) newStation;

            stop();
            mView.setTitle(String.format("%s - %s ", station.getId(), station.getName()));
            mView.changeFavoriteStatus(station.isFavorite());

            mTask.execute(
                    new PostRequest(Param.SERVICE_URL,
                            new SOAPContent(
                                    SOAPTemplate.station(station.getId()),
                                    Param.SOAPActions.INCOMING_BUS_LIST))
            );

        } catch (Exception e) {
            mView.disableLoading();
            mView.showErrorMessage(e);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setView(BaseView view) {
        mView = (BusListView) view;
    }

    public BusListView getView() {
        return mView;
    }

    @Override
    public void setOnListItemSelectedListener(OnListItemSelectedListener onListItemSelectedListener) {
        mView.setOnListItemSelectedListener(onListItemSelectedListener);
    }

    private RequestTask<Request, Response> mTask = new RequestTask<>(new RequestTask.ResponseReady() {
        int errorCounter = 0;

        @Override
        public void onSuccess(Response responses) {
            data = ((Document) responses.getData()).getElementsByTagName("a:DuragaYaklasanOtobus");
            errorCounter = 0;
            mView.disableLoading();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onError(ErrorResponse responses) {
            errorCounter++;
            if (errorCounter >= 3) {
                mView.showErrorMessage(new Exception(responses.getData().toString()));
                mView.disableLoading();
                data = null;
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void onComplete(Response responses) {
            if (errorCounter < 3) {
                mTimer.start(new Runnable() {
                    @Override
                    public void run() {
                        refresh(station);
                    }
                }, responses instanceof ErrorResponse ? 0 : 10000L);
            } else
                errorCounter = 0;
        }
    });

    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return data == null ? 0 : data.getLength();
        }

        @Override
        public Object getItem(int position) {
            return data.item(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mView.onItemCreate((Element) getItem(position), position, convertView, parent);
        }
    };

    View.OnClickListener busStopBusListListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (station != null) {
                BusStopBusList list = new BusStopBusList(mView.getContext());
                list.setStation(station);
                list.show();
            } else {
                final MainActivity activity = (MainActivity) getContext();

                console.showErrorMessage(activity, R.string.select_bus_stop_to_see_bus_list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.openMenu();
                        ((StationsMap) ((MainActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.mapview)).moveCameraToCurrentPosition();
                    }
                });
            }
        }
    };
}
