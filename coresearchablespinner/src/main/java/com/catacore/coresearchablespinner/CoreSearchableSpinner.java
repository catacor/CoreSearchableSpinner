package com.catacore.coresearchablespinner;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.catacore.coresearchablespinner.spinnerItem.adapters.SearchableItemsAdapter;
import com.catacore.coresearchablespinner.spinnerItem.model.SearchableItem;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.Unregistrar;

import java.util.ArrayList;
import java.util.Iterator;

public class CoreSearchableSpinner  extends RelativeLayout {

    public interface Listener{
        void onSpinnerClicked();
        void onSpinnerShown();
        void onSpinnerDismissed();
        void onItemClicked(int position);
    }
    private ArrayList<Listener> mListeners;

    private Context mContext;
    private RelativeLayout fullSpinnerRelativeLayout;
    private EditText inputEditText;
    private TextView displayTextView;
    private ImageView displayIcon;

    //display list items
    private TextView emptyTag;
    private PopupWindow popupWindow;
    private LinearLayout customLayoutList;
    private ListView contentList;
    private Unregistrar unregistrar;

    private static final int DefaultElevation = 16;
    private LayoutInflater inflater;
    private SearchableItemsAdapter itemsAdapter;
    private ArrayList<SearchableItem> items;
    private int selectedIndex;
    private SearchableItem selectedItem;

    private Activity mActivity;

