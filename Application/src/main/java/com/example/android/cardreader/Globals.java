package com.example.android.cardreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

/**
 * Created by dkukreja on 1/13/16.
 */
public class Globals {
    public static ArrayList<String> trustedIds = new ArrayList<String>() {{
        add("-16-122-8940");
        add("-101103-27-45");
    }};

    public static ArrayList<String> names = new ArrayList<String>() {{
        add("Dave andch");
        add("Puhven");
    }};

    public static ArrayList<User> allUsers = new ArrayList<User>();
    public static ArrayList<User> checkedIn = new ArrayList<User>();
    public static ArrayList<User> pending = new ArrayList<User>();

    public static void executeEden(){
        Random random = new Random();
        for (int i=0; i<50; i++){
            String f = first.get(random.nextInt(first.size()));
            String l = last.get(random.nextInt(last.size()));
            User u = new User(f+" "+l);
            allUsers.add(u);
            if (i%20 == 0){
                u.checkinTime = new Date();
                checkedIn.add(u);
            }
            else
                pending.add(u);
        }

        Collections.sort(allUsers, new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                String a = lhs.name;
                String b = rhs.name;
                return a.compareTo(b);
            }
        });
    }

    static ArrayList<String> first = new ArrayList<String>(){{
        add("Derek");
        add("Kyle");
        add("Shubhangi");
        add("Ryan");
        add("Devansh");
        add("Andy");
        add("Anderson");
        add("Martin");
        add("Tiffany");
        add("Oscar");
        add("Remi");
        add("Jake");
        add("John");
        add("Shalom");
        add("Rameez");
        add("Tyler");
        add("Pavan");
        add("Wilson");
        add("Linda");
        add("Nicole");
        add("Minami");
        add("Mira");
    }};

    static ArrayList<String> last = new ArrayList<String>(){{
        add("Anderson");
        add("Carnegie");
        add("Kukreja");
        add("Brigden");
        add("Jiang");
        add("Bai");
        add("Lee");
        add("Peng");
        add("Vasudev");
    }};
}