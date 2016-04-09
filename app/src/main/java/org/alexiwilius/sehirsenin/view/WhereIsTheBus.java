package org.alexiwilius.sehirsenin.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.domain.BalanceController;
import org.alexiwilius.sehirsenin.domain.BusListController;
import org.alexiwilius.sehirsenin.domain.map.StationsController;
import org.alexiwilius.sehirsenin.model.Station;
import org.json.JSONObject;

/**
 * Created by AlexiWilius on 18.11.2014.
 */
public class WhereIsTheBus extends Fragment {

    private BusListController busController;
    private BalanceController balanceController;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        busController = new BusListController();
        balanceController = new BalanceController();
        StationsController mapController = getMapController();
        if (mapController != null)
            mapController.setOnStationSelectListener(new StationsController.OnStationSelectListener() {
                @Override
                public void onSelect(Station data) {
                    busController.refresh(data);
                    ((MainActivity) getActivity()).closeMenu();
                }
            });
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.where_is_the_bus, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        busController.setView((BaseView) view.findViewById(R.id.bus_list));
        balanceController.setView((BaseView) view.findViewById(R.id.balance_view));
    }

    @Override
    public void onResume() {
        super.onResume();
        startController();
    }

    @Override
    public void onPause() {
        stopController();
        super.onPause();

    }

    private void startController() {
        busController.start();
        balanceController.start();
    }

    private void stopController() {
        busController.stop();
        balanceController.stop();
    }

    private StationsController getMapController() {
        return (StationsController) ((StationsMap) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapview)).getController();
    }
}