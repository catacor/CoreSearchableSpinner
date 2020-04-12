package com.catacore.coresearchablespinner;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;

import com.catacore.coresearchablespinner.R;
import com.catacore.coresearchablespinner.spinnerItem.adapters.SearchableItemsAdapter;
import com.catacore.coresearchablespinner.spinnerItem.model.SearchableItem;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class CoreSearchableSpinner extends RelativeLayout implements ExtendedEditText.Listener{
    private GestureDetector detector;

    private TextView promptTextView;
    private ImageView dropdownIcon;
    private RelativeLayout spinnerSearchLayout;
    private RelativeLayout displayLoayout;
    private ExtendedEditText searchInput;
//    private String items[];
    private ArrayList<SearchableItem> items;
//    private ArrayAdapter<String> itemsAdapter;
    private SearchableItemsAdapter itemsAdapter;
    private Activity currentActivity;
    private ListView contentList;

    private TextView emptyTag;
    private boolean dismissStarted;
    PopupWindow popupWindow;

    private static final int DefaultElevation = 16;

    int resIDdisplayBackground;
    int resIdDropdownRightIcon;
    int resIdSearchRightIcon;

    private Drawable displayBackground;
    private Drawable dropdownRightIcon;
    private Boolean displayDropdownRightIcon;
    private Drawable searchRightIcon;
    private Boolean displaySearchRightIcon;
    private String defaultDisplayText;
    private Boolean displayDefaultText;
    private Boolean displayResultTextOnSelect;
    private String defaultEmptyText;
    private Boolean displayEmptyText;
    private Integer topBottomTextPadding;

    private LinearLayout customLayoutList;
    Context mContext;

    private ArrayList<Listener> mListeners;

    private int selectedIndex;
    private SearchableItem selectedItem;

    public interface Listener{
        //will notify when the spinner is shown
        public void onSpinnerClicked();
        void onItemClicked(int position);
    }

    public void registerListener(Listener listener){
        if(mListeners!=null)
            mListeners.add(listener);
    }

    public void unregisterListener(Listener listener){
        if(mListeners!=null)
            mListeners.remove(listener);
    }

    public CoreSearchableSpinner(Context context) {
        this(context,null);
    }

    public CoreSearchableSpinner(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public CoreSearchableSpinner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }


    public CoreSearchableSpinner(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        getTypedArray(attrs);

        init();

    }

    public void setItems(ArrayList<SearchableItem> items){
        this.items = items;
        actualizeSpinner();
    }

    private void actualizeSpinner() {

        itemsAdapter = new SearchableItemsAdapter(mContext,(ArrayList<SearchableItem>) items.clone());
        contentList.setAdapter(itemsAdapter);

    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
        initializeIcons();

    }



    private void initializeIcons() {
        if(resIDdisplayBackground!=-1)
            displayBackground = currentActivity.getDrawable(resIDdisplayBackground);
        if(resIdDropdownRightIcon!=-1)
            dropdownRightIcon = currentActivity.getDrawable(resIdDropdownRightIcon);
        if(resIdSearchRightIcon!=-1)
            searchRightIcon = currentActivity.getDrawable(resIdSearchRightIcon);

        if(displaySearchRightIcon==false) {
            searchInput.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            searchInput.setPadding(0,topBottomTextPadding,0,topBottomTextPadding);
        }
        if(displayDropdownRightIcon==false) {
            promptTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            promptTextView.setPadding(0,topBottomTextPadding,0,topBottomTextPadding);
        }

        if(displayBackground!=null)
        {
            promptTextView.setBackgroundResource(resIDdisplayBackground);
        }

        if(dropdownRightIcon != null)
        {
            promptTextView.setCompoundDrawablesWithIntrinsicBounds(null,null,dropdownRightIcon,null);
        }
        if(displayBackground!=null)
        {
            searchInput.setBackground(displayBackground);
        }

        if(searchRightIcon != null)
        {
            searchInput.setCompoundDrawablesWithIntrinsicBounds(null,null,searchRightIcon,null);
        }
    }


    private void getTypedArray(AttributeSet attrs) {
        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CoreSearchableSpinner,
                0, 0);

        try {
            resIDdisplayBackground = typedArray.getResourceId(R.styleable.CoreSearchableSpinner_displayBackground,-1);
            resIdDropdownRightIcon = typedArray.getResourceId(R.styleable.CoreSearchableSpinner_dropdownRightIcon,-1);
            displayDropdownRightIcon = typedArray.getBoolean(R.styleable.CoreSearchableSpinner_displayDropdownRightIcon,true);
            resIdSearchRightIcon = typedArray.getResourceId(R.styleable.CoreSearchableSpinner_searchRightIcon,-1);
            displaySearchRightIcon = typedArray.getBoolean(R.styleable.CoreSearchableSpinner_displaySearchRightIcon,true);
            defaultDisplayText = typedArray.getString(R.styleable.CoreSearchableSpinner_defaultDisplayText);
            displayDefaultText = typedArray.getBoolean(R.styleable.CoreSearchableSpinner_displayDefaultText,true);
            displayResultTextOnSelect = typedArray.getBoolean(R.styleable.CoreSearchableSpinner_displayResultTextOnSelect,true);
            defaultEmptyText = typedArray.getString(R.styleable.CoreSearchableSpinner_defaultEmptyText);
            displayEmptyText = typedArray.getBoolean(R.styleable.CoreSearchableSpinner_displayEmptyText,true);
            topBottomTextPadding = typedArray.getInteger(R.styleable.CoreSearchableSpinner_topBottomTextPadding,0);
        } finally {
            typedArray.recycle();
        }
    }

    public int getSelectedIndex(){
        return selectedIndex;
    }

    public SearchableItem getSelectedItem(){
        return selectedItem;
    }

    private static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void hideKeyboard() {
        View v = currentActivity.getWindow().getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) currentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void init() {
        mListeners = new ArrayList<>();
        detector = new GestureDetector(this.getContext(),new MyListener());
        selectedIndex=-1;
        selectedItem = null;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.core_search_spinner_layout,this);

        displayLoayout = findViewById(R.id.custom_spinner_display_layout);

        promptTextView = findViewById(R.id.custom_spinner_text_view);

        if(displayBackground!=null)
        {
            promptTextView.setBackground(displayBackground);
        }

        if(dropdownRightIcon != null)
        {
            promptTextView.setCompoundDrawables(null,null,dropdownRightIcon,null);
        }

        if(displayDefaultText)
        {
            if( defaultDisplayText!=null &&  !defaultDisplayText.isEmpty())
            {
                promptTextView.setText(defaultDisplayText);
            }
            else
            {
                promptTextView.setText("Click to open");
            }
        }

        promptTextView.setVisibility(VISIBLE);

        spinnerSearchLayout = findViewById(R.id.custom_spinner_search_layout);
        spinnerSearchLayout.setVisibility(GONE);


        //setting the container
        popupWindow = new PopupWindow(mContext);

        customLayoutList = (LinearLayout) inflater.inflate(R.layout.core_search_spinner_layout_list,this, false);
        popupWindow.setContentView(customLayoutList);

        contentList = popupWindow.getContentView().findViewById(R.id.core_search_container_list_view);
        contentList.setAdapter(itemsAdapter);

        contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(itemsAdapter!=null) {
                    //TODO: add onItemSelected
                    selectedIndex = position;
                    selectedItem = (SearchableItem)itemsAdapter.getItem(position);
                    hideKeyboard();
                    for(Listener listener:mListeners)
                        listener.onItemClicked(position);
                    if(displayResultTextOnSelect)
                        promptTextView.setText( ((SearchableItem)itemsAdapter.getItem(position)).getDisplayText());
                    hideContent();
                }
            }
        });

        emptyTag = popupWindow.getContentView().findViewById(R.id.core_search_container_empty_tag);

        if(displayEmptyText)
        {
            if(defaultEmptyText!=null && !defaultEmptyText.isEmpty())
            {
                emptyTag.setText(defaultDisplayText);
            }
            else
            {
                emptyTag.setText(mContext.getString(R.string.empty_list));
            }
        }

        displayLoayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {//called once, after drawing is ready
                displayLoayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //use it to set the same width on list
                int spinnerWidth = displayLoayout.getWidth();
                popupWindow.setWidth(spinnerWidth);
            }
        });

        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(spinnerSearchLayout.getVisibility() == VISIBLE) {
                    resetOnFocusListener();
                    dismissStarted = true;
//                    popupWindow.dismiss();
                    searchInput.setText("");
                    spinnerSearchLayout.setVisibility(GONE);
                    invalidate();
                    requestLayout();
                }



            }
        });

        popupWindow.setTouchInterceptor(new OnTouchListener()
        {

            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE)
                {
                    popupWindow.dismiss();
                    return true;
                }

                return false;
            }
        });

