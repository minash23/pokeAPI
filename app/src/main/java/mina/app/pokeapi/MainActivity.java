package mina.app.pokeapi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView numberTV;
    TextView name;
    TextView HeightTV;
    TextView BaseTV;
    TextView WeightTV;
    ImageView pokePic;
    Button searchButton;
    EditText user_input;
    ListView pokeList;
    TextView sigText;

    String input = "";
    Pokemon currPokemon;
    ArrayAdapter<String> adapter;
    ArrayList<String> pokemons;

    Cursor mCursor;

    Button clearButton;


    View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            input = user_input.getText().toString().toLowerCase().trim();
            if(isAlpha(input) && input.length() > 0){
                Log.d("input", input);
                user_input.setText("");
                makeRequest(input);
            }else{
                user_input.setText("");
                Toast.makeText(getApplicationContext(), "This is not a valid Pokemon", Toast.LENGTH_LONG).show();
            }
        }
    };
    public boolean isAlpha(String name) {
        char[] chars = name.toCharArray();
        for (char c : chars) {
            if(!Character.isLetter(c) || c != '.') {
                return false;
            }
        }
        return true;
    }

    JSONObjectRequestListener requestListener = new JSONObjectRequestListener() {

        @Override
        public void onResponse(JSONObject response) {
            try {
                String frontDefaultImageUrl = response.getJSONObject("sprites")
                        //.getJSONObject("front_default")
                        .getString("front_default");

                currPokemon = new Pokemon(response.getString("name"), response.getInt("id"), response.getInt("weight"), response.getInt("height"),response.getInt("base_experience"),  frontDefaultImageUrl);
                Log.d("Name", currPokemon.getName());
                Log.d("Number", "" + currPokemon.getNumber());
                Log.d("Height", "" + currPokemon.getHeight());
                Log.d("Weight", "" + currPokemon.getWeight());
                Log.d("base_exp", "" + currPokemon.getBase_exp());
                Log.d("ImageURL" , frontDefaultImageUrl);

                name.setText(currPokemon.getName().toUpperCase());
                numberTV.setText("" + currPokemon.getNumber());
                WeightTV.setText("" + currPokemon.getWeight());
                HeightTV.setText("" + currPokemon.getHeight());
                BaseTV.setText("" + currPokemon.getBase_exp());
                Picasso.get().load(frontDefaultImageUrl).into(pokePic);
                String oneAbility  = response.getJSONArray("abilities").getJSONObject(0).getJSONObject("ability").get("name").toString();
                String move = response.getJSONArray("moves").getJSONObject(0).getJSONObject("move").get("name").toString();
                sigText.setText("Signature Move/Ability: " + move.toUpperCase() + " and " + oneAbility.toUpperCase());
                //Log.d("movesArray", move);

                ContentValues contentValues = new ContentValues();
                contentValues.put(PokemonDBProvider.COLUMN_ONE, name.getText().toString().trim());
                contentValues.put(PokemonDBProvider.COLUMN_TWO, numberTV.getText().toString().trim());
                contentValues.put(PokemonDBProvider.COLUMN_THREE, HeightTV.getText().toString().trim());
                contentValues.put(PokemonDBProvider.COLUMN_FOUR, WeightTV.getText().toString().trim());
                contentValues.put(PokemonDBProvider.COLUMN_FIVE, BaseTV.getText().toString().trim());

                String[] projection = {
                        PokemonDBProvider.COLUMN_ONE,
                        PokemonDBProvider.COLUMN_TWO,
                        PokemonDBProvider.COLUMN_THREE,
                        PokemonDBProvider.COLUMN_FOUR,
                        PokemonDBProvider.COLUMN_FIVE,
                };

                String selection = PokemonDBProvider.COLUMN_ONE + "= ? AND " +
                        PokemonDBProvider.COLUMN_TWO + "= ? AND " +
                        PokemonDBProvider.COLUMN_THREE + "= ? AND " +
                        PokemonDBProvider.COLUMN_FOUR + "= ? AND " +
                        PokemonDBProvider.COLUMN_FIVE + "= ?";

                String[] selectionArgs = {
                        name.getText().toString().trim(),
                        numberTV.getText().toString().trim(),
                        HeightTV.getText().toString().trim(),
                        WeightTV.getText().toString().trim(),
                        BaseTV.getText().toString().trim()
                };



                //getContentResolver().insert(PokemonDBProvider.CONTENT_URI, contentValues);
                Cursor cursor = getContentResolver().query(PokemonDBProvider.CONTENT_URI, projection, selection, selectionArgs, null);

                if (cursor.getCount() < 1){
                    getContentResolver().insert(PokemonDBProvider.CONTENT_URI, contentValues);
                    String message = "We Have added your Pokemon to the Watchlist!";
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    query();
                } else{
                    //String message = "Pokemon is Already on Watchlist!";
                    //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }



            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void onError(ANError anError) {
            Log.d("S", currPokemon.getName());
            Toast.makeText(getApplicationContext(), "Error Getting Data, Not a Valid Pokemon", Toast.LENGTH_LONG).show();
        }
    };
    private void makeRequest(String poke){
        Log.d("url", "https://pokeapi.co/api/v2/pokemon/{poke}");
        ANRequest req =
                AndroidNetworking.get("https://pokeapi.co/api/v2/pokemon/" + poke)
                                .addPathParameter("name", poke)
                                .setPriority(Priority.LOW)
                                .build();
        req.getAsJSONObject(requestListener);
    }

    public void query(){
        mCursor = getContentResolver().query(PokemonDBProvider.CONTENT_URI, null, null, null, null);
        pokeList= findViewById(R.id.poke_listview);

        String attributes = "";
        pokemons = new ArrayList<>();
        if (!mCursor.moveToNext()) {
            mCursor.moveToFirst();
        }

        while (!mCursor.isLast()) {
            String Name = mCursor.getString(1);
            String Number = mCursor.getString(2);

            attributes = Number + ": " + Name;
            pokemons.add(attributes);

            mCursor.moveToNext();
            attributes = "";
        }
        String Name = mCursor.getString(1);
        String Number = mCursor.getString(2);

        attributes = Number + ": " + Name;
        pokemons.add(attributes);


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pokemons);
        pokeList.setAdapter(adapter);

    }

    AdapterView.OnItemClickListener listviewListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String item = (String) pokeList.getItemAtPosition(position);
            String[] parts = item.split(":");
            if (parts.length > 1) {
                // Extract the Pokemon name and trim any leading or trailing whitespace
                String pokemonName = parts[1].trim();

                // Now, pokemonName should contain "DITTO"
                makeRequest(pokemonName.toLowerCase());
                Log.d("name", pokemonName);
            }
        }
    };

    View.OnClickListener clearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            numberTV.setText("");
            HeightTV.setText("");
            WeightTV.setText("");
            BaseTV.setText("");
            sigText.setText("Signature Moves/Ability");
            name.setText("Pokemon");
            pokePic.setImageURI(Uri.EMPTY);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidNetworking.initialize(getApplicationContext());


        numberTV = findViewById(R.id.number_tv);
        name = findViewById(R.id.pokemon_name);
        HeightTV = findViewById(R.id.height_tv);
        BaseTV = findViewById(R.id.basexp_tv);
        WeightTV = findViewById(R.id.weight_tv);
        pokePic = findViewById(R.id.imageView);
        searchButton = findViewById(R.id.search_button);
        user_input = findViewById(R.id.user_input);
        user_input.setHint("Name");
        pokeList = findViewById(R.id.poke_listview);
        sigText = findViewById(R.id.signature_text);
        clearButton = findViewById(R.id.clear_button);

        searchButton.setOnClickListener(searchListener);
        clearButton.setOnClickListener(clearListener);

        makeRequest("pikachu");
        pokeList.setAdapter(adapter);
        pokeList.setOnItemClickListener(listviewListener);
        query();



    }
}