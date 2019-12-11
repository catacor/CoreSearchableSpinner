package com.catacore.coresearchablespinner.spinnerItem.adapters;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.catacore.coresearchablespinner.spinnerItem.model.SearchableItem;
import com.catacore.coresearchablespinner.spinnerItem.views.SearchableItemView;

import java.util.ArrayList;

public class SearchableItemsAdapter extends BaseAdapter implements SearchableItemView.Listener {
    private LayoutInflater mInflater;
    private ArrayList<SearchableItem> items = null;

    public SearchableItemsAdapter(Context context,ArrayList<SearchableItem> objects) {
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = objects;
    }




    @Override
    public int getCount() {
        if(items!=null)
            return items.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchableItem item = items.get(position);
        if(convertView==null)
        {
            SearchableItemView viewMvc = new SearchableItemView(mInflater,parent);
            viewMvc.registerListener(this);
            convertView = viewMvc.getRootView();
            convertView.setTag(viewMvc);
        }

        SearchableItemView viewMvc = (SearchableItemView)convertView.getTag();
        viewMvc.bindItem(item);

        return convertView;
    }


    @Override
    public void onItemClicked(SearchableItem item) {

    }

    public void clear() {
        if(items!=null)
            items.clear();
    }

    public void add(SearchableItem item)
    {
        if(items!=null)
            items.add(item);
    }
}

