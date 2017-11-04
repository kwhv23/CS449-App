package kentonhanifl.CryptoGraph;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Collections;

import com.google.gson.Gson;

//ERROR CODES
//ERR1: Error loading currency. Clear app data and start again.

public class Main extends AppCompatActivity implements AsyncResponse, View.OnClickListener, SearchView.OnQueryTextListener{

    private boolean sortedByName = true;
    private boolean sortedByPrice = false;
    private boolean sortedByChange = false;


    public static String tag = "kk"; //For debug messages to logcat

    private boolean lock = false; //Lock on the refresh button

    public static ArrayList<Currency> Currencies = new ArrayList<Currency>();
    private ArrayList<Currency> BannerCurrencies = new ArrayList<Currency>();
    public static ArrayList<Currency> AdapterCurrencies = new ArrayList<Currency>();

    private CustomAdapter adapter;

    public static Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load the database
        database = new Database(getSharedPreferences("TradeViewData", 0));
        //Load the database (currently loading the USDT markets into the banner)
        database.loadDatabase(Currencies, BannerCurrencies, new BannerCondition<Currency>(){
            @Override
            boolean test(Currency currency) {
                return currency.MarketName.startsWith("USDT-");
            }
        });

        //Have the list be sorted by name initially
        Collections.sort(Currencies, new CurrencyNameCompare());

