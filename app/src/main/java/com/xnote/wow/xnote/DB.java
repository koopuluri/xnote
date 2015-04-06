package com.xnote.wow.xnote;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.xnote.wow.xnote.models.ParseArticle;
import com.xnote.wow.xnote.models.ParseConstant;
import com.xnote.wow.xnote.models.ParseFeedback;
import com.xnote.wow.xnote.models.ParseImage;
import com.xnote.wow.xnote.models.ParseNote;
import com.xnote.wow.xnote.models.ParseUserInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Integrates Parse.
 * Created by koopuluri on 2/5/15.
 */
public class DB {
    public static final String TAG = "DB";
    public static final String ARTICLE = "Article";
    public static final String NOTE = "Note";
    public static final String IMAGE = "Image";
    public static final String FEEDBACK = "Feedback";
    public static final String CONSTANT = "Constant";
    public static final String PARSE_USER_INFO = "ParseUserInfo";

    public static void sync() throws ParseException {
        List<ParseObject> cloudArticleObjects = (List<ParseObject>)(List<?>) getArticlesFromCloud();
        List<ParseObject> localArticleObjects = (List<ParseObject>)(List<?>) getArticlesLocally();

        List<ParseObject> cloudNoteObjects = (List<ParseObject>)(List<?>) getAllNotesFromCloud();
        List<ParseObject> localNoteObjects = (List<ParseObject>)(List<?>) getAllNotesLocally();

//        List<ParseObject> cloudImageObjects = (List<ParseObject>)(List<?>) getAllImagesFromCloud();
//        List<ParseObject> localImageObjects = (List<ParseObject>)(List<?>) getAllImagesLocally();

        if((cloudNoteObjects.size() == 0) && (localNoteObjects.size() == 0)) {
            Log.d(TAG, "Setting is new to true");
            setIsNew(true);
        } else {
            Log.d(TAG, "Setting is new to false");
            setIsNew(false);
        }

        syncObjects(localArticleObjects, cloudArticleObjects);
        syncObjects(localNoteObjects, cloudNoteObjects);

        Log.d(TAG, "sync complete.");
    }

    //-----------------------------------GET ARTICLES AND NOTES-------------------------------------

