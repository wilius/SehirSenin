package org.alexiwilius.sehirsenin.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import org.alexiwilius.sehirsenin.R;
import org.alexiwilius.sehirsenin.domain.map.PathController;


public class LinePath extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((RadioGroup) view.findViewById(R.id.direction_wrapper))
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        getController().setDirection(checkedId == R.id.departure ? 0 : 1);

                    }
                });
    }

    private PathController getController() {
        return (PathController) ((StationsMap) getActivity().getSupportFragmentManager().getFragments().get(1).getChildFragmentManager().findFragmentById(R.id.mapview)).getController();
    }
}