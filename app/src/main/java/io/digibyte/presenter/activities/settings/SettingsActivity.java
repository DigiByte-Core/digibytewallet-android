package io.digibyte.presenter.activities.settings;

import static io.digibyte.R.layout.settings_list_item;
import static io.digibyte.R.layout.settings_list_section;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import io.digibyte.R;
import io.digibyte.databinding.ActivitySettingsBinding;
import io.digibyte.presenter.activities.SettingsCallback;
import io.digibyte.presenter.activities.util.BRActivity;
import io.digibyte.presenter.entities.BRSettingsItem;
import io.digibyte.presenter.interfaces.BRAuthCompletion;
import io.digibyte.tools.manager.BRSharedPrefs;
import io.digibyte.tools.security.AuthManager;

public class SettingsActivity extends BRActivity {
    public List<BRSettingsItem> items = new LinkedList();
    private SettingsListAdapter adapter;

    private SettingsCallback callback = new SettingsCallback() {
        private int count = 0;
        @Override
        public void onTitleClick() {
            count++;
            if (count == 5) {
                items.add(new BRSettingsItem(getString(R.string.Settings_advancedTitle), "", v -> {
                    Intent intent = new Intent(SettingsActivity.this, AdvancedActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                }, false));
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        binding.setCallback(callback);
        populateItems();
        adapter = new SettingsListAdapter(this, R.layout.settings_list_item, items);
        binding.setAdapter(adapter);
    }

    public class SettingsListAdapter extends ArrayAdapter<String> {

        private List<BRSettingsItem> items;
        private Context mContext;

        public SettingsListAdapter(@NonNull Context context, @LayoutRes int resource,
                @NonNull List<BRSettingsItem> items) {
            super(context, resource);
            this.items = items;
            this.mContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View v;
            BRSettingsItem item = items.get(position);
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();

            if (item.isSection) {
                v = inflater.inflate(settings_list_section, parent, false);
            } else {
                v = inflater.inflate(settings_list_item, parent, false);
                TextView addon = v.findViewById(R.id.item_addon);
                addon.setText(item.addonText);
                v.setOnClickListener(item.listener);
            }

            TextView title = v.findViewById(R.id.item_title);
            title.setText(item.title);
            return v;

        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }

    private void populateItems() {

        items.add(new BRSettingsItem(getString(R.string.Settings_wallet), "", null, true));

        items.add(new BRSettingsItem(getString(R.string.Settings_importTitle), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, ImportActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);

        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_wipe), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, WipeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_manage), "", null, true));

        if (AuthManager.isFingerPrintAvailableAndSetup(this)) {
            items.add(new BRSettingsItem(getString(R.string.Settings_touchIdLimit_android), "",
                    v -> AuthManager.getInstance().authPrompt(SettingsActivity.this, null,
                            getString(R.string.VerifyPin_continueBody),
                            new BRAuthCompletion() {
                                @Override
                                public void onComplete() {
                                    Intent intent = new Intent(SettingsActivity.this,
                                            SpendLimitActivity.class);
                                    overridePendingTransition(R.anim.enter_from_right,
                                            R.anim.exit_to_left);
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancel() {

                                }
                            }), false));
        }

        items.add(new BRSettingsItem(getString(R.string.Settings_currency),
                BRSharedPrefs.getIso(this), v -> {
            Intent intent = new Intent(SettingsActivity.this, DisplayCurrencyActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_sync), "", v -> {
            Intent intent = new Intent(SettingsActivity.this,
                    SyncBlockchainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, false));

        items.add(new BRSettingsItem(getString(R.string.Settings_about), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, false));
    }
}