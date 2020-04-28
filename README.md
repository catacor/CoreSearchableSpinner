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

        ArrayList<SearchableItem> items = new ArrayList<>();
        items.add(new SearchableItem("Steve", new ArrayList<>(Arrays.asList("Geeks", "for", "Geeks"))));
        items.add(new SearchableItem("Tim",new ArrayList<>(Arrays.asList("Gregor Clegane"))));
        items.add(new SearchableItem("Lucy",new ArrayList<>(Arrays.asList("Khal Drogo"))));
        items.add(new SearchableItem("Pat",new ArrayList<>(Arrays.asList("Cersei Lannister"))));
          
        spinner.setItems(items);
         
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

        android:windowSoftInputMode="adjustPan"


5)You have onSpinnerClicked(); onSpinnerShown(); onSpinnerDismissed(); onItemClicked(int position); getSelectedIndex(), getSelectedItem() and setCurrentItem(int position) to magange the spinner;


Possible existent xml tags:
```xml
name="displayBackground" format="reference|integer"
name="dropdownRightIcon" format="reference|integer"
name="searchRightIcon" format="reference|integer"
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


Feel free to extend the code!
