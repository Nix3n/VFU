package com.example.martin.vfu;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Martin on 2016-05-16.
 */
public class GUI {

    public int width, height;
    public Context context;
    private CheckBox box;

    public GUI(Context context){
        this.context = context;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); // the results will be higher than using the activity context object or the getWindowManager() shortcut
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }

    public Button arrow(int style){
        Button btn = new Button(context);

        btn.setBackgroundResource(style);

        return btn;
    }

    public Button smallBtn(String text, int style, int smallWidth, int smallHeight){
        Button btn = new Button(context);

        btn.setText(text);
        btn.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(smallWidth, smallHeight);
        params.setMargins(0,0,10,0);
        btn.setLayoutParams(params);

        btn.setBackgroundResource(style);
        btn.setTextColor(context.getResources().getColor(android.R.color.black));

        return btn;
    }

    public Button mediumBtn(String text, int style, int mediumWidth, int mediumHeight){
        Button btn = new Button(context);

        btn.setText(text);
        btn.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mediumWidth, mediumHeight);
        params.setMargins(0,0,10,0);
        btn.setLayoutParams(params);

        btn.setBackgroundResource(style);
        btn.setTextColor(context.getResources().getColor(android.R.color.black));

        return btn;
    }

    public Button largeBtn(String text){
        Button btn = new Button(context);

        btn.setText(text);
        btn.setAllCaps(false);

        int largeWidth = width/2;
        int largeHeight = height/10;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(largeWidth, largeHeight);
        params.setMargins(0,0,0,10);
        btn.setLayoutParams(params);

        btn.setBackgroundResource(android.R.drawable.btn_default);
        btn.setTextColor(context.getResources().getColor(android.R.color.black));

        return btn;
    }

    public CheckBox autoSave(String text){
        box = new CheckBox(context);

        box.setText(text);
        box.setTextColor(context.getResources().getColor(android.R.color.black));
        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            // checkbox status is changed from uncheck to checked.
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                int btnDrawable = android.R.drawable.checkbox_off_background;

                if (isChecked)
                {
                    btnDrawable = android.R.drawable.checkbox_on_background;
                }

                box.setButtonDrawable(btnDrawable);

            }
        });

        int boxHeight = height/9;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, boxHeight);
        box.setLayoutParams(params);

        return box;
    }

    public TextView status(int textWidth){
        TextView text = new TextView(context);

        text.setTextColor(context.getResources().getColor(android.R.color.black));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(textWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10,0,0,0);
        text.setLayoutParams(params);

        return text;
    }

    public EditText editText(){
        EditText editText = new EditText(context);

        editText.setHint("Number of samples to collect");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(params);

        return editText;
    }
}