    private boolean wasEditTextClicked;
    private boolean isSpinnerOpen;
    private boolean isPopupOpen;
    private boolean isKeyboardOpen;
    private boolean redrawOnDispatchKeyboard;
    private boolean redrawOnItemsChanged;
    private boolean dismissStarted;
    private int[] locationsBeforeOnTouch = new int[2];

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

//        mActivity = (Activity) mContext;
        init();
    }

    public void registerListener(Listener listener){
        if(mListeners!=null)
            mListeners.add(listener);
    }

    public void unregisterListener(Listener listener){
        if(mListeners!=null)
            mListeners.remove(listener);
    }


    private void getTypedArray(AttributeSet attrs) {
        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CoreSearchableSpinner,
                0, 0);
    }

    public void setItems(ArrayList<SearchableItem> items){
        this.items = items;
        actualizeSpinnerItems();


//        KeyboardVisibilityEvent.setEventListener(mActivity, new KeyboardVisibilityEventListener() {
//            @Override
//            public void onVisibilityChanged(boolean b) {
//                if(b)
//                {
//                    Log.d("CORE_SPINNER","Keyboard open");
//                    isKeyboardOpen = true;
//                    Handler handler = new Handler();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(isPopupOpen)
//                            {
//                                redrawOnDispatchKeyboard = true;
//                                popupWindow.dismiss();
//                                redrawPopupList();
//                            }
//                        }
//                    }, 100);
//
//
//                }
//                else
//                {
//                    Log.d("CORE_SPINNER","Keyboard close");
//                    isKeyboardOpen = false;
//                    if(isPopupOpen)
//                    {
//
//                        redrawOnDispatchKeyboard = true;
//                        popupWindow.dismiss();
//                        redrawPopupList();
//
//                    }
//
//                }
//            }
//        });

        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        registerKeyboardListener();
    }

    private void registerKeyboardListener(){
        if(unregistrar!=null) {
            unregistrar.unregister();
            unregistrar = null;
        }



        KeyboardVisibilityEvent keyboardVisibilityEvent = KeyboardVisibilityEvent.INSTANCE;
        unregistrar = keyboardVisibilityEvent.registerEventListener(mActivity, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean b) {
//                registerKeyboardListener();
                if (b) {
                    if(isKeyboardOpen)
                    {
                        //bug on keyboard dismissing
                        Log.d("CORE_SPINNER", "Keyboard bug");
                        inputEditText.clearFocus();
                        inputEditText.requestFocus();
                        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);

                    }

                    Log.d("CORE_SPINNER", "Keyboard open");
                    isKeyboardOpen = true;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isPopupOpen) {
                                redrawOnDispatchKeyboard = true;
                                popupWindow.dismiss();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        redrawPopupList();
                                    }
                                }, 200);

                            }
                        }
                    }, 100);


                } else {
                    Log.d("CORE_SPINNER", "Keyboard close");
                    isKeyboardOpen = false;
                    if (isPopupOpen) {

                        redrawOnDispatchKeyboard = true;
                        popupWindow.dismiss();

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                redrawPopupList();
                            }
                        }, 200);

                    }

                }
            }
        });


    }

    private void init() {
        //zona unde initize comportamentul si view-urile
        mActivity = (Activity) mContext;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.core_search_spinner_layout,this);

        fullSpinnerRelativeLayout = findViewById(R.id.spinner_search_layout);
        inputEditText = findViewById(R.id.spinner_input_edit_text);
        displayTextView = findViewById(R.id.spinner_display_text_view);
        displayIcon = findViewById(R.id.spinner_display_icon);

        initVariables();
        setBehaviour();
    }

    private void initVariables() {
        isSpinnerOpen = false;
        isPopupOpen = false;
        wasEditTextClicked = false;
        isKeyboardOpen = false;
        redrawOnDispatchKeyboard = false;
        redrawOnItemsChanged = false;
        dismissStarted = false;
        locationsBeforeOnTouch[0]=0;
        locationsBeforeOnTouch[1]=0;

        selectedIndex=-1;
        selectedItem = null;

        mListeners = new ArrayList<>();
        popupWindow = new PopupWindow(mContext);
        customLayoutList = (LinearLayout) inflater.inflate(R.layout.core_search_spinner_layout_list,this, false);
        popupWindow.setContentView(customLayoutList);
//        popupWindow.setFocusable(true);
        popupWindow.setElevation(DefaultElevation);
        popupWindow.setBackgroundDrawable(mContext.getDrawable(R.drawable.spinner_drawable));
        popupWindow.setOutsideTouchable(true);

        contentList = popupWindow.getContentView().findViewById(R.id.core_search_container_list_view);

        items = new ArrayList<>();
        itemsAdapter = new SearchableItemsAdapter(mContext,(ArrayList<SearchableItem>) items.clone());
        contentList.setAdapter(itemsAdapter);

    }

    private void setBehaviour() {
        fullSpinnerRelativeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openSpinner();
            }
        });

        fullSpinnerRelativeLayout.setFocusableInTouchMode(true);

        fullSpinnerRelativeLayout.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                return interceptKeyEvent(view,keyCode,keyEvent);
            }
        });

        inputEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                return interceptKeyEvent(view,keyCode,keyEvent);
            }
        });

        fullSpinnerRelativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {//called once, after drawing is ready
                fullSpinnerRelativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                //use it to set the same width on list
                int spinnerWidth = fullSpinnerRelativeLayout.getWidth();
                popupWindow.setWidth(spinnerWidth);
                popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
            }
        });

        inputEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                {

                    saveLocations();
                    Log.d("CORE_SPINNER","Edit text has focus");
                    wasEditTextClicked = true;
                }
                else
                {
                    Log.d("CORE_SPINNER","Edit text has lost focus");
                }
            }
        });

        inputEditText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLocations();
                Log.d("CORE_SPINNER","Edit text clicked");
                wasEditTextClicked = true;
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                isPopupOpen = false;
                if(!wasEditTextClicked) {
                    if (isSpinnerOpen) {
                        if(!redrawOnDispatchKeyboard && !redrawOnItemsChanged) {
                            dismissStarted = true;
                            inputEditText.setText("");
                            closeSpinner();
                        }
                        else
                            if(redrawOnDispatchKeyboard || redrawOnItemsChanged){
                                if(redrawOnDispatchKeyboard) {
                                    redrawOnDispatchKeyboard = false;
                                }
                                else {
                                    redrawOnItemsChanged = false;
                                }
                            }

                    }
                }
            }
        });

        popupWindow.setTouchInterceptor(new OnTouchListener()
        {
            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE)
                {
                    Log.d("CORE_SPINNER","tapped outside");
                    if(isKeyboardOpen && event.getY()==0){
                        //dont dispathc
                    }
                    else
                    {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(wasEditTextClicked){
                                    //do nothing
                                    //reset the flag
                                    Log.d("CORE_SPINNER","the tap was in the input");
                                    wasEditTextClicked = false;
                                }
                                else
                                {
                                    Log.d("CORE_SPINNER","the tap was outside the input");
                                    popupWindow.dismiss();
                                }
                            }
                        }, 100);
                    }
                    return true;
                }
                return false;
            }
        });

        contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(itemsAdapter!=null) {
                    //TODO: add onItemSelected
                    selectedIndex = position;
                    selectedItem = (SearchableItem)itemsAdapter.getItem(position);

                    for(Listener listener: mListeners)
                        listener.onItemClicked(position);

                    displayTextView.setText( ((SearchableItem)itemsAdapter.getItem(position)).getDisplayText());
                    closeSpinner();
                }
            }
        });

        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Log.d("CORE_SPINNER","on text edit called with the text \""+s+"\"");
                if(itemsAdapter!=null) {

                    if (dismissStarted == true) {
                        //we just cleared the input
                        //reset the search
                        resetListItems();
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
                                    break;
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
                        redrawOnItemsChanged = true;
                        popupWindow.dismiss();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                redrawPopupList();
                            }
                        }, 200);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    private void resetListItems() {
        Log.d("CORE_SPINNER","RESETTING ITEMS");
        itemsAdapter.clear();
        for (int i = 0; i < items.size(); i++) {
            itemsAdapter.add(items.get(i));
        }
        itemsAdapter.notifyDataSetChanged();
        dismissStarted = false;
    }

    private void saveLocations() {
        if(!isKeyboardOpen)
            fullSpinnerRelativeLayout.getLocationOnScreen(locationsBeforeOnTouch);
        Log.d("CORE_SPINNER","Locations before keyboard: X: "+locationsBeforeOnTouch[0] +" Y:"+locationsBeforeOnTouch[1]);
    }

    private void redrawPopupList() {

        registerKeyboardListener();

        Log.d("CORE_SPINNER","REDRAW");
        int totalListHeigh = getListHeigh();
        int distanceTop = getDistanceToTop();
        int distanceBottom = getDistanceToBottom();


        int totalItems=0;
        if(itemsAdapter!=null)
            totalItems = itemsAdapter.getCount();

        if(distanceBottom > distanceTop)
        {
            int itemSize=0;
            int maxItems=0;
            if(totalItems == 0) {
                itemSize = 0;
                maxItems = 0;
            }
            else
            {
                itemSize = totalListHeigh / totalItems;
                maxItems = distanceBottom / itemSize;
            }

            maxItems = maxItems < totalItems ? maxItems : totalItems;

            ViewGroup.LayoutParams params = contentList.getLayoutParams();
            params.height = maxItems * itemSize + (contentList.getDividerHeight() * (maxItems - 1));
            contentList.setLayoutParams(params);
            contentList.requestLayout();

            popupWindow.showAsDropDown(this, fullSpinnerRelativeLayout.getLeft(), 0);
            isPopupOpen = true;
        }
        else
        {
            int itemSize=0;
            int maxItems=0;
            if(totalItems == 0) {
                itemSize = 0;
                maxItems = 0;
            }
            else
            {
                itemSize = totalListHeigh / totalItems;
                maxItems = distanceTop / itemSize;
            }

            maxItems = maxItems < totalItems ? maxItems : totalItems;

            ViewGroup.LayoutParams params = contentList.getLayoutParams();
            params.height = maxItems * itemSize + (contentList.getDividerHeight() * (maxItems - 1));
            contentList.setLayoutParams(params);
            contentList.requestLayout();


            View v = CoreSearchableSpinner.this;
            int[] loc = new int[2];
            fullSpinnerRelativeLayout.getLocationOnScreen(loc);

            Log.d("CORE_SPINNER","Locations after keyboard: X: "+loc[0] +" Y:"+loc[1]);
            int decalajResizePan =0;

            if(isKeyboardOpen)
                decalajResizePan = Math.abs(locationsBeforeOnTouch[1] - loc[1]);

//            if(decalajResizePan!=0)
//            {
//                registerKeyboardListener();
//            }

            int offsetY = -maxItems * itemSize - fullSpinnerRelativeLayout.getMeasuredHeight() - decalajResizePan;
            popupWindow.showAsDropDown(fullSpinnerRelativeLayout, 0 ,offsetY);

            isPopupOpen = true;
        }
    }



    private boolean interceptKeyEvent(View view, int keyCode, KeyEvent keyEvent) {
        Log.d("CORE_SPINNER","key interceptor");
        if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

            if (isSpinnerOpen) {
                closeSpinner();
//                popupWindow.dismiss();
                return true;
            } else
                return false;
        }

        return false;
    }

    private void openSpinner() {
        Log.d("CORE_SPINNER","spinner opened");
        displayTextView.setVisibility(GONE);
        inputEditText.setVisibility(VISIBLE);
        isSpinnerOpen = true;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                redrawPopupList();
            }
        }, 200);

    }

    private void closeSpinner() {
        Log.d("CORE_SPINNER","spinner closed");
        displayTextView.setVisibility(VISIBLE);
        inputEditText.setVisibility(GONE);
        if(isPopupOpen)
            popupWindow.dismiss();
        if(isKeyboardOpen)
            hideKeyboard();
        isSpinnerOpen = false;
    }

    private void actualizeSpinnerItems() {
        itemsAdapter = new SearchableItemsAdapter(mContext,(ArrayList<SearchableItem>) items.clone());
        contentList.setAdapter(itemsAdapter);
    }


    private int getDistanceToBottom(){
        Rect r = new Rect();
        View rootView = mActivity.getWindow().getDecorView();
        rootView.getWindowVisibleDisplayFrame(r);

        // instantiate DisplayMetrics
        DisplayMetrics dm = new DisplayMetrics();
        // fill dm with data from current display
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        // loc will hold the coordinates of your view
        int[] loc = new int[2];
        // fill loc with the coordinates of your view (loc[0] = x, looc[1] = y)
        fullSpinnerRelativeLayout.getLocationOnScreen(loc);

        if(locationsBeforeOnTouch[1] > loc[1])
        {
            Log.d("CORE_SPINNER","input under keyboard");
        }
        // calculate the distance from the TOP(its y-coordinate) of your view to the bottom of the screen
//        int distance = dm.heightPixels - loc[1] - fullSpinnerRelativeLayout.getMeasuredHeight();
        int distance = r.bottom - loc[1] - fullSpinnerRelativeLayout.getMeasuredHeight();

        return distance;
    }

    private int getDistanceToTop(){
        Log.d("CORE_SPINNER","Calculate distance to top");
        Rect r = new Rect();
        View rootView = mActivity.getWindow().getDecorView();
        rootView.getWindowVisibleDisplayFrame(r);

        // instantiate DisplayMetrics
        DisplayMetrics dm = new DisplayMetrics();
        // fill dm with data from current display
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        // loc will hold the coordinates of your view
        int[] loc = new int[2];
        // fill loc with the coordinates of your view (loc[0] = x, looc[1] = y)
        fullSpinnerRelativeLayout.getLocationOnScreen(loc);
        int distanceViewKeyboard =0;
        if(locationsBeforeOnTouch[1] > loc[1])
        {
            Log.d("CORE_SPINNER","input under keyboard - reacalculating top distance");
            //recalculate the heigh
            distanceViewKeyboard =locationsBeforeOnTouch[1] - loc[1] ;
        }

        // calculate the distance from the TOP(its y-coordinate) of your view to the bottom of the screen
        int distance = loc[1] - distanceViewKeyboard;

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


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

}