    public static void setIsNew(boolean isNew) {
        Log.d(TAG, "setIsNew():");
        ParseQuery query = ParseQuery.getQuery(PARSE_USER_INFO);
        query.fromLocalDatastore();
        try {
            ParseUserInfo info = (ParseUserInfo) query.getFirst();
            if (info == null) {
                Log.d(TAG, "Set is new set to: " + isNew);
                info = new ParseUserInfo();
                info.setIsNew(isNew);
                info.pin();
            } else {
                Log.d(TAG, "info is not null isUserNew().");
                if (info.getIsNew() != isNew) {
                    info.setIsNew(isNew);
                    info.pin();
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "setIsNew Error: " + e);
            ParseUserInfo info = new ParseUserInfo();
            info = new ParseUserInfo();
            info.setIsNew(isNew);
            try {
                Log.d(TAG, "Set is new set to " + isNew);
                info.pin();
            } catch (ParseException e2) {
                Log.e(TAG, "couldn't pin: " + e2);
            }
        }
    }


    public static boolean isNew() {
        ParseQuery query = ParseQuery.getQuery(PARSE_USER_INFO);
        query.fromLocalDatastore();
        try {
            ParseUserInfo info = (ParseUserInfo) query.getFirst();
            if (info != null) {
                return info.getIsNew();
            }
        } catch (ParseException e) {
            Log.e(TAG, "isNew() error: " + e);
        }
        return true;
    }


    private static void syncObjects(List<ParseObject> localObjects,
                                    List<ParseObject> cloudObjects) throws ParseException{
        Set<String> A = new HashSet<String>();
        for (ParseObject o : localObjects) {
            A.add(o.getObjectId());
        }

        Set<String> B = new HashSet<String>();
        for (ParseObject o : cloudObjects) {
            B.add(o.getObjectId());
        }
        A.retainAll(B);  // 'A' is now the intersection.

        // handling local
        for (ParseObject o : localObjects) {
            if (!A.contains(o.getObjectId())) {
                o.unpin();
            }
        }

        // pinning cloudArticle not already in local:
        for (ParseObject o : cloudObjects) {
            if (!A.contains(o.getObjectId())) {
                o.pin();
            }
        }

        Log.d(TAG, "syncObjects() complete.");
    }


    public static List<ParseImage> getAllImagesLocally() throws ParseException {
        Log.d(TAG, "getAllImagesLocally().");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(IMAGE);
        query.fromLocalDatastore();
        return (List<ParseImage>)(List<?>) query.find();
    }

    public static List<ParseImage> getAllImagesFromCloud() throws ParseException {
        Log.d(TAG, "getAllImagesFromCloud().");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(IMAGE);
        return (List<ParseImage>)(List<?>) query.find();
    }


    public static List<ParseNote> getAllNotesLocally() throws ParseException {
        Log.d(TAG, "getAllNotesLocally().");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(NOTE);
        query.fromLocalDatastore();
        return (List<ParseNote>)(List<?>) query.find();
    }

    public static List<ParseNote> getAllNotesFromCloud() throws ParseException {
        Log.d(TAG, "getAllNotesFromCloud().");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(NOTE);
        return (List<ParseNote>)(List<?>) query.find();
    }


    public static ParseArticle getLocalArticle(String articleId) {
        if (articleId == null) {
            Log.e(TAG, "articleId is null in getArticle()!");
            return null;
        }

        Log.d(TAG, "getArticle() with articleId: " + articleId);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ARTICLE);
        query.whereEqualTo(ParseArticle.ID, articleId);
        query.fromLocalDatastore();
        List<ParseObject> out;
        try {
            out = query.find();
            if (out.size() == 0) {
                Log.d(TAG, "no article found with this id: " + articleId);
                return null;
            }
            return (ParseArticle) out.get(0);

        } catch (ParseException e) {
            Log.e(TAG, "ParseException e in getArticle(): " + e);
        }
        return null;
    }

    public static ParseNote getLocalNote(String noteId) {
        Log.d(TAG, "getNote() with noteId: " + noteId);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(NOTE);
        query.whereEqualTo(ParseNote.ID, noteId);
        query.fromLocalDatastore();
        List<ParseObject> out;
        try {
            out = query.find();
            Log.d(TAG, "length of getNote() output: " + out.size());
            ParseNote outNote = (ParseNote) out.get(out.size() - 1);
            Log.d(TAG, "getNote() returns note w/: " + outNote.getStartIndex() + ", "
                    + outNote.getEndIndex());
            Log.d(TAG, "getNote(): id: " + outNote.getId());
            Log.d(TAG, "getNote(): articleId: " + outNote.getArticleId());
            return outNote;

        } catch (ParseException e) {
            Log.e(TAG, "ParseException e in getNote(): " + e);
        }
        return null;
    }