        //If we are connected, try to get the feed for the markets.
        if (Network.isConnected(this))
        {
            try
            {
                AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed(this)
                                                                    .execute(new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast toast = Toast.makeText(this, "No internet connection. Loading data if available.", Toast.LENGTH_SHORT);
            toast.show();
            //If we have data loaded:
            if (database.getDataSize() != 0)
            {
                //Updating the ListView
                setListAdapter();

                //Start the scrolling banner
                startBanner();
            }
        }

        //Set up all of the buttons and searchview

        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(this);

        Button sortName = (Button) findViewById(R.id.sortName);
        sortName.setOnClickListener(this);

        Button sortPrice = (Button) findViewById(R.id.sortPrice);
        sortPrice.setOnClickListener(this);

        Button sortChanges = (Button) findViewById(R.id.sortChanges);
        sortChanges.setOnClickListener(this);

        Button sortFavorites = (Button) findViewById(R.id.sortFavorites);
        sortFavorites.setOnClickListener(this);

        SearchView tableSearchBar = (SearchView) findViewById(R.id.tableSearchBar);
        tableSearchBar.setOnQueryTextListener(this);
    }

    /*
    --------------------------------------------------------------------------------
    Handling the AsyncTask data (from getFeed())
    Builds up the list of currencies from the returned JSON objects
    --------------------------------------------------------------------------------
    */
    @Override
    public void processFinish(StringBuffer jsonStringBuffer){
        try {
            //The HTTP response comes with a bunch of useless stuff before the objects we want. Clean it up.
            jsonStringBuffer.delete(0, jsonStringBuffer.indexOf("[") + 1); //Do note, if the optional message field ever comes with an open or close bracket [], this will break.
            jsonStringBuffer.delete(jsonStringBuffer.lastIndexOf("]"), jsonStringBuffer.lastIndexOf("}") + 1);

            Gson gsonout = new Gson();
            while (jsonStringBuffer.length() != 0) {
                Currency c = getNextJSONObject(gsonout, jsonStringBuffer);
                appendCurrency(c);
            }

        }catch(NullPointerException e){
            //If we did not get data, or did not get good data, load from the database.
            Toast toast = Toast.makeText(Main.this, "Did not receive data from Bittrex.", Toast.LENGTH_SHORT);
            toast.show();
            database.loadDatabase(Currencies, BannerCurrencies, new BannerCondition<Currency>(){
                @Override
                boolean test(Currency currency) {
                    return currency.MarketName.startsWith("USDT-");
                }
            });
        }finally {
            //Updating the ListView
            setListAdapter();

            //Start the scrolling banner
            startBanner();

            //Save array in shared preferences
            saveCurrencies();

            lock = false; //Reset the lock on the refresh button
        }
    }

    /*
    --------------------------------------------------------------------------------
    FUNCTIONS CALLED BY PROCESSFINISH()
    --------------------------------------------------------------------------------
    */

    private Currency getNextJSONObject(Gson parser, StringBuffer JSONObjects)
    {

        String json = JSONObjects.substring(0, JSONObjects.indexOf("}") + 1); //Get the next JSON object in the StringBuffer
        Currency c = parser.fromJson(json, Currency.class); //Gson parses the object and puts all of the data into a Currency
        JSONObjects.delete(0, JSONObjects.indexOf("}") + 1); //Delete the JSON object we just parsed from the StringBuffer to get the next one
        if (JSONObjects.length() != 0) {
            JSONObjects.delete(0, 1); //Delete the comma between each object
        }
        return c;
    }

    private void appendCurrency(Currency c)
    {
        if (Currencies.indexOf(c) == -1) //If this isn't in our list of currencies (I.E. a new currency was added)
        {
            if (!c.MarketName.startsWith("ETH-")) { //Take out the ETH markets
                Currencies.add(c); //Add the newly parsed JSON object turned into a currency into the list
            }

            if (c.MarketName.startsWith("USDT-")) //Add to separate list for the banner too if it's a USDT market
            {
                BannerCurrencies.add(c);
            }
        }
        else {
            if (!c.MarketName.startsWith("ETH-")) { //Take out the ETH markets
                Currencies.get(Currencies.indexOf(c)).Last = c.Last;
                Currencies.get(Currencies.indexOf(c)).PrevDay = c.PrevDay;
            }
            if (c.MarketName.startsWith("USDT-")) {
                BannerCurrencies.get(BannerCurrencies.indexOf(c)).Last = c.Last;
            }
        }
    }


    /*
    --------------------------------------------------------------------------------
    BUTTONS
    The OnClick method and then every action after.
    --------------------------------------------------------------------------------
    */
    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.refresh:
                refreshButtonAction();
                break;

            case R.id.sortName:
                if (!isSortButtonLock())
                {
                    sortNameButtonAction();
                }
                break;

            case R.id.sortPrice:
                if (!isSortButtonLock())
                {
                    sortPriceButtonAction();
                }
                break;

            case R.id.sortChanges:
                if (!isSortButtonLock())
                {
                    sortChangesButtonAction();
                }
                break;

            case R.id.sortFavorites:
                if (!isSortButtonLock())
                {
                    sortFavoritesButtonAction();
                }
                break;

        }
    }


    private void refreshButtonAction() {
        SearchView tableSearchBar = (SearchView) findViewById(R.id.tableSearchBar);

        if(!lock && tableSearchBar.isIconified()) //There were bugs letting the user refresh while the SearchView was pressed, so I just disable it here.
        {
            boolean isConnected = Network.isConnected(Main.this);

            lock = true;
            if (isConnected)
            {
                try
                {
                    AsyncTask<URL, Integer, StringBuffer> Markets = new GetFeed(Main.this)
                            .execute(new URL("https://bittrex.com/api/v1.1/public/getmarketsummaries"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast toast = Toast.makeText(Main.this, "No connection. Cannot refresh.", Toast.LENGTH_SHORT);
                toast.show();
                lock = false;
            }
        }
    }

    private void sortNameButtonAction() {
        if (sortedByName) {
            Collections.sort(AdapterCurrencies, new BackwardsCurrencyNameCompare());
        }
        else
        {
            Collections.sort(AdapterCurrencies, new CurrencyNameCompare());
        }

        adapter.notifyDataSetChanged();

        sortedByName = !sortedByName;
        sortedByPrice = false;
        sortedByChange = false;
    }

    private void sortPriceButtonAction() {
        if (sortedByPrice) {
            Collections.sort(AdapterCurrencies, new BackwardsCurrencyPriceCompare());
        }
        else
        {
            Collections.sort(AdapterCurrencies, new CurrencyPriceCompare());
        }

        adapter.notifyDataSetChanged();

        sortedByPrice = !sortedByPrice;
        sortedByName = false;
        sortedByChange = false;
    }

    private void sortChangesButtonAction() {
        if (sortedByChange) {
            Collections.sort(AdapterCurrencies, new BackwardsCurrencyChangeCompare());
        }
        else
        {
            Collections.sort(AdapterCurrencies, new CurrencyChangeCompare());
        }

        adapter.notifyDataSetChanged();

        sortedByChange = !sortedByChange;
        sortedByName = false;
        sortedByPrice = false;
    }

    private void sortFavoritesButtonAction() {
        Collections.sort(AdapterCurrencies, new CurrencyFavoriteCompare());
        adapter.notifyDataSetChanged();

        sortedByName = false;
        sortedByPrice = false;
        sortedByChange = false;
    }

    private boolean isSortButtonLock()
    {
        if (AdapterCurrencies.size()!=0)
        {
            return false;
        }
        return true;
    }

    /*
    --------------------------------------------------------------------------------
    SEARCH BAR
    The onQuery action
    --------------------------------------------------------------------------------
    */

    @Override
    public boolean onQueryTextSubmit(String s)
    {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s)
    {
        adapter.getFilter().filter(s);
        return true;
    }

    /*
    --------------------------------------------------------------------------------
    GRAPHICS FUNCTIONS
    The adapter for the ListView and the Banner
    --------------------------------------------------------------------------------
    */

    //Sets the adapter for the list so that data is actually displayed.
    //Sets the SearchView onclick listener
    //Sets the sorting buttons
    public void setListAdapter()
    {
        //Start the listAdapter
        sortedByName = true;
        sortedByPrice = false;
        sortedByChange = false;
        AdapterCurrencies.clear();
        AdapterCurrencies.addAll(Currencies);
        ListView list = (ListView) findViewById(R.id.list);
        adapter = new CustomAdapter(AdapterCurrencies, this);
        list.setAdapter(adapter);
        list.deferNotifyDataSetChanged();
    }

    //Starts the horizontal scrolling for the banner
    public void startBanner()
    {
        Collections.sort(BannerCurrencies, new CurrencyNameCompare());
        TextView banner = (TextView) findViewById(R.id.Banner);
        StringBuilder bannerStream = new StringBuilder();
        for(Currency c : BannerCurrencies)
        {
            bannerStream.append("| " + c.getName() + ":  " + String.format("%.2f", c.Last)+ " |      ");
        }
        banner.setText(bannerStream.toString());

        banner.setHorizontallyScrolling(true);
    }



    //Should be called whenever data is changed in any way. Currently just saves EVERYTHING to shared preferences.
    //Only used by CustomAdapter, but CustomAdapter should not have access to the main list of currencies.
    //This will be changed when the favoriting logic is moved out of the CustomAdapter
    public static void saveCurrencies()
    {
        database.save(Currencies);
    }

}