//        popupWindow.setTouchInterceptor(new OnTouchListener()
//        {
//
//            public boolean onTouch(View v, MotionEvent event)
//            {
//                Rect editTextRect = new Rect();
//                displayLoayout.getHitRect(editTextRect);
//
//                Rect editTextRect2 = new Rect();
//                contentList.getHitRect(editTextRect2);
//
//                Log.i("TOUCH","x=" + editTextRect.left);
//                Log.i("TOUCH","y=" + editTextRect.top);
//                Log.i("TOUCH","x=" + editTextRect.right);
//                Log.i("TOUCH","y=" + editTextRect.bottom);
//
//                Log.i("TOUCH","x2=" + editTextRect2.left);
//                Log.i("TOUCH","y2=" + editTextRect2.top);
//                Log.i("TOUCH","x2=" + editTextRect2.right);
//                Log.i("TOUCH","y2=" + editTextRect2.bottom);
//
//                Log.i("TOUCH","mx=" + event.getX());
//                Log.i("TOUCH","my=" + event.getY());
//
//                if(event.getX() >= editTextRect.left && event.getX() <= editTextRect.right) {
//                    if (event.getY() >= editTextRect2.top && event.getY() <= editTextRect.bottom + editTextRect2.bottom) {
//                        //inside touch
//                    } else {
//                        resetOnFocusListener();
//                        dismissStarted = true;
//                        popupWindow.dismiss();
//                        searchInput.setText("");
//                        spinnerSearchLayout.setVisibility(GONE);
//                        invalidate();
//                        requestLayout();
//
//                        return false;
//                    }
//                }
//
//
//                return true;
//            }
//        });

        popupWindow.setFocusable(true);
        popupWindow.setElevation(DefaultElevation);
        popupWindow.setBackgroundDrawable(mContext.getDrawable(R.drawable.spinner_drawable));
        popupWindow.setOutsideTouchable(true);


        searchInput = findViewById(R.id.custom_spinner_search_input);

        if(displayBackground!=null)
        {
            searchInput.setBackground(displayBackground);
        }

        if(searchRightIcon != null)
        {
            searchInput.setCompoundDrawables(null,null,searchRightIcon,null);
        }
        searchInput.registerListener(this);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(itemsAdapter!=null) {
                    if (dismissStarted == true) {
                        //we just cleared the input
                        //reset the search
                        itemsAdapter.clear();
                        for (int i = 0; i < items.size(); i++) {
                            itemsAdapter.add(items.get(i));
                        }

                        dismissStarted = false;
                        return;
                    }

                    //rewrite adapter items and update the list
                    ArrayList<SearchableItem> beforeItems = new ArrayList<>();
                    for (int i = 0; i < itemsAdapter.getCount(); i++) {
                        beforeItems.add((SearchableItem) itemsAdapter.getItem(i));
                    }

                    ArrayList<SearchableItem> newItems = new ArrayList<>();
//                itemsAdapter.clear();
                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getDisplayText().toLowerCase().contains(s.toString().toLowerCase())) {
                            newItems.add(items.get(i));
                        } else {
                            ArrayList<String> tags = items.get(i).getTags();
                            for (int j = 0; j < tags.size(); j++) {
                                if (tags.get(j).toLowerCase().contains(s.toString().toLowerCase())) {
                                    newItems.add(items.get(i));
                                }
                            }
                        }
                    }

                    boolean isEqual = true;
                    if (beforeItems.size() == newItems.size()) {
                        Iterator<SearchableItem> iterator = beforeItems.iterator();

                        while (iterator.hasNext()) {
                            SearchableItem temp = iterator.next();
                            if (!newItems.contains(temp)) {
                                isEqual = false;
                                break;
                            }
                        }
                    } else {
                        isEqual = false;
                    }


                    if (!isEqual) {
                        itemsAdapter.clear();
                        for (int i = 0; i < newItems.size(); i++) {
                            itemsAdapter.add(newItems.get(i));
                        }
                        itemsAdapter.notifyDataSetChanged();
                        recalculateContentAndShow();
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        resetOnFocusListener();



    }

    private  void resetOnFocusListener(){
        searchInput.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {


                    if(currentActivity==null) {
                        Toast.makeText(mContext,"Set the activity dependencies!",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    popupWindow.dismiss();
                    searchInput.setOnFocusChangeListener(null);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int totalListHeigh = getListHeigh();
                            int distanceTop = getDistanceToTop();
                            int distanceBottom = getDistanceToBottom();


                            //display to top
                            int totalItems= 0;
                            if(itemsAdapter!=null)
                                totalItems = itemsAdapter.getCount();

                            int itemSize;
                            int maxItems;

                            if(totalItems == 0)
                            {
                                ViewGroup.LayoutParams params = contentList.getLayoutParams();
                                params.height = 0;
                                contentList.setLayoutParams(params);
                                contentList.requestLayout();

                                spinnerSearchLayout.bringToFront();
                                spinnerSearchLayout.setVisibility(VISIBLE);
                                popupWindow.showAsDropDown(CoreSearchableSpinner.this, displayLoayout.getLeft(),  - displayLoayout.getMeasuredHeight());
                            }
                            else
                            {
                                itemSize = totalListHeigh/totalItems;
                                maxItems = distanceTop/itemSize;

                                maxItems = maxItems < totalItems ? maxItems : totalItems;

                                ViewGroup.LayoutParams params = contentList.getLayoutParams();
                                params.height = maxItems*itemSize + (contentList.getDividerHeight() * (maxItems-1));
                                contentList.setLayoutParams(params);
                                contentList.requestLayout();

                                spinnerSearchLayout.bringToFront();
                                spinnerSearchLayout.setVisibility(VISIBLE);
                                popupWindow.showAsDropDown(CoreSearchableSpinner.this, displayLoayout.getLeft(), - maxItems*itemSize - displayLoayout.getMeasuredHeight());
                            }

                        }
                    }, 100);

                }
            }
        });
    }

    public void setSelectedSpinnerItem(String displayText)
    {
        if(displayText!=null) {
            int pos = 0;
            for (SearchableItem item : items) {
                if (item.getDisplayText().equals(displayText)) {
                    selectedIndex = pos;
                    selectedItem = (SearchableItem) itemsAdapter.getItem(pos);
                    if(displayResultTextOnSelect)
                        promptTextView.setText(((SearchableItem) itemsAdapter.getItem(pos)).getDisplayText());
                    return;
                }
                pos++;
            }
        }
    }

    public void setSelectedSpinnerItem(Integer position)
    {
        if(position!=null) {
            if (position >= 0 && position < items.size()) {

                selectedIndex = position;
                selectedItem = (SearchableItem) itemsAdapter.getItem(position);
                if(displayResultTextOnSelect)
                    promptTextView.setText(((SearchableItem) itemsAdapter.getItem(position)).getDisplayText());
                return;

            }
        }
    }

    public void setSelectedSpinnerItem(SearchableItem _item)
    {
        if(_item!=null) {
            int pos = 0;
            for (SearchableItem item : items) {
                if (item.getDisplayText().equals(_item.getDisplayText())) {
                    if (item.getTags().equals(_item.getTags())) {
                        selectedIndex = pos;
                        selectedItem = (SearchableItem) itemsAdapter.getItem(pos);
                        if(displayResultTextOnSelect)
                            promptTextView.setText(((SearchableItem) itemsAdapter.getItem(pos)).getDisplayText());
                        return;
                    }
                }
                pos++;
            }
        }

    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = detector.onTouchEvent(event);
        if(!result){
            if(event.getAction() == MotionEvent.ACTION_UP)
            {

                if(currentActivity==null) {
                    Toast.makeText(mContext,"Set the activity dependencies!",Toast.LENGTH_SHORT).show();
                    return false;
                }
                if(mListeners!=null)
                    for(Listener listener : mListeners)
                        listener.onSpinnerClicked();
                //should show dropdown
                showContent();

                return true;
            }
            if(event.getAction() == MotionEvent.ACTION_DOWN)
            {
//                if(spinnerSearchLayout.getVisibility() == VISIBLE) {
//                    //de verificat daca e in edit text
//                }


                //instintez ca vreau sa ascult si urmatoarele evenimente
                return true;
            }
        }
        return result;

    }



    private void recalculateContentAndShow(){
        int totalListHeigh = getListHeigh();
        int distanceTop = getDistanceToTop();

        //display to top
        int totalItems=0;
        if(itemsAdapter!=null)
            totalItems = itemsAdapter.getCount();

        if(totalItems == 0)
        {


            if(displayEmptyText) {
                //show empty tag
                contentList.setVisibility(GONE);
                emptyTag.setVisibility(VISIBLE);

                int desiredWidth = MeasureSpec.makeMeasureSpec(contentList.getWidth(), MeasureSpec.UNSPECIFIED);
//            emptyTag.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                emptyTag.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
                int emptyTagHeight = emptyTag.getMeasuredHeight();

                popupWindow.dismiss();
                popupWindow.showAsDropDown(CoreSearchableSpinner.this, displayLoayout.getLeft(), -emptyTagHeight - displayLoayout.getMeasuredHeight());
            }
            else
            {
                contentList.setVisibility(GONE);
                popupWindow.dismiss();
                popupWindow.showAsDropDown(CoreSearchableSpinner.this, displayLoayout.getLeft(),  - displayLoayout.getMeasuredHeight());
            }

        }
        else
        {
            contentList.setVisibility(VISIBLE);
            emptyTag.setVisibility(GONE);

            int itemSize = totalListHeigh / totalItems;
            int maxItems = distanceTop / itemSize;

            maxItems = maxItems < totalItems ? maxItems : totalItems;

            popupWindow.dismiss();

            ViewGroup.LayoutParams params = contentList.getLayoutParams();
            params.height = maxItems * itemSize + (contentList.getDividerHeight() * (maxItems - 1));
            contentList.setLayoutParams(params);
            contentList.requestLayout();

            spinnerSearchLayout.bringToFront();
            spinnerSearchLayout.setVisibility(VISIBLE);

            popupWindow.dismiss();
            popupWindow.showAsDropDown(CoreSearchableSpinner.this, displayLoayout.getLeft(), - maxItems*itemSize - displayLoayout.getMeasuredHeight());
        }


    }



    private void showContent(){
        int totalListHeigh = getListHeigh();
        int distanceTop = getDistanceToTop();
        int distanceBottom = getDistanceToBottom();
        if(distanceBottom > distanceTop)
        {
            //display to bottom
            //check how many items cand i fit
            int totalItems=0;
            if(itemsAdapter!=null)
                totalItems = itemsAdapter.getCount();


            if(totalItems == 0)
            {
                if(displayEmptyText) {
                    //show empty tag
                    contentList.setVisibility(GONE);
                    emptyTag.setVisibility(VISIBLE);

                    int desiredWidth = MeasureSpec.makeMeasureSpec(contentList.getWidth(), MeasureSpec.UNSPECIFIED);
//                emptyTag.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    emptyTag.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
                    int emptyTagHeight = emptyTag.getMeasuredHeight();

                    popupWindow.dismiss();
                    popupWindow.showAsDropDown(this, displayLoayout.getLeft(), 0);
                }
                else
                {
                    contentList.setVisibility(GONE);
                    popupWindow.dismiss();
                    popupWindow.showAsDropDown(this, displayLoayout.getLeft(), 0);
                }

            }
            else {
                contentList.setVisibility(VISIBLE);
                emptyTag.setVisibility(GONE);


                int itemSize = totalListHeigh / totalItems;
                int maxItems = distanceBottom / itemSize;

                maxItems = maxItems < totalItems ? maxItems : totalItems;

                ViewGroup.LayoutParams params = contentList.getLayoutParams();
                params.height = maxItems * itemSize + (contentList.getDividerHeight() * (maxItems - 1));
                contentList.setLayoutParams(params);
                contentList.requestLayout();

                spinnerSearchLayout.bringToFront();
                spinnerSearchLayout.setVisibility(VISIBLE);
                popupWindow.showAsDropDown(this, displayLoayout.getLeft(), 0);
            }
        }
        else {
            //display to top
            int totalItems=0;
            if(itemsAdapter!=null)
                totalItems = itemsAdapter.getCount();


            if(totalItems == 0)
            {
                if(displayEmptyText) {
                    //show empty tag
                    contentList.setVisibility(GONE);
                    emptyTag.setVisibility(VISIBLE);

                    int desiredWidth = MeasureSpec.makeMeasureSpec(contentList.getWidth(), MeasureSpec.UNSPECIFIED);
//                emptyTag.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    emptyTag.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
                    int emptyTagHeight = emptyTag.getMeasuredHeight();

                    popupWindow.dismiss();
                    popupWindow.showAsDropDown(CoreSearchableSpinner.this, displayLoayout.getLeft(), -emptyTagHeight - displayLoayout.getMeasuredHeight());
                }
                else
                {
                    contentList.setVisibility(GONE);
                    popupWindow.dismiss();
                    popupWindow.showAsDropDown(CoreSearchableSpinner.this, displayLoayout.getLeft(),  - displayLoayout.getMeasuredHeight());
                }

            }
            else {
                contentList.setVisibility(VISIBLE);
                emptyTag.setVisibility(GONE);

                int itemSize = totalListHeigh / totalItems;
                int maxItems = distanceTop / itemSize;

                maxItems = maxItems < totalItems ? maxItems : totalItems;

                ViewGroup.LayoutParams params = contentList.getLayoutParams();
                params.height = maxItems * itemSize + (contentList.getDividerHeight() * (maxItems - 1));
                contentList.setLayoutParams(params);
                contentList.requestLayout();

                spinnerSearchLayout.bringToFront();
                spinnerSearchLayout.setVisibility(VISIBLE);
                popupWindow.showAsDropDown(CoreSearchableSpinner.this, displayLoayout.getLeft(), -maxItems * itemSize - displayLoayout.getMeasuredHeight());
            }
        }
    }
    public void hideContent(){
        if(spinnerSearchLayout.getVisibility() == VISIBLE) {
            resetOnFocusListener();
            dismissStarted = true;
            popupWindow.dismiss();
            searchInput.setText("");
            spinnerSearchLayout.setVisibility(GONE);
            invalidate();
            requestLayout();
        }

    }

    public boolean isContentVisible() {
        if(spinnerSearchLayout.getVisibility() == VISIBLE)
            return true;
        return false;
    }

    @Override
    public void onKeyboardDispatch() {
        hideContent();
    }

    class MyListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }
    }


    private int getDistanceToBottom(){
        // instantiate DisplayMetrics
        DisplayMetrics dm = new DisplayMetrics();
        // fill dm with data from current display
        currentActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        // loc will hold the coordinates of your view
        int[] loc = new int[2];
        // fill loc with the coordinates of your view (loc[0] = x, looc[1] = y)
        displayLoayout.getLocationOnScreen(loc);
        // calculate the distance from the TOP(its y-coordinate) of your view to the bottom of the screen
        int distance = dm.heightPixels - loc[1] - displayLoayout.getMeasuredHeight();

        return distance;
    }

    private int getDistanceToTop(){
        // instantiate DisplayMetrics
        DisplayMetrics dm = new DisplayMetrics();
        // fill dm with data from current display
        currentActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        // loc will hold the coordinates of your view
        int[] loc = new int[2];
        // fill loc with the coordinates of your view (loc[0] = x, looc[1] = y)
        displayLoayout.getLocationOnScreen(loc);
        // calculate the distance from the TOP(its y-coordinate) of your view to the bottom of the screen
        int distance = loc[1];

        return distance;
    }

    private int getListHeigh(){

        int desiredWidth = MeasureSpec.makeMeasureSpec(contentList.getWidth(), MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        if(itemsAdapter!=null && itemsAdapter.getCount()>0) {
            View listItem = itemsAdapter.getView(0, null, contentList);

            if (listItem != null) {
                // This next line is needed before you call measure or else you won't get measured height at all. The listitem needs to be drawn first to know the height.
                listItem.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
                totalHeight = listItem.getMeasuredHeight() * itemsAdapter.getCount();

            }

        }
        return totalHeight;
    }
}
