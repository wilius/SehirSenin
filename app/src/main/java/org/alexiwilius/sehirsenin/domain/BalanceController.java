package org.alexiwilius.sehirsenin.domain;

import android.content.DialogInterface;
import android.view.View;
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
import org.alexiwilius.sehirsenin.view.BalanceView;
import org.alexiwilius.sehirsenin.view.BaseView;
import org.alexiwilius.sehirsenin.view.CardList;
import org.alexiwilius.ranti_app.util.UIThread;
import org.json.JSONObject;
import org.w3c.dom.Document;

public class BalanceController extends ViewController {

    private BalanceView mView;

    @Override
    public void start() {
        addOnClickListener();
        update();
    }

    @Override
    public void stop() {
        mTask.cancel();
    }

    @Override
    public void refresh(Object params) {
        update();
    }

    @Override
    public void setView(BaseView view) {
        mView = (BalanceView) view;
    }

    @Override
    View getView() {
        return mView;
    }

    private void addOnClickListener() {
        mView.setOnClickListener(mListener);
        mView.findViewById(R.id.add_card_button).setOnClickListener(mListener);
    }

    private void update() {
        JSONObject card = Database.getStarredCard();
        TextView balance = (TextView) mView.findViewById(R.id.balance);
        if (card == null) {
            mView.setOnClickListener(mListener);
            mView.setNoCardTitle();
            balance.setClickable(false);
            return;
        }

        mView.setClickable(false);

        balance.setOnClickListener(mUpdateListener);
        mView.findViewById(R.id.balanceLabel).setOnClickListener(mUpdateListener);
        try {
            mView.setCardName(card.getString("name"));
            mView.enableLoading();
            mTask.execute(
                    new PostRequest(Param.SERVICE_URL,
                            new SOAPContent(SOAPTemplate.balance(card.getString("number")),
                                    Param.SOAPActions.BALANCE)));
        } catch (Exception e) {
            mView.showErrorMessage(e);
        }
    }

    private RequestTask<Request, JSONResponse> mTask = new RequestTask<>(new RequestTask.ResponseReady() {
        private String balance;

        @Override
        protected void onSuccess(Response responses) {
            balance = ((Document) responses.getData()).getElementsByTagName("UlasimKartiBakiyesiGetirResult").item(0).getTextContent();
        }

        @Override
        protected void onError(ErrorResponse responses) {
            console.showErrorMessage(mView.getContext(), responses.getData().toString());
            balance = "";
        }

        @Override
        protected void onComplete(Response responses) {
            mView.setBalance(balance);
        }
    });

    private void showAddCardDialog() {
        CardList dialog = new CardList(mView.getContext());
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                update();
            }
        });
        dialog.show();
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showAddCardDialog();
        }
    };

    private View.OnClickListener mUpdateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            update();
        }
    };
}
