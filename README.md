# CoreSearchableSpinner
CoreSearchableSpinner

Usage:

XML file
```xml
  <com.catacore.coresearchablespinner.CoreSearchableSpinner
      android:id="@+id/my_spinner"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      app:displayEmptyText="false"
      app:dropdownRightIcon="@drawable/my_right_drpdown"/>
```
Android Java class implementation
```xml
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
          
        spinner.setItems(items);
          
        spinner.setSelectedSpinnerItem(items.get(2));
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
  
```


Implementation

on build.gradle (Project)
```xml
  allprojects {
    repositories {
         ...
         maven {
            url 'http://jitpack.io'
         }
         ...
    }
  }
```

on build.gradle (Application)
```xml
  dependencies {
    implementation 'com.github.catacor:CoreSearchableSpinner:1.0.0'
  }
```

Mandatory

1)Add to your activity tag in manifest the next tags for window resize

        android:windowSoftInputMode="adjustResize"
        android:fitsSystemWindows="true"
2)Overwrite onBackPressed() and onTouchEvent() in the main activity (if needed, transfer the event to your MVC View Implementation)

3)If you have multiple spinners in the same view, implement onSpinnerCliked to close the opened ones.

4)Don't forget to set the activity to your spinner. You will get the next message if you forget "Set the activity dependencies!". You need that for screen measurments.

5)You have onItemSelected(), getSelectedItemIndex() and getSelectedItem() to magange the spinner result;

6)Added setSelectedSpinnerItem(Integer position), setSelectedSpinnerItem(String displayText), setSelectedSpinnerItem(SearchableItem _item)

Possible existent xml tags:
```xml
name="displayBackground" format="integer"
name="dropdownRightIcon" format="integer"
name="searchRightIcon" format="integer"
```
(for the design format, open ic_right_drawable_search.xml and custom_edit_text_bg_empty.xml)
```xml
name="defaultDisplayText" format="string"
name="displayDefaultText" format="boolean"
```
(default is true)
```xml
name="defaultEmptyText" format="string"
name="displayEmptyText" format="boolean"
```
(default is true)

If you want to have a custom view, for a clean methon, modify those drawables:

For right icons:

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list  xmlns:android="http://schemas.android.com/apk/res/android">

    <item android:height="40dp" android:width="50dp">
        <shape
            android:shape="rectangle"
            xmlns:android="http://schemas.android.com/apk/res/android">
            <corners android:topRightRadius="3dp" android:bottomRightRadius="3dp"></corners>
            <stroke android:color="@color/gray_333" android:width="1dp"></stroke>
            <solid android:color="@color/white"></solid>
        </shape>
    </item>

    <item
        android:top="1.2dp"
        android:right="1.2dp"
        android:height="37.8dp" android:width="48.9dp">
        <shape
            android:shape="rectangle"
            xmlns:android="http://schemas.android.com/apk/res/android">
            <corners android:topRightRadius="3dp" android:bottomRightRadius="3dp"></corners>

            <solid android:color="@color/white"></solid>
        </shape>
    </item>
    <item

        android:height="26dp" android:width="26dp"
        android:gravity="center" android:drawable="@drawable/ic_dropdown_arrow">

    </item>
</layer-list>
```
For background
```xml
<?xml version="1.0" encoding="UTF-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:top="3dp">
        <shape android:shape="rectangle">
            <solid android:color="@android:color/transparent" />
            <corners
                android:bottomLeftRadius="1dp"
                android:bottomRightRadius="1dp"
                android:topLeftRadius="1dp"
                android:topRightRadius="1dp" />
        </shape>
    </item>
</layer-list>
```

Feel free to extend the code!
