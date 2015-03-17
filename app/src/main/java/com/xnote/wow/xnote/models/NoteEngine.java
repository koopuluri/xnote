package com.xnote.wow.xnote.models;

import android.util.Log;

import com.xnote.wow.xnote.TwoThings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by koopuluri on 2/24/15.
 * This is a singleton class that is used to communicate information about the notes associated to
 * a single article. The reason this is a singleton is that both NoteActivity and ArticleActivity need
 * information about the notes associated with the current Article (NoteActivity needs it for its ViewPager,
 * and ArticleActivity needs it for updating note buffers).
 */
public class NoteEngine {
    public static final String TAG = "NoteEngine";
    Map<Integer, TwoThings<Integer, String>> mMap;
    static NoteEngine mEngine = null;

    protected NoteEngine() {
        // to prevent initialization.
    }

    public static NoteEngine getInstance() {
        if (mEngine == null) {
            mEngine = new NoteEngine();
        }
        return mEngine;
    }

    public void initializeNotes(List<ParseNote> notes) {
        mMap = new HashMap<>();
        for (ParseNote note : notes) {
            mMap.put(note.getStartIndex(),
                    new TwoThings<>(note.getEndIndex(), note.getId()));
        }
        Log.d(TAG, "setNotes() completed.");
    }


    public boolean noteExistsWithRange(int i, int j) {
        if (mMap.containsKey(i))
            if (mMap.get(i).a == j)
                return true;
        return false;
    }


    public int size() {
        return mMap.size();
    }

    public String getNoteIdAtPos(int position) {
        // TODO: always returns the first note in the keySet.
        Log.d(TAG, "getNoteIdAtPos(): " + position);
        List<Integer> keys = new ArrayList<>();
        keys.addAll(mMap.keySet());
        Collections.sort(keys);
        Log.d(TAG, "keys: " + String.valueOf(keys));
        return mMap.get(keys.get(position)).b;  // the noteId.
    }

    // TODO: (Waaay later): optimize.
    public int getPositionForNoteId(String noteId) {
        List<Map.Entry<Integer, TwoThings<Integer, String>>> entryList =
                new ArrayList<Map.Entry<Integer, TwoThings<Integer, String>>>();
        entryList.addAll(mMap.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<Integer, TwoThings<Integer, String>>>() {
            @Override
            public int compare(Map.Entry<Integer, TwoThings<Integer, String>> a,
                               Map.Entry<Integer, TwoThings<Integer, String>> b) {
                if (a.getKey() < b.getKey())
                    return -1;
                else if (a.getKey() > b.getKey())
                    return 1;
                else
                    return 0;
            }
        });
        Log.d(TAG, "entryList: " + String.valueOf(entryList));
        // finding the note in map:
        for (int i = 0; i < entryList.size(); i++) {
            Log.d(TAG, String.format("entry_note_id: %s vs. noteId: %s",
                    entryList.get(i).getValue().b, noteId));
            if (entryList.get(i).getValue().b.equals(noteId)) {
                Log.d(TAG, "noteId found in entryList: " + i);
                return i;
            }
        }
        return -1;
    }


    /**
     * returns true if there is some part or a whole note that exists within the given range.
     * @return
     */
    public boolean notesPresentWithinRange(int start, int end) {
        // TODO: ensure that inclusivity is correct.
        for (int noteStart : mMap.keySet()) {
            if (noteStart >= start && noteStart <= end) {
                Log.d(TAG, "note start in range. noteId: " + mMap.get(noteStart).b);
                return true;
            } else {
                int noteEnd = mMap.get(noteStart).a;
                if (noteEnd >= start && noteEnd <= end) {
                    Log.d(TAG, "note end in range. noteId: " + mMap.get(noteStart).b);
                    return true;
                }
            }
        }
        return false;
    }


    public void addNote(ParseNote n) {
        mMap.put(n.getStartIndex(), new TwoThings<>(n.getEndIndex(), n.getId()));
        Log.d(TAG, "addNote(): " + n.getId());
    }


    public String getNoteId(int start, int end) {
        try {
            return mMap.get(start).b;
        } catch (Exception e) {
            Log.d(TAG, "note does not exist with this start: " + start);
            return null;
        }
    }

    public void removeNote(ParseNote note) {
        int start = note.getStartIndex();
        String noteId = note.getId();

        if (mMap.containsKey(start)) {
            if (mMap.get(start).b.equals(noteId)) {
                mMap.remove(start);
                return;
            }
        }
        Log.e(TAG, "could not remove: " + start + ", with noteId: " + noteId);
    }
}
