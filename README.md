# ToolText Android

[![Release](https://jitpack.io/v/noecivitillo/ToolText.svg)](https://jitpack.io/#noecivitillo/ToolText)


__**A nice and simple toolbar for rich text edit in Android**__


![Alt text](https://raw.github.com/noecivitillo/ToolText/master/toolbar/src/main/res/drawable/device20180209200500.png)
![Alt text](https://raw.github.com/noecivitillo/ToolText/master/toolbar/src/main/res/drawable/device20180209200825.png)
![Alt text](https://raw.github.com/noecivitillo/ToolText/master/toolbar/src/main/res/drawable/device20180213111200.png)	


### Features

**Bold**

_Italic_

Underline

Background Color

Foreground Color

Font Size

Bullets

Numbers

### Setup

1- In your root **build.gradle**

        allprojects {

        	         repositories {
        			...
        			maven { url 'https://jitpack.io' }
        		          }
       }
        	 
2- Add the dependencies
             
      dependencies {
         	        compile 'com.github.noecivitillo:ToolText:v1.1.1'
       }

### Usage

Check the Demo App for usage, or download it

1- Init ToolText in your Application class:


            new MyToolText(getApplicationContext());


2- In **styles.xml** (you need to set a theme with ToolbarBaseTheme parent)

            <style name="ThemeLight" parent="@style/ToolbarBaseTheme"/>

3- In your layout set CustomEditText and include toolbar

              <com.tool.CustomEditText
               android:id="@+id/customEditText"
               android:layout_width="match_parent"
               android:layout_height="wrap_content"/>
               

              <include layout="@layout/toolbar_edittext"/>

4- In your activity:

  a) Find toolbar layout and CustomEditText

             LinearLayout toolbarLayout = findViewById(R.id.toolbar_layout);
             CustomEditText customEditText = findViewById(R.id.customEditText);

  b) Set all styles:

            customEditText.setAllStyles(toolbarLayout);

  c) Or set styles independently
     (don't forget to find the view):

            customEditText.setBoldButton(boldBtn);

To **get Html**: 
                
            customEditText.getTextHtml;
            
To **set Html**: 
   
            customEditText.setTextHtml;


See source code for more features


### Libraries consulted 
 [Android-RTEditor](https://github.com/1gravity/Android-RTEditor)
 
 [RichEditor Android](https://github.com/1gravity/Android-RTEditor)
 
 [Cwac richedit](https://github.com/commonsguy/cwac-richedit)
 
 [CustomEditor](https://github.com/trietphm/CustomEditor)
 
                          
### License

MIT License

Copyright (c) 2018 Noelia Civitillo

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
