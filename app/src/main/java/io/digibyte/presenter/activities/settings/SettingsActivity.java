package io.digibyte.presenter.activities.settings;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                }, BRSettingsItem.Type.ITEM));
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this,
                R.layout.activity_settings);
        binding.setCallback(callback);
        binding.settingsList.setLayoutManager(new LinearLayoutManager(this));
        populateItems();
        adapter = new SettingsListAdapter(items);
        binding.setAdapter(adapter);
    }

    public class SettingsListAdapter extends RecyclerView.Adapter<SettingsListAdapter.ViewHolder> {

        private List<BRSettingsItem> items;
        LayoutInflater inflater = LayoutInflater.from(SettingsActivity.this);

        public SettingsListAdapter(@NonNull List<BRSettingsItem> items) {
            this.items = items;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private View view;
            private TextView addon;
            private TextView title;
            private SwitchCompat settingSwitch;

            public ViewHolder(View view ) {
                super(view);
                this.view = view;
                addon = view.findViewById(R.id.item_addon);
                title = view.findViewById(R.id.item_title);
                settingSwitch = view.findViewById(R.id.settings_switch);
            }
        }

        @Override
        public int getItemCount() {
            return items == null ? 0 : items.size();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                default:
                case 0:
                    return new ViewHolder(inflater.inflate(R.layout.settings_list_item, parent, false));
                case 1:
                    return new ViewHolder(inflater.inflate(R.layout.settings_list_section, parent, false));
                case 2:
                    return new ViewHolder(inflater.inflate(R.layout.settings_switch, parent, false));
            }
        }

        @Override
        public int getItemViewType(int position) {
            BRSettingsItem item = items.get(position);
            switch (item.type) {
                default:
                case ITEM:
                    return 0;
                case SECTION:
                    return 1;
                case SWITCH:
                    return 2;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BRSettingsItem item = items.get(position);
            switch (item.type) {
                default:
                case ITEM: {
                    holder.addon.setText(item.addonText);
                    holder.view.setOnClickListener(item.listener);
                    holder.title.setText(item.title);
                    break;
                }
                case SECTION: {
                    holder.title.setText(item.title);
                    break;
                }
                case SWITCH:
                    holder.title.setText(item.title);
                    holder.settingSwitch.setChecked(
                            BRSharedPrefs.getGenericSettingsSwitch(SettingsActivity.this,
                                    item.switchKey));
                    holder.settingSwitch.setOnCheckedChangeListener(
                            (buttonView, isChecked) -> BRSharedPrefs.setGenericSettingsSwitch(SettingsActivity.this,
                                    item.switchKey, isChecked));
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }

    private void populateItems() {

        items.add(new BRSettingsItem(getString(R.string.Settings_wallet), "", null,
                BRSettingsItem.Type.SECTION));

        //No support currently in BRActivity for the concept of import wallet from QR
        /*items.add(new BRSettingsItem(getString(R.string.Settings_importTitle), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, ImportActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);

        }, false));*/

        items.add(new BRSettingsItem(getString(R.string.Settings_wipe), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, WipeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_bottom, R.anim.empty_300);
        }, BRSettingsItem.Type.ITEM));

        items.add(new BRSettingsItem(getString(R.string.Settings_manage), "", null,
                BRSettingsItem.Type.SECTION));

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
                            }), BRSettingsItem.Type.ITEM));
        }

        items.add(new BRSettingsItem(getString(R.string.max_send_enabled), "max_send_enabled",
                BRSettingsItem.Type.SWITCH));

        items.add(new BRSettingsItem(getString(R.string.Settings_currency),
                BRSharedPrefs.getIso(this), v -> {
            Intent intent = new Intent(SettingsActivity.this, DisplayCurrencyActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, BRSettingsItem.Type.ITEM));

        items.add(new BRSettingsItem(getString(R.string.Settings_sync), "", v -> {
            Intent intent = new Intent(SettingsActivity.this,
                    SyncBlockchainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, BRSettingsItem.Type.ITEM));

        items.add(new BRSettingsItem(getString(R.string.Settings_about), "", v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }, BRSettingsItem.Type.ITEM));
    }
}