    public static List<ParseArticle> getArticlesFromCloud() throws ParseException{
        Log.d(TAG, "getArticlesFromCloud().");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ARTICLE);
        query.orderByDescending(ParseArticle.TIMESTAMP);
        if(!Util.IS_ANON) {
            try {
                List<ParseArticle> out = (List<ParseArticle>) (List<?>) query.find();
                Log.d(TAG, "getArticlesFromCloud(): size of list: " + out.size());
                return out;
            } catch (ParseException e) {
                Log.e(TAG, "couldn't getArticlesFromCloud()!");
                throw e;
            }
        } else {
            return null;
        }
    }


    public static List<ParseArticle> getArticlesLocally() {
        Log.d(TAG, "getArticlesLocally().");
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ARTICLE);
        query.fromLocalDatastore();
        query.orderByDescending(ParseArticle.TIMESTAMP);
        try {
            List<ParseArticle> out = (List<ParseArticle>)(List<?>) query.find();
            Log.d(TAG, "getArticlesLocally(): size of list: " + out.size());
            return out;
        } catch (ParseException e) {
            Log.e(TAG, "couldn't getArticlesLocally()!");
            return null;
        }
    }


    public static List<ParseNote> getNotesForArticleLocally(String articleId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(NOTE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        try {
            List<ParseObject> notes = query.find();
            Log.d(TAG, "getNotesForArticleLocally() number of notes found: " + notes.size());
            List<ParseNote> out = new ArrayList<>();
            for (ParseObject obj : notes) {
                out.add((ParseNote) obj);   // TODO: don't do this! :(
            }
            return out;

        } catch (ParseException e) {
            Log.e(TAG, "ParseException when getting notesForArticleLocally(): " + e);
            return null;
        }
    }


    public static List<ParseNote> getNotesForArticleFromCloud(String articleId)
            throws ParseException{
        ParseQuery<ParseObject> query = ParseQuery.getQuery(NOTE);
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        if(!Util.IS_ANON) {
            try {
                List<ParseObject> notes = query.find();
                Log.d(TAG, "getNotesForArticleFromCloud() number of notes found: " + notes.size());
                List<ParseNote> out = new ArrayList<>();
                for (ParseObject obj : notes) {
                    out.add((ParseNote) obj);
                }
                return out;

            } catch (ParseException e) {
                Log.e(TAG, "ParseException when getting notesForArticleFromCloud(): " + e);
                throw e;
            }
        } else {
            return null;
        }
    }

    //---------------------------------SAVING ARTICLES----------------------------------------------

    public static void saveArticleImmediately(final ParseArticle article) {
        saveArticleImmediatelyLocally(article);
        Log.d(TAG, "saveArticleImmediatelyLocally() completed saving locally.");
        if (!Util.IS_ANON) {
            Log.d(TAG, "saveArticleImmediately() not anon user, so saving eventually to cloud.");
            saveArticleImmediatelyToCloud(article);
        }
    }

    public static void saveArticleImmediatelyLocally(final ParseArticle article) {
        Log.d(TAG, "saveArticleImmediatelyLocally() with id: " + article.getId());
        try {
            article.pin();
        } catch (ParseException e) {
            Log.e(TAG, "could not save article immediately!" + e);
        }
    }

    public static void saveArticleImmediatelyToCloud(final ParseArticle article) {
        Log.d(TAG, "saveArticleImmediatelyToCloud() with id: " + article.getId());
        try {
            article.save();
        } catch (ParseException e) {
            article.setContent("<br> <h2> The article could not be saved to the cloud. It will " +
                    " not show up if you refresh or login from another device. We are working hard" +
                    " to fix this problem. </h2>");
            Log.e(TAG, "could not save article immediately!" + e);
        }
    }

    public static void saveArticle(final ParseArticle article) {
        Log.d(TAG, "saveArticle() with id: " + article.getId());
        saveArticleLocally(article);
        Log.d(TAG, "saveArticle() completed saving locally.");
        if (!Util.IS_ANON) {
            Log.d(TAG, "saveArticle() not anon user, so saving eventually to cloud.");
            saveArticleToCloud(article);
        }
    }


    private static void saveArticleToCloud(final ParseArticle article) {
        ParseQuery query = ParseQuery.getQuery(DB.ARTICLE);
        query.whereEqualTo(ParseArticle.ID, article.getId());
        query.getFirstInBackground(new GetCallback() {
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    Log.d(TAG, "message from getFirstInBackground() in saveArticleToCloud(): " + e);
                }
                if (object == null) {
                    Log.d(TAG, "saveArticleToCloud(): No items with this articleId in db!.");
                    article.saveEventually();
                } else {
                    Log.d(TAG, "saveArticleToCloud(): article already exists with this articleid");
                    // article.setObjectId(object.getObjectId());
                    ParseArticle other = (ParseArticle) object;
                    Util.copyOverArticle(other, article);
                    other.saveEventually();
                }
            }
        });
    }


    public static void saveArticleLocally(final ParseArticle article) {
        ParseQuery query = ParseQuery.getQuery(DB.ARTICLE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseArticle.ID, article.getId());
        query.getFirstInBackground(new GetCallback() {
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    Log.d(TAG, "message from getFirstInBackground() in saveArticle(): " + e);
                }
                if (object == null) {
                    Log.d(TAG, "saveArticleLocally(): No items with this articleId in db!.");
                    article.pinInBackground();
                } else {
                    Log.d(TAG, "saveArticleLocally(): article already exists with this articleid");
                    // article.setObjectId(object.getObjectId());
                    ParseArticle other = (ParseArticle) object;
                    if(other.isParsed()) {
                        Util.copyOverArticle(other, article);
                    }
                    other.pinInBackground();
                }
            }
        });
    }

    //---------------------------------------SAVING NOTES-------------------------------------------

    public static void saveNote(final ParseNote note) {
        Log.d(TAG, "saveNote(): " + note.getStartIndex() + ", " + note.getEndIndex());
        Log.d(TAG, "saveNote():  noteid: " + note.getId());
        saveNoteLocally(note);
        Log.d(TAG, "note saved locally.");
        if (!Util.IS_ANON) {
            Log.d(TAG, "not an anon user, so also saving note eventually.");
            saveNoteToCloud(note);
        }
        // toggling is user new:
        if (isNew()) {
            setIsNew(false);
        }
    }


    public static void saveNoteLocally(final ParseNote note) {
        ParseQuery query = ParseQuery.getQuery(DB.NOTE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseNote.ID, note.getId());
        query.getFirstInBackground(new GetCallback() {
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "saveNoteLocally: " + e);
                }
                if (object != null) {
                    Log.d(TAG, "saveNoteLocally(): a note exists with same noteId.");
                    note.setObjectId(object.getObjectId());
                }
                note.pinInBackground();
            }
        });
        if (isNew()) {
            setIsNew(false);
        }
    }


    private static void saveNoteToCloud(final ParseNote note) {
        note.remove(ParseNote.TIMESTAMP);
        ParseQuery query = ParseQuery.getQuery(DB.NOTE);
        query.whereEqualTo(ParseNote.ID, note.getId());
        query.getFirstInBackground(new GetCallback() {
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    Log.d(TAG, "message from getFirstInBackground() in saveNoteToCloud(): " + e);
                }
                if (object == null) {
                    Log.d(TAG, "saveNoteToCloud(): No items with this noteId in db!.");
                    note.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "saveNoteToCloud() parseException: " + e);
                            } else {
                                Log.d(TAG, "no error in saveEventually.");
                            }
                        }
                    });
                }
            }
        });
    }

    //--------------------------------DELETING ARTICLES AND ASSOCIATED NOTES------------------------

    public static void clearLocalArticles() {
        Log.d(TAG, "clearLocalArticle().");
        List<ParseArticle> articles = DB.getArticlesLocally();
        for (ParseArticle a : articles) {
            deleteArticleLocally(a);
        }
    }


    public static void clearLocalNotes() {
        Log.d(TAG, "clearLocalNotes().");
        ParseQuery query = ParseQuery.getQuery(DB.NOTE);
        query.fromLocalDatastore();
        try {
            List<ParseObject> localNotes = query.find();
            for (ParseObject note : localNotes) {
                note.unpin();
            }
            Log.d(TAG, "local notes cleared.");
        } catch (ParseException e) {
            Log.e(TAG, "could not delete local notes: " + e);
        }
    }


    private static void deleteLocalNotesForArticleInBackground(final String articleId) {
        final String tag = TAG + ".deleteLocalNotesForArticleInBackground(): ";
        ParseQuery query = ParseQuery.getQuery(NOTE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        query.findInBackground(new FindCallback<ParseNote>() {
            @Override
            public void done(List<ParseNote> list, ParseException e) {
                for (ParseNote obj : list) {
                    // unpin in background:
                    Log.d(tag, "note with note id deleted locally: " + obj.getId());
                    obj.unpinInBackground();
                }
            }
        });
    }

    private static void deleteCloudNotesForArticleInBackground(final String articleId) {
        final String tag = TAG + ".deleteCloudNotesForArticleInBackground(): ";
        ParseQuery query = ParseQuery.getQuery(NOTE);
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        query.findInBackground(new FindCallback<ParseNote>() {
            @Override
            public void done(List<ParseNote> list, ParseException e) {
                for (ParseNote obj : list) {
                    // delete in background:
                    Log.d(tag, "note with note id deleted in cloud: " + obj.getId());
                    obj.deleteEventually();
                }
            }
        });
    }

    public static void deleteLocalNotesForArticle(final String articleId) {
        final String tag = TAG + ".deleteLocalNotesForArticle(): ";
        Log.d(tag, "");
        ParseQuery query = ParseQuery.getQuery(NOTE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        List<ParseObject> list = null;
        try {
            list = query.find();
        } catch(ParseException e) {
            Log.e(TAG, "Error finding notes for article locally");
        }
        if(list != null) {
            for (ParseObject obj : list) {
                    deleteLocalNoteImmediately((ParseNote)obj);
            }
        }
    }

    private static void deleteLocalNoteImmediately(ParseNote note) {
        try {
            note.unpin();
        } catch (ParseException e) {
            Log.e(TAG, "deleteLocalNoteImmediately(): " + e.getMessage());
        }
    }

    private static void deleteCloudNotesForArticle(final String articleId) {
        final String tag = TAG + ".deleteCloudNotesForArticle(): ";
        Log.d(tag, "");
        ParseQuery query = ParseQuery.getQuery(NOTE);
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        List<ParseObject> list = null;
        try {
            list = query.find();
        } catch(ParseException e) {
            Log.e(TAG, "Error finding notes for article locally");
        }
        if(list != null) {
            for (ParseObject obj : list) {
                deleteCloudNoteImmediately((ParseNote) obj);
            }
        }
    }


    private static void deleteCloudNoteImmediately(ParseNote note) {
        try {
            note.delete();
        } catch (ParseException e) {
            Log.e(TAG, "deleteCloudNoteImmediately(): " + e.getMessage());
        }
    }

    public static void deleteArticle(final ParseArticle article) {
        Log.d(TAG, "deleteArticle(ParseArticle): " + article.getId());
        deleteArticleLocally(article);
        deleteLocalNotesForArticle(article.getId());
        if (!Util.IS_ANON) {
            deleteArticleInCloud(article);
            deleteCloudNotesForArticle(article.getId());
        }
    }



    private static void deleteArticleLocally(final ParseArticle article) {
        try {
            article.unpin();
        } catch (ParseException e) {
            Log.e(TAG, "article could not be deleted locally: " + article.getId());
        }
        deleteLocalNotesForArticle(article.getId());
    }

    private static void deleteArticleInCloud(final ParseArticle article) {
        try {
            article.delete();
        } catch (ParseException e) {
            Log.e(TAG, "could not delete article from cloud: " + e);
        }
        deleteCloudNotesForArticle(article.getId());
    }


    public static void deleteArticleInBackground(final ParseArticle article) {
        Log.d(TAG, "deleteArticleInBackground(ParseArticle): " + article.getId());
        article.unpinInBackground();
        deleteLocalNotesForArticleInBackground(article.getId());
        if (!Util.IS_ANON) {
            article.deleteInBackground();
            deleteCloudNotesForArticleInBackground(article.getId());
        }
    }


    public static void deleteArticle(final String articleId) {
        Log.d(TAG, "deleteArticle(article_id): " + articleId);
        deleteArticleLocally(articleId);
        deleteLocalNotesForArticle(articleId);

        if (!Util.IS_ANON) {
            Log.d(TAG, "deleteArticle(article_id): user is not anon, so deleting from cloud as well.");
            deleteArticleInCloud(articleId);
            deleteCloudNotesForArticle(articleId);
        }
    }


    private static void deleteArticleLocally(final String articleId) {
        ParseQuery query = ParseQuery.getQuery(DB.ARTICLE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseArticle.ID, articleId);
        ParseObject object = null;
        try {
            object = query.getFirst();
        } catch (ParseException e){
            Log.d(TAG, "could not get article for deletion locally. Exception: " + e);
        }
        if (object == null) {
            Log.d(TAG, "could not get ParseArticle locally for deletion with id: " + articleId);
        } else {
            try {
                object.unpin();
                deleteLocalNotesForArticle(articleId);
                Log.d(TAG, "unpin launched with id: " + articleId);
            } catch(ParseException e2) {
                Log.e(TAG, "Article could not be deleted " + e2.getMessage());
            }
        }
    }

    private static void deleteArticleInCloud(final String articleId) {
        ParseQuery query = ParseQuery.getQuery(DB.ARTICLE);
        query.whereEqualTo(ParseArticle.ID, articleId);
        ParseObject object = null;
        try {
            object = query.getFirst();
        } catch (ParseException e){
            Log.d(TAG, "could not get article for deletion in cloud. Exception: " + e);
        }
        if (object == null) {
            Log.d(TAG, "could not get ParseArticle in cloud for deletion with id: " + articleId);
        } else {
            try {
                object.delete();
                deleteCloudNotesForArticle(articleId);
            } catch(ParseException e2) {
                Log.e(TAG, "Article could not be deleted " + e2.getMessage());
            }
            Log.d(TAG, "unpin launched with id: " + articleId);
        }
    }

    //-----------------------------------DELETE NOTES-----------------------------------------------

//    public static void deleteNote(final ParseNote note) {
//        Log.d(TAG, "deletNote(note):" + note);
//        deleteNoteLocallyInBackground(note);
//        if (!Util.IS_ANON) {
//            Log.d(TAG, "deleteNote(): user is not anon, so deleting from cloud as well.");
//            deleteNoteInCloudInBackground(note);
//        }
//    }

    private static void deleteNoteLocallyInBackground(final ParseNote note) {
        note.unpinInBackground();
    }

    private static void deleteNoteInCloudInBackground(final ParseNote note) {
        note.deleteEventually();
    }

    public static void deleteNote(final String noteId) {
        Log.d(TAG, "deleteNote(noteId):" + noteId);
        deleteNoteLocally(noteId);
        if (!Util.IS_ANON) {
            Log.d(TAG, "deleteNote(noteId): user is not anon, so deleting from cloud as well.");
            deleteNoteInCloud(noteId);
        }
    }

    private static void deleteNoteLocally(final String noteId) {
        ParseQuery query = ParseQuery.getQuery(DB.NOTE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseNote.ID, noteId);
        query.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Log.d(TAG, "could not get note for deletion locally. Exception: " + e);
                } else {
                    if (parseObject == null) {
                        Log.d(TAG, "could not get note for deletion locally with id: " + noteId);
                    } else {
                        parseObject.unpinInBackground();
                        Log.d(TAG, "deletedNote locally with id: " + noteId);
                    }
                }
            }
        });
    }

    private static void deleteNoteInCloud(final String noteId) {
        ParseQuery query = ParseQuery.getQuery(DB.NOTE);
        query.whereEqualTo(ParseNote.ID, noteId);
        query.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Log.d(TAG, "could not get note for deletion in cloud. Exception: " + e);
                } else {
                    if (parseObject == null) {
                        Log.d(TAG, "could not get note for deletion in cloud with id: " + noteId);
                    } else {
                        parseObject.deleteEventually();
                        Log.d(TAG, "deletedNote in cloud with id: " + noteId);
                    }
                }
            }
        });
    }


    //------------------------------------SOME IMAGE STUFF------------------------------------------

    public static void saveImage(ParseImage image) {
        saveImageLocally(image);
        if(!Util.IS_ANON) {
            saveImageToCloud(image);
        }
    }

    public static void saveImageLocally(ParseImage image) {
        try {
            image.pin();
        } catch (ParseException e) {
            Log.d(TAG, "saveImageLocally(): " + e);
        }
    }

    public static void saveImageToCloud(ParseImage image) {
        try {
            image.save();
        } catch(ParseException e) {
            Log.e(TAG, "saveImageToCloud()" + e);
        }
    }

    public static ParseImage getImage(String imageUrlString) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(IMAGE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseImage.URL, imageUrlString);
        List<ParseObject> out;
        try {
            out = query.find();
            if (out.size() == 0) {
                Log.d(TAG, "getImageLocally(), no image found for given imageUrlString: " +
                        imageUrlString);
                if(!Util.IS_ANON) {
                    return getImageFromCloud(imageUrlString);
                } else {
                    return null;
                }
            } else {
                ParseImage img = (ParseImage) out.get(0);
                Log.d(TAG, "image locally: " + img.getUrl() + ", " + img.getArticleId());
                //Check to see if the image has an error before returning.
                if(!img.getError()) {
                    return img;
                } else {
                    return null;
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "getImage(): " + e);
            return null;
        }
    }

    public static ParseImage getImageFromCloud(String imageUrlString) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(IMAGE);
        query.whereEqualTo(ParseImage.URL, imageUrlString);
        Log.d(TAG, "called get image from cloud");
        List<ParseObject> out;
        try {
            out = query.find();
            if (out.size() == 0) {
                Log.d(TAG, "getImageFromCloud(), no image found for given imageUrlString: " +
                        imageUrlString);
                return null;
            } else {
                ParseImage img = (ParseImage) out.get(0);
                Log.d(TAG, "image from cloud: " + img.getUrl() + ", " + img.getArticleId());
                DB.saveImageLocally(img);
                //Check to see if the image has an error before returning.
                if(!img.getError()) {
                    return img;
                } else {
                    return null;
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "getImage(): " + e);
            return null;
        }
    }

    //---------------------------FEEDBACK STUFF---------------------------
    public static void saveFeedbackInBackground(ParseFeedback feedback) {
        Log.d(TAG, "saveFeedback()");
        feedback.saveEventually();
    }
    //---------------------------DIFFBOT CONSTANT STUFF-------------------

    public static String getConstantCloud() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(DB.CONSTANT);
        try {
            List<ParseConstant> constants = (List<ParseConstant>) (List<?>) query.find();
            if(constants.size() > 0) {
                return constants.get(0).getDiffbotToken();
            } else {
                return null;
            }
        } catch (ParseException e) {
            Log.e(TAG, "getConstantCloud query error :" + e);
            return null;
        }
    }



    public static String getConstantLocally() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(DB.CONSTANT);
        query.fromLocalDatastore();
        try {
            List<ParseConstant> constants = (List<ParseConstant>) (List<?>) query.find();
            if(constants.size() > 0) {
                return constants.get(0).getDiffbotToken();
            } else {
                return null;
            }
        } catch (ParseException e) {
            Log.e(TAG, "getConstantLocally query error :" + e);
            return null;
        }
    }

    public static void clearLocalConstants() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(DB.CONSTANT);
        query.fromLocalDatastore();
        try {
            List<ParseConstant> constants = (List<ParseConstant>) (List<?>) query.find();
            for(ParseConstant constant : constants) {
                constant.unpin();
            }
        } catch (ParseException e) {
            Log.e(TAG, "clearConstantLocally error :" + e);
        }
    }

    public static void saveConstantLocally(String token) {
        ParseConstant constant = new ParseConstant();
        constant.setDiffbotToken(token);
        try {
            constant.pin();
        } catch(ParseException e) {
            Log.e(TAG, "Could not save token locally: " + e);
        }
    }

}


