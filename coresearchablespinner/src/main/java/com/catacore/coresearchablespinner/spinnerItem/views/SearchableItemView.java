package com.catacore.coresearchablespinner.spinnerItem.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.catacore.coresearchablespinner.R;
import com.catacore.coresearchablespinner.spinnerItem.model.SearchableItem;

import java.util.ArrayList;

public class SearchableItemView implements View.OnClickListener {
    LayoutInflater mInflater;
    View rootView;

    TextView displayName;
    SearchableItem item;
    private ArrayList<Listener> listeners = null;
    public SearchableItemView(LayoutInflater mInflater, ViewGroup parent) {
        rootView = mInflater.inflate(R.layout.core_search_spinner_item_layout,parent,false);

        this.mInflater = mInflater;
        this.listeners = new ArrayList<>();

        displayName = rootView.findViewById(R.id.core_search_item_text_view);
    }

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public View getRootView() {
        return rootView;
    }

    public void bindItem(SearchableItem item) {
        //TODO:: display
        displayName.setText(item.getDisplayText());
    }

    @Override
    public void onClick(View v) {
        for(Listener listener : listeners)
            listener.onItemClicked(item);
    }

    public interface Listener{
        void onItemClicked(SearchableItem item);
    }


}
