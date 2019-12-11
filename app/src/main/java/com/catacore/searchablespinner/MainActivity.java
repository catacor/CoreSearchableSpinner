package com.catacore.searchablespinner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.catacore.coresearchablespinner.CoreSearchableSpinner;
import com.catacore.coresearchablespinner.spinnerItem.model.SearchableItem;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<String> mStrings = new ArrayList<>();

    private CoreSearchableSpinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner = findViewById(R.id.my_spinner);
        spinner.setCurrentActivity(this);
        ArrayList<SearchableItem> items = new ArrayList<>();
        items.add(new SearchableItem("Steve", new ArrayList<>(Arrays.asList("Geeks", "for", "Geeks"))));
        items.add(new SearchableItem("Tim",new ArrayList<>(Arrays.asList("Gregor Clegane"))));
        items.add(new SearchableItem("Lucy",new ArrayList<>(Arrays.asList("Khal Drogo"))));
        items.add(new SearchableItem("Pat",new ArrayList<>(Arrays.asList("Cersei Lannister"))));
        items.add(new SearchableItem("Angela",new ArrayList<>(Arrays.asList("Sandor Clegane"))));
        items.add(new SearchableItem("Tom",new ArrayList<>(Arrays.asList("Tyrion Lannister", "Geeks"))));
        spinner.setItems(items);

    }

    @Override
    public void onBackPressed() {
        if(spinner.isContentVisible())
        {
            spinner.hideContent();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            hideKeyboard(this);
            spinner.hideContent();
        }

        return super.onTouchEvent(event);
    }

    public static void hideKeyboard(Activity activity) {
        View v = activity.getWindow().getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
