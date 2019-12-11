package com.catacore.coresearchablespinner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import java.util.ArrayList;


@SuppressLint("AppCompatCustomView")
public class ExtendedEditText extends EditText {

    public interface Listener{
        public void onKeyboardDispatch();
    }

    private ArrayList<Listener> listeners;

    public ExtendedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public ExtendedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public ExtendedEditText(Context context) {
        super(context);

    }

    public void registerListener(Listener listener)
    {
        if(listeners==null)
            listeners= new ArrayList<>();
        listeners.add(listener);
    }

    public void unregisterListener(Listener listener)
    {
        if(listeners!=null)
            listeners.remove(listener);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if(listeners!=null) {
                for (Listener listener : listeners) {
                    listener.onKeyboardDispatch();
                }
                return false;
            }
            else
                return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

}
