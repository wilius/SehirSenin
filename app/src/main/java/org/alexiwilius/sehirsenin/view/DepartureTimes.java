package org.alexiwilius.sehirsenin.view;

/**
 * Created by AlexiWilius on 4.11.2015.
 */

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.alexiwilius.ranti_app.network.RequestTask;
import org.alexiwilius.ranti_app.network.request.PostRequest;
import org.alexiwilius.ranti_app.network.request.Request;
import org.alexiwilius.ranti_app.network.request.SOAPContent;
import org.alexiwilius.ranti_app.network.response.ErrorResponse;
import org.alexiwilius.ranti_app.network.response.JSONResponse;
import org.alexiwilius.ranti_app.network.response.Response;
import org.alexiwilius.ranti_app.util.Timer;
import org.alexiwilius.ranti_app.util.console;
import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.res.Database;
import org.alexiwilius.sehirsenin.res.Param;
import org.alexiwilius.sehirsenin.res.SOAPTemplate;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DepartureTimes extends Fragment {

    private String lineNumber, dayId = "97";
    private Integer dayResId = R.string.weekdays;

    private List<String> mDepartureTimes = new ArrayList<>(), mDepartureTimesC = new ArrayList<>(),
            mComebackTimes = new ArrayList<>(), mComebackTimesC = new ArrayList<>();

    private ListFilterAdapter mDepartureAdapter = new ListFilterAdapter(mDepartureTimes),
            mComebackAdapter = new ListFilterAdapter(mComebackTimes);
    private Calendar mCalendar;
    private Boolean mIsDataLoaded = false;
    private int mDay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCalendar = Calendar.getInstance();
        lineNumber = getActivity().getIntent().getStringExtra(BusActivity.BUS_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.departure_times, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        JSONObject line = Database.getLine(lineNumber);

        try {
            mapDay(mCalendar.get(Calendar.DAY_OF_WEEK));
            getTV(R.id.departure_header).setText(line.getString("departure"));
            getTV(R.id.comeback_header).setText(line.getString("comeback"));
            Integer[] listIds = {R.id.departure, R.id.comeback};
            getView().findViewById(R.id.day_info_wrapper).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final DepartureDaySelect select = new DepartureDaySelect(getContext(), mDay);
                    select.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mapDay(select.day);
                            loadData();
                        }
                    });
                    select.show();
                }
            });
            mDepartureAdapter.setEmptyView(getTV(R.id.departure_empty));
            mComebackAdapter.setEmptyView(getTV(R.id.comeback_empty));

            ListFilterAdapter[] adapters = {mDepartureAdapter, mComebackAdapter};
            int counter = 0;
            for (int id : listIds) {
                RecyclerView list = getLV(id);
                final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                list.setLayoutManager(layoutManager);
                list.setHasFixedSize(true);
                list.setAdapter(adapters[counter]);
                adapters[counter].setListView(list);
                counter++;
            }
        } catch (Exception e) {
            console.notifyAndClose(getActivity(), e.getMessage());
        }
    }

    private void mapDay(int day) {
        mDay = day;
        if (day == Calendar.SATURDAY) {
            dayId = "98";
            dayResId = R.string.saturday;
        } else if (day == Calendar.SUNDAY) {
            dayId = "99";
            dayResId = R.string.sunday;
        } else {
            dayId = "97";
            dayResId = R.string.weekdays;
        }
        getTV(R.id.day_info).setText(String.format(getString(R.string.day_info_text), getString(dayResId)));
    }

    private RecyclerView getLV(int resId) {
        return (RecyclerView) getView().findViewById(resId);
    }

    private TextView getTV(int resId) {
        return (TextView) getView().findViewById(resId);
    }

    private RequestTask<Request, JSONResponse> mTask = new RequestTask<>(new RequestTask.ResponseReady() {
        @Override
        protected void onSuccess(Response responses) {
            mDepartureTimes.clear();
            mDepartureTimesC.clear();
            mComebackTimes.clear();
            mComebackTimesC.clear();
            NodeList list = ((Document) responses.getData()).getElementsByTagName("a:HareketSaati");
            for (int i = 0; i < list.getLength(); i++) {
                NodeList list2 = list.item(i).getChildNodes();
                for (int j = 0; j < list2.getLength(); j++) {
                    Element el = (Element) list2.item(j);
                    if (el == null)
                        continue;
                    String content = el.getTextContent();
                    if (content == null || content.isEmpty())
                        continue;
                    content = padLeft(content, 5);
                    if (el.getNodeName().contains("Donus")) {
                        mComebackTimes.add(content);
                        mComebackTimesC.add(content);
                    } else if (el.getNodeName().contains("Gidis")) {
                        mDepartureTimes.add(content);
                        mDepartureTimesC.add(content);
                    }
                }
            }
            Collections.sort(mComebackTimesC);
            Collections.sort(mDepartureTimesC);
            mIsDataLoaded = true;
            mDepartureAdapter.notifyDataSetChanged();
            mComebackAdapter.notifyDataSetChanged();
            getView().findViewById(R.id.progress).setVisibility(View.GONE);
            getView().findViewById(R.id.departure_times_wrapper).setVisibility(View.VISIBLE);
            start();
            errorCounter = 0;
        }

        int errorCounter = 0;

        @Override
        protected void onError(ErrorResponse responses) {
            errorCounter++;
            if (errorCounter < 3)
                loadData();
            else
                console.notifyAndClose(getActivity(), responses.getData().toString());

        }
    });

    private int updateTimes(List<String> list) {
        mCalendar.setTime(new Date());
        String cTime = String.format("%s:%s", padLeft("" + mCalendar.get(Calendar.HOUR_OF_DAY), 2), padLeft("" + mCalendar.get(Calendar.MINUTE), 2), 5);
        String min = "";
        int pos = -1, i = -1;
        String temp;
        for (String time : list) {
            i++;
            temp = cTime;
            if (time.compareTo(temp) < 0)
                continue;

            if (min.isEmpty() || time.compareTo(min) <= 0) {
                min = time;
                pos = i;
            }
        }
        return pos != -1 ? pos : list.size() == 0 ? -1 : 0;
    }

    private int calcScrollPos(int pos, int listSize) {
        if (pos == -1 || pos <= 5) return pos;
        int a = listSize - 1 - pos;
        a = a > 5 ? 5 : a;
        return pos + a;
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$" + n + "s", s).replaceAll(" ", "0");
    }

    @Override
    public void onPause() {
        stop();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        start();
    }

    public void stop() {
        mTimer.stop();
    }

    public void start() {
        if (!mIsDataLoaded)
            loadData();
        else
            mTimer.start(new Runnable() {
                @Override
                public void run() {
                    int comeback = updateTimes(mComebackTimesC),
                            departure = updateTimes(mDepartureTimesC);

                    if (comeback == -1 || departure == -1) return;
                    comeback = mComebackTimes.indexOf(mComebackTimesC.get(comeback));
                    departure = mDepartureTimes.indexOf(mDepartureTimesC.get(departure));
                    if (mComebackAdapter.mP.val != comeback) {
                        getLV(R.id.comeback).scrollToPosition(calcScrollPos(comeback, mComebackTimes.size()));
                        int old = mComebackAdapter.mP.val;
                        mComebackAdapter.mP.val = comeback;
                        mComebackAdapter.notifyItemChanged(old);
                        mComebackAdapter.notifyItemChanged(comeback);
                    }

                    if (mDepartureAdapter.mP.val != departure) {
                        getLV(R.id.departure).scrollToPosition(calcScrollPos(departure, mDepartureTimes.size()));
                        int old = mDepartureAdapter.mP.val;
                        mDepartureAdapter.mP.val = departure;
                        mDepartureAdapter.notifyItemChanged(old);
                        mDepartureAdapter.notifyItemChanged(departure);
                    }
                    mTimer.start(this, 10000L);
                }
            });
    }

    private void loadData() {
        try {
            getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.departure_times_wrapper).setVisibility(View.GONE);
            mTask.cancel();
            mTask.execute(
                    new PostRequest(Param.SERVICE_URL,
                            new SOAPContent(SOAPTemplate.departureTimes(lineNumber, dayId),
                                    Param.SOAPActions.LINE_DEPARTURE_LIST)));
        } catch (Exception e) {
            console.notifyAndClose(getActivity(), e.getMessage());
        }
    }

    class ListFilterAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<String> mList;
        protected POJO mP;
        private TextView mEmptyView;
        private RecyclerView mListView;

        public ListFilterAdapter(List<String> list) {
            mList = list;
            mP = new POJO();
        }

        protected void setEmptyView(TextView emptyView) {
            mEmptyView = emptyView;
        }

        protected void setListView(RecyclerView listView) {
            mListView = listView;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.line_departure_times_row, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            TextView textView = (TextView) holder.itemView.findViewById(R.id.departure_time);
            textView.setText(mList.get(position));
            if (position == mP.val) {
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.claret_red_window));
                textView.setTextColor(Color.WHITE);
            } else {
                holder.itemView.setBackgroundColor(0);
                textView.setTextColor(getResources().getColor(R.color.secondary_text_default_material_light));
            }
        }


        @Override
        public int getItemCount() {
            int count = mList.size();
            /*if (count <= 0) {
                mEmptyView.setText(String.format(getString(R.string.no_voyage), getString(dayResId)));
                mEmptyView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }*/
            return count;
        }
    }


    private Timer mTimer = new Timer();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class POJO {
        Integer val = -1;
    }
}

