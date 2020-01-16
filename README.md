# CoreSearchableSpinner
CoreSearchableSpinner

Usage:
```xml
  <com.catacore.coresearchablespinner.CoreSearchableSpinner
      android:id="@+id/my_spinner"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"/>
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
```
name="displayEmptyText" format="boolean"
(default is true)

Feel free to extend the code!
