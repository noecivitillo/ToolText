package demo.tooltextdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.tool.CustomEditText;





public class DemoActivity extends AppCompatActivity {

    private TextView textView;
    private CustomEditText customEditText;
    private boolean show= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //Set theme
        setTheme(R.style.ThemeLight);

        setContentView(R.layout.activity_demo);

        //Find toolbar layout
        LinearLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        //Find CustomEdittext
        customEditText = findViewById(R.id.customEditText);
        //Set all effects
        customEditText.setAllStyles(toolbarLayout);

        textView = findViewById(R.id.textViewHtml);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.showHtml){
            if(item.isChecked()) {
                item.setChecked(false);
                show=false;
            }else{
                item.setChecked(true);
                show=true;

            }
            showHtml();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showHtml(){
        if(show){
            String textHtml = customEditText.getTextHTML();
            textView.setText(textHtml);
            textView.setVisibility(View.VISIBLE);
            textView.setMovementMethod(new ScrollingMovementMethod());
        }else{
            textView.setVisibility(View.GONE);
        }

    }
}
