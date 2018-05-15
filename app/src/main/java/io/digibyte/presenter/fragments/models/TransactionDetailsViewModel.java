package io.digibyte.presenter.fragments.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.platform.tools.KVStoreManager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.digibyte.DigiByte;
import io.digibyte.R;
import io.digibyte.presenter.entities.TxItem;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.manager.TxManager;
import io.digibyte.tools.threads.BRExecutor;
import io.digibyte.tools.util.BRCurrency;
import io.digibyte.tools.util.BRExchange;

public class TransactionDetailsViewModel extends BaseObservable {

    private TxItem item;

    public TransactionDetailsViewModel(TxItem item) {
        this.item = item;
    }

    @Bindable
    public String getAmount() {
        boolean isBTCPreferred = BRSharedPrefs.getPreferredBTC(DigiByte.getContext());
        boolean received = item.getSent() == 0;
        String iso = isBTCPreferred ? "DGB" : BRSharedPrefs.getIso(DigiByte.getContext());
        long satoshisAmount = received ? item.getReceived() : (item.getSent() - item.getReceived());
        return BRCurrency.getFormattedCurrencyString(DigiByte.getContext(), iso,
                BRExchange.getAmountFromSatoshis(DigiByte.getContext(), iso,
                        new BigDecimal(satoshisAmount)));
    }

    @Bindable
    public String getToFrom() {
        return item.getReceived() - item.getSent() < 0 ? DigiByte.getContext().getString(R.string.sent) : DigiByte.getContext().getString(R.string.received);
    }

    @Bindable
    public boolean getSent() {
        return item.getReceived() - item.getSent() < 0;
    }

    @Bindable
    public String getAddress() {
        return getSent() ? item.getTo()[0] : item.getFrom()[0];
    }

    @Bindable
    public int getSentReceivedIcon() {
        if (getSent()) {
            return R.drawable.transaction_details_sent;
        } else {
            return R.drawable.transaction_details_received;
        }
    }

    @Bindable
    public String getComment() {
        return item.metaData.comment;
    }

    @Bindable
    public String getDate() {
        return getFormattedDate(item.getTimeStamp());
    }

    @Bindable
    public String getTime() {
        return getFormattedTime(item.getTimeStamp());
    }

    @Bindable
    public boolean getCompleted() {
        return BRSharedPrefs.getLastBlockHeight(DigiByte.getContext()) - item.getBlockHeight() + 1 >= 6;
    }
    public void setComment(String comment) {
        item.metaData.comment = comment;
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                KVStoreManager.getInstance().putTxMetaData(DigiByte.getContext(), item.metaData, item.getTxHash());
                TxManager.getInstance().updateTxList();
            }
        });
    }

    private String getFormattedDate(long timeStamp) {
        Date currentLocalTime = new Date(timeStamp == 0 ? System.currentTimeMillis() : timeStamp * 1000);
        SimpleDateFormat date1 = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        return date1.format(currentLocalTime);
    }

    private String getFormattedTime(long timeStamp) {
        Date currentLocalTime = new Date(timeStamp == 0 ? System.currentTimeMillis() : timeStamp * 1000);
        SimpleDateFormat date2 = new SimpleDateFormat("HH:mm a", Locale.getDefault());
        return  date2.format(currentLocalTime);
    }
//
//    private String getShortAddress(String addr) {
//        String p1 = addr.substring(0, 5);
//        String p2 = addr.substring(addr.length() - 5, addr.length());
//        return p1 + "..." + p2;
//    }
}