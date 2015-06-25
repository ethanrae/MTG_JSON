package mtg_json;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MTG_JSON {

    //Lists for storing querys
    public static List<String> set_list_query = new ArrayList<>();
    public static List<String> type_list_query = new ArrayList<>();
    public static List<String> subtype_list_query = new ArrayList<>();
    public static List<String> cost_list_query = new ArrayList<>();
    public static List<String> artist_list_query = new ArrayList<>();
    public static List<String> rarity_list_query = new ArrayList<>();
    public static List<String> card_data_list_query = new ArrayList<>();
    public static List<String> card_list_query = new ArrayList<>();

    //Hashtables for keeping track of a rows index
    public static Hashtable<String, Integer> set_table = new Hashtable<>();
    public static Hashtable<String, Integer> type_table = new Hashtable<>();
    public static Hashtable<String, Integer> subtype_table = new Hashtable<>();
    public static Hashtable<String, Integer> cost_table = new Hashtable<>();
    public static Hashtable<String, Integer> artist_table = new Hashtable<>();
    public static Hashtable<String, Integer> rarity_table = new Hashtable<>();
    public static Hashtable<String, Integer> card_table = new Hashtable<>();
    public static Hashtable<String, Integer> card_data_table = new Hashtable<>();

    //JSON file names
    public static String all_sets_file_name = "AllSets.json";
    public static String set_codes_file_name = "SetList.json";

    //Debug variables for finding ideal sizes of VARCHAR and INT 
    //for the MySQL tables
    public static int type_len = 0;
    public static int subtype_len = 0;
    public static int cost_len = 0;
    public static int artist_len = 0;
    public static int set_name_len = 0;//
    public static int set_code_len = 0;//
    public static int rarity_len = 0;
    public static int card_name_len = 0;
    public static int power_len = 0;
    public static int toughness_len = 0;
    public static long cmc_len = 0;
    public static int card_text_len = 0;
    public static int image_name_len = 0;
    public static int flavor_len = 0;

    public static final boolean PRINT_MAX = false;

    public static void run() {

        //initialize tables SUBTYPE, TYPE, and COST 
        //they use empty strings instead of null
        String query;
        query = "INSERT INTO SUBTYPE VALUES (" + subtype_table.size() + ",'');";
        subtype_table.put("", subtype_table.size());
        subtype_list_query.add(query);

        query = "INSERT INTO TYPE VALUES (" + type_table.size() + ",'');";
        type_table.put("", type_table.size());
        type_list_query.add(query);

        query = "INSERT INTO COST VALUES (" + cost_table.size() + ",'');";
        cost_table.put("", cost_table.size());
        cost_list_query.add(query);

        getSetCodes(set_codes_file_name);
    }

    //Loops through each set name
    //Creates a C_SET insert query
    //Calls getSetData for each set
    public static void getSetCodes(String file) {
        JSONParser code_parser = new JSONParser();
        try {

            JSONObject all_set_obj = (JSONObject) code_parser.parse(new FileReader(all_sets_file_name));
            JSONArray jsonArray = (JSONArray) code_parser.parse(new FileReader(file));

            Iterator<Object> iterator = jsonArray.iterator();

            while (iterator.hasNext()) {

                JSONObject set_data = (JSONObject) iterator.next();

                String set_code = (String) set_data.get("code");
                set_code = recode(set_code);
                set_code_len = Math.max(set_code_len, set_code.length());

                String set_name = (String) set_data.get("name");
                set_name = recode(set_name);
                set_name_len = Math.max(set_name_len, set_name.length());

                String set_rel_date = (String) set_data.get("releaseDate");

                String set_query = "INSERT INTO C_SET (set_id,set_name,set_code,set_rel_date) VALUES (" + set_table.size() + ",'" + set_name + "','" + set_code + "','" + set_rel_date + "');";
                set_table.put(set_code, set_table.size());
                set_list_query.add(set_query);

                getSetData(all_set_obj, set_code);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //retrieves the card data for a given set
    //loops through each card in the set
    //creates all insert statements for all new data incountered
    public static void getSetData(JSONObject obj, String set_code) {

        JSONParser parser = new JSONParser();

        try {

            JSONObject jsonObject = (JSONObject) obj.get(set_code);
            JSONArray jsonArray = (JSONArray) jsonObject.get("cards");

            //System.out.println(set_code + "\n");
            Iterator<Object> iterator = jsonArray.iterator();

            while (iterator.hasNext()) {
                JSONObject card = (JSONObject) iterator.next();

                //get card name
                String card_name = (String) card.get("name");
                card_name = recode(card_name);
                if (card_name != null && !card_name.isEmpty() && !card_table.containsKey(card_name)) {
                    card_name_len = Math.max(card_name_len, card_name.length());
                    card_table.put(card_name, card_table.size());
                }

                //get mana cost
                String mana_cost = (String) card.get("manaCost");
                //check if we have seen before, if not create insert statement
                if (mana_cost != null && !mana_cost.isEmpty() && !cost_table.containsKey(mana_cost)) {
                    cost_len = Math.max(cost_len, mana_cost.length());
                    String query = "INSERT INTO COST VALUES (" + cost_table.size() + ",'" + mana_cost + "');";
                    //System.out.println(query);
                    cost_list_query.add(query);
                    cost_table.put(mana_cost, cost_table.size());
                }

                //get cmc
                int cmc;
                try {
                    cmc = ((Long) card.get("cmc")).intValue();
                } catch (Exception e) {
                    cmc = 0;
                }
                //keep track of max
                cmc_len = Math.max(cmc_len, cmc);

                //get rarity
                String rarity = (String) card.get("rarity");
                //check if we have seen before, if not create insert statement
                if (rarity != null && !rarity.isEmpty() && !rarity_table.containsKey(rarity)) {
                    //keep track of max
                    rarity_len = Math.max(rarity_len, rarity.length());
                    String query = "INSERT INTO RARITY VALUES (" + rarity_table.size() + ",'" + rarity + "');";
                    //System.out.println(query);
                    rarity_list_query.add(query);
                    rarity_table.put(rarity, rarity_table.size());
                }

                //get artist
                String artist = (String) card.get("artist");
                artist = recode(artist);
                //check if we have seen before, if not create insert statement
                if (artist != null && !artist.isEmpty() && !artist.isEmpty() && !artist_table.containsKey(artist)) {
                    //keep track of max
                    artist_len = Math.max(artist_len, artist.length());
                    String query = "INSERT INTO ARTIST VALUES (" + artist_table.size() + ",'" + artist + "');";
                    //System.out.println(query);
                    artist_list_query.add(query);
                    artist_table.put(artist, artist_table.size());
                }

                //get power and toughness
                int power = 0;
                int toughness = 0;
                try {
                    power = Integer.decode((String) card.get("power"));
                } catch (Exception e) {
                    power = 0;
                    //System.out.println(e.getMessage());
                    //System.exit(0);
                }
                //keep track of max
                power_len = Math.max(power_len, power);

                try {
                    toughness = Integer.decode((String) card.get("toughness"));
                } catch (Exception e) {
                    toughness = 0;
                    //System.out.println(e.getLocalizedMessage());
                    //System.exit(0);
                }
                //keep track of max
                toughness_len = Math.max(toughness_len, toughness);

                //get type in fulltype format
                String fulltype = (String) card.get("type");
                String type = null;
                String subtype = null;
                //System.out.println(fulltype);

                //parse fulltype into type and subtype
                if (fulltype != null) {
                    try {
                        if (fulltype.contains("—")) {
                            type = fulltype.substring(0, fulltype.indexOf('—') - 1);
                        } else {
                            type = fulltype;
                        }

                    } catch (Exception e) {
                        type = "";
                    }

                    try {
                        if (fulltype.contains("—")) {
                            subtype = fulltype.substring(fulltype.indexOf('—') + 2);
                        } else {
                            subtype = "";
                        }
                    } catch (Exception e) {
                        subtype = "";
                    }

                    type = recode(type);
                    //check if we have seen before, if not create insert statement
                    if (type != null && !type.isEmpty() && !type_table.containsKey(type)) {
                        //keep track of max
                        type_len = Math.max(type_len, type.length());
                        String query = "INSERT INTO TYPE VALUES (" + type_table.size() + ",'" + type + "');";
                        //System.out.println(query);
                        type_list_query.add(query);
                        type_table.put(type, type_table.size());
                    }

                    subtype = recode(subtype);
                    //check if we have seen before, if not create insert statement
                    if (subtype != null && !subtype.isEmpty() && !subtype_table.containsKey(subtype)) {
                        //keep track of max
                        subtype_len = Math.max(subtype_len, subtype.length());
                        String query = "INSERT INTO SUBTYPE VALUES (" + subtype_table.size() + ",'" + subtype + "');";
                        //System.out.println(query);
                        subtype_list_query.add(query);
                        subtype_table.put(subtype, subtype_table.size());
                    }
                }

                //get text
                String card_text = (String) card.get("text");
                card_text = recode(card_text);
                //keep track of max
                card_text_len = Math.max(card_text_len, card_text.length());

                //get flavor
                String flavor = (String) card.get("flavor");
                flavor = recode(flavor);
                //keep track of max
                flavor_len = Math.max(flavor_len, flavor.length());

                //get image name
                String imageName = (String) card.get("imageName");
                imageName = recode(imageName);
                //keep track of max
                image_name_len = Math.max(image_name_len, imageName.length());

                //check if card is new or a reprinted card
                //if reprinted find the correct CARD_DATA index for CARD insert
                //else create new CARD_DATA and CARD inserts
                if (card_name != null && !card_name.isEmpty() && !card_data_table.containsKey(card_name)) {
                    String card_query;
                    String card_data_query;
                    if (subtype != null && !subtype.isEmpty()) {
                        if (mana_cost != null && !mana_cost.isEmpty()) {
                            card_data_query = "INSERT INTO CARD_DATA VALUES(" + card_data_table.size() + ", " + type_table.get(type) + ", " + subtype_table.get(subtype) + ", " + cost_table.get(mana_cost) + ", '" + card_name + "', " + power + ", " + toughness + ", " + cmc + ", '" + card_text + "' );";

                        } else {
                            card_data_query = "INSERT INTO CARD_DATA VALUES(" + card_data_table.size() + ", " + type_table.get(type) + ", " + subtype_table.get(subtype) + ", 0, '" + card_name + "', " + power + ", " + toughness + ", " + cmc + ", '" + card_text + "' );";

                        }

                    } else {
                        if (mana_cost != null && !mana_cost.isEmpty()) {
                            card_data_query = "INSERT INTO CARD_DATA VALUES(" + card_data_table.size() + ", " + type_table.get(type) + ", 0, " + cost_table.get(mana_cost) + ", '" + card_name + "', " + power + ", " + toughness + ", " + cmc + ", '" + card_text + "' );";

                        } else {
                            card_data_query = "INSERT INTO CARD_DATA VALUES(" + card_data_table.size() + ", " + type_table.get(type) + ", 0, 0, '" + card_name + "', " + power + ", " + toughness + ", " + cmc + ", '" + card_text + "' );";

                        }

                    }

                    if (flavor.isEmpty()) {
                        card_query = "INSERT INTO CARD VALUES (" + card_list_query.size() + ", " + card_data_table.size() + ", " + artist_table.get(artist) + ", " + set_table.get(set_code) + ", " + rarity_table.get(rarity) + ", '', '" + imageName + "');";

                    } else {
                        card_query = "INSERT INTO CARD VALUES (" + card_list_query.size() + ", " + card_data_table.size() + ", " + artist_table.get(artist) + ", " + set_table.get(set_code) + ", " + rarity_table.get(rarity) + ", '" + flavor + "', '" + imageName + "');";

                    }
                    card_data_table.put(card_name, card_data_table.size());
                    card_data_list_query.add(card_data_query);
                    card_list_query.add(card_query);
                } else {
                    String card_query;
                    if (flavor.isEmpty()) {
                        card_query = "INSERT INTO CARD VALUES (" + card_list_query.size() + ", " + card_data_table.get(card_name) + ", " + artist_table.get(artist) + ", " + set_table.get(set_code) + ", " + rarity_table.get(rarity) + ", '', '" + imageName + "');";

                    } else {
                        card_query = "INSERT INTO CARD VALUES (" + card_list_query.size() + ", " + card_data_table.get(card_name) + ", " + artist_table.get(artist) + ", " + set_table.get(set_code) + ", " + rarity_table.get(rarity) + ", '" + flavor + "', '" + imageName + "');";

                    }
                    card_list_query.add(card_query);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Prints the insert statements in the correct order
    //TODO use PrintWriter writer = new PrintWriter("mtg_inserts.sql", "UTF-8");
    public static void print() {
        System.out.println("\n\n\t/* ----------------------------------SETS---------------------------------- */\n");
        for (int i = 0; i < set_list_query.size(); i++) {
            System.out.println(set_list_query.get(i));
        }

        System.out.println("\n\n\t/* ----------------------------------TYPES---------------------------------- */\n");
        for (int i = 0; i < type_list_query.size(); i++) {
            System.out.println(type_list_query.get(i));
        }

        System.out.println("\n\n\t/* ----------------------------------SUBTYPES---------------------------------- */\n");
        for (int i = 0; i < subtype_list_query.size(); i++) {
            System.out.println(subtype_list_query.get(i));
        }

        System.out.println("\n\n\t/* ----------------------------------COST---------------------------------- */\n");
        for (int i = 0; i < cost_list_query.size(); i++) {
            System.out.println(cost_list_query.get(i));
        }

        System.out.println("\n\n\t/* ----------------------------------ARTIST---------------------------------- */\n");
        for (int i = 0; i < artist_list_query.size(); i++) {
            System.out.println(artist_list_query.get(i));
        }

        System.out.println("\n\n\t/* ----------------------------------RARITY---------------------------------- */\n");
        for (int i = 0; i < rarity_list_query.size(); i++) {
            System.out.println(rarity_list_query.get(i));
        }

        System.out.println("\n\n\t/* ----------------------------------CARD_DATA---------------------------------- */\n");
        for (int i = 0; i < card_data_list_query.size(); i++) {
            System.out.println(card_data_list_query.get(i));
        }

        System.out.println("\n\n\t/* ----------------------------------CARD---------------------------------- */\n");
        for (int i = 0; i < card_list_query.size(); i++) {
            System.out.println(card_list_query.get(i));
        }
    }

    public static String recode(String input) {
        if (input == null) {
            return "";
        } else {
            input = input.replace("'null'", "''");
            input = input.replace("'", "\\'");
            return input;
        }
    }

    public static void main(String[] args) {
        run();
        if (PRINT_MAX) {
            System.out.println("type_len = " + type_len);
            System.out.println("subtype_len = " + subtype_len);
            System.out.println("cost_len = " + cost_len);
            System.out.println("artist_len = " + artist_len);
            System.out.println("set_name_len = " + set_name_len);
            System.out.println("set_code_len = " + set_code_len);
            System.out.println("rarity_len = " + rarity_len);
            System.out.println("card_name_len = " + card_name_len);
            System.out.println("power_len = " + power_len);
            System.out.println("toughness_len = " + toughness_len);
            System.out.println("cmc_len = " + cmc_len);
            System.out.println("card_text_len = " + card_text_len);
            System.out.println("image_name_len = " + image_name_len);
            System.out.println("flavor_len = " + flavor_len);
        } else {
            print();
        }
    }
}
