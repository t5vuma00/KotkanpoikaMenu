package kotkanpoika.menu;

import android.app.DatePickerDialog;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG_LUNCH_MENU = "LunchMenu";
    private static final String TAG_MENUS = "SetMenus";
    private static final String TAG_MEALS = "Meals";
    private static final String TAG_MEAL_NAME = "Name";
    private static final String TAG_LUNCH_TYPE = "Name";

    private static final String TAG_MAIN_DISH = "MainDish";
    private static final String TAG_SIDE_DISH = "SideDish";
    private static final String TAG_LUNCH = "Lunch";

    private String date = "yyyy-mm-dd";

    ListView listViewMeals;
    TextView textViewError;

    Button buttonFinnish;
    Button buttonEnglish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewMeals = findViewById(R.id.listViewMeals);
        textViewError = findViewById(R.id.textViewError);
        buttonEnglish = findViewById(R.id.buttonEnglish);
        buttonFinnish = findViewById(R.id.buttonFinnish);

        date = getTodaysDate();

        String url = createUrl(date, "fi");

        new ParseTask().execute(url);
    }

    public void getFinnishMenu(View view){
        buttonFinnish.getBackground().setColorFilter(null);
        buttonEnglish.getBackground().setColorFilter(0xe9999999, PorterDuff.Mode.DARKEN);

        String url = createUrl(date, "fi");

        new ParseTask().execute(url);
    }

    public void getEnglishMenu(View view){
        buttonEnglish.getBackground().setColorFilter(null);
        buttonFinnish.getBackground().setColorFilter(0xe9999999, PorterDuff.Mode.DARKEN);

        String url = createUrl(date, "en");

        new ParseTask().execute(url);
    }

    public void getDate(View view){

        Calendar myCalendar = Calendar.getInstance(TimeZone.getDefault());

        new DatePickerDialog(MainActivity.this, this, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        date = Integer.toString(year) + "-" + Integer.toString(month+1) + "-" + Integer.toString(day);
        Log.d("Main", date);
    }

    private String createUrl(String urlDate, String urlLanguage){
        return "http://amica.fi/api/restaurant/menu/day?date=" +  urlDate +
                     "&language=" + urlLanguage + "&restaurantPageId=66287";
    }

    public String getTodaysDate(){
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private class ParseTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {
        @Override
        protected void onPreExecute() {
            ProgressBar bar=findViewById(R.id.progressbar);
            bar.setVisibility(View.VISIBLE);
            buttonEnglish.setClickable(false);
            buttonFinnish.setClickable(false);
            textViewError.setText("");
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... url) {
            ArrayList<HashMap<String, String>> mealList = new ArrayList<HashMap<String, String>>();

            if(url[0].isEmpty()){
                return null;
            }
            else{
                try {
                    // Creating JSON Parser instance
                    JSONParser jParser = new JSONParser();
                    // getting JSON string from URL
                    JSONObject jsonObject = jParser.getjObj(url[0]);

                    JSONObject lunchMenu = jsonObject.getJSONObject(TAG_LUNCH_MENU);

                    JSONArray menus = lunchMenu.getJSONArray(TAG_MENUS);

                    String mainDish = "";
                    String sideDish = "";
                    String lunchType = "";

                    for (int i = 0; i < menus.length(); i++) {
                        JSONObject jsonMealNumber = menus.getJSONObject(i);

                        lunchType = jsonMealNumber.getString(TAG_LUNCH_TYPE);

                        JSONArray jsonMeals = jsonMealNumber.getJSONArray(TAG_MEALS);

                        for (int j = 0; j < jsonMeals.length(); j++) {
                            JSONObject jsonMealType = jsonMeals.getJSONObject(j);
                            if(j == 0){
                                mainDish = jsonMealType.getString(TAG_MEAL_NAME);
                            }
                            else{
                                sideDish = jsonMealType.getString(TAG_MEAL_NAME);
                            }
                        }

                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_LUNCH, lunchType);
                        map.put(TAG_MAIN_DISH, mainDish);
                        if(!sideDish.isEmpty())
                            map.put(TAG_SIDE_DISH, sideDish);
                        //else
                            //map.put(TAG_SIDE_DISH, "NoSideDish");

                        if(!mainDish.isEmpty()){
                            Log.d("MAP", map.toString());
                            mealList.add(map);
                        }

                        mainDish = "";
                        sideDish = "";
                        lunchType = "";
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                return mealList;
            }
        }

        protected void onPostExecute(ArrayList<HashMap<String, String>> mealList) {
            ProgressBar bar=findViewById(R.id.progressbar);
            bar.setVisibility(View.INVISIBLE);

            printMeals(mealList);

            buttonEnglish.setClickable(true);
            buttonFinnish.setClickable(true);
        }
    }

    public void printMeals(final ArrayList<HashMap<String, String>> meals){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int arraySize = meals.size();
                Log.d("PrintMeals ", Integer.toString(arraySize));

                if(meals.size() != 0){
                    ListAdapter listAdapter = new SimpleAdapter(MainActivity.this, meals, R.layout.list_item,
                            new String[] {TAG_LUNCH, TAG_MAIN_DISH, TAG_SIDE_DISH},
                            new int[]{R.id.textViewLunchType, R.id.textViewMainDish,
                                    R.id.textViewSideDish});

                    listViewMeals.setAdapter(listAdapter);
                }
                else{
                    textViewError.setText("Error getting data");
                }
            }
        });
    }
}
