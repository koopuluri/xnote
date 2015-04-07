package com.xnote.lol.xnote;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.xnote.lol.xnote.models.ParseArticle;
import com.xnote.lol.xnote.models.ParseConstant;
import com.xnote.lol.xnote.models.ParseFeedback;
import com.xnote.lol.xnote.models.ParseImage;
import com.xnote.lol.xnote.models.ParseNote;
import com.xnote.lol.xnote.models.ParseUserInfo;

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
            setIsNew(true);
        } else {
            setIsNew(false);
        }
        syncObjects(localArticleObjects, cloudArticleObjects);
        syncObjects(localNoteObjects, cloudNoteObjects);
    }

    //-----------------------------------GET ARTICLES AND NOTES-------------------------------------

    public static void setIsNew(boolean isNew) {
        ParseQuery query = ParseQuery.getQuery(PARSE_USER_INFO);
        query.fromLocalDatastore();
        try {
            ParseUserInfo info = (ParseUserInfo) query.getFirst();
            if (info == null) {
                info = new ParseUserInfo();
                info.setIsNew(isNew);
                info.pin();
            } else {
                if (info.getIsNew() != isNew) {
                    info.setIsNew(isNew);
                    info.pin();
                }
            }
        } catch (ParseException e) {
            ParseUserInfo info = new ParseUserInfo();
            info = new ParseUserInfo();
            info.setIsNew(isNew);
            try {
                info.pin();
            } catch (ParseException e2) {
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
    }


    public static List<ParseImage> getAllImagesLocally() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(IMAGE);
        query.fromLocalDatastore();
        return (List<ParseImage>)(List<?>) query.find();
    }

    public static List<ParseImage> getAllImagesFromCloud() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(IMAGE);
        return (List<ParseImage>)(List<?>) query.find();
    }


    public static List<ParseNote> getAllNotesLocally() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(NOTE);
        query.fromLocalDatastore();
        return (List<ParseNote>)(List<?>) query.find();
    }

    public static List<ParseNote> getAllNotesFromCloud() throws ParseException {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(NOTE);
        return (List<ParseNote>)(List<?>) query.find();
    }


    public static ParseArticle getLocalArticle(String articleId) {
        if (articleId == null) {
            return null;
        }
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ARTICLE);
        query.whereEqualTo(ParseArticle.ID, articleId);
        query.fromLocalDatastore();
        List<ParseObject> out;
        try {
            out = query.find();
            if (out.size() == 0) {
                return null;
            }
            return (ParseArticle) out.get(0);
        } catch (ParseException e) {
        }
        return null;
    }

    public static ParseNote getLocalNote(String noteId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(NOTE);
        query.whereEqualTo(ParseNote.ID, noteId);
        query.fromLocalDatastore();
        List<ParseObject> out;
        try {
            out = query.find();
            ParseNote outNote = (ParseNote) out.get(out.size() - 1);
            return outNote;
        } catch (ParseException e) {
        }
        return null;
    }

    public static List<ParseArticle> getArticlesFromCloud() throws ParseException{
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ARTICLE);
        query.orderByDescending(ParseArticle.TIMESTAMP);
        if(!Util.IS_ANON) {
            try {
                List<ParseArticle> out = (List<ParseArticle>) (List<?>) query.find();
                return out;
            } catch (ParseException e) {
                throw e;
            }
        } else {
            return null;
        }
    }


    public static List<ParseArticle> getArticlesLocally() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ARTICLE);
        query.fromLocalDatastore();
        query.orderByDescending(ParseArticle.TIMESTAMP);
        try {
            List<ParseArticle> out = (List<ParseArticle>)(List<?>) query.find();
            return out;
        } catch (ParseException e) {
            return null;
        }
    }


    public static List<ParseNote> getNotesForArticleLocally(String articleId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(NOTE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        try {
            List<ParseObject> notes = query.find();
            List<ParseNote> out = new ArrayList<>();
            for (ParseObject obj : notes) {
                out.add((ParseNote) obj);   // TODO: don't do this! :(
            }
            return out;

        } catch (ParseException e) {
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
                List<ParseNote> out = new ArrayList<>();
                for (ParseObject obj : notes) {
                    out.add((ParseNote) obj);
                }
                return out;

            } catch (ParseException e) {
                throw e;
            }
        } else {
            return null;
        }
    }

    //---------------------------------SAVING ARTICLES----------------------------------------------

    public static void saveArticleImmediately(final ParseArticle article) {
        saveArticleImmediatelyLocally(article);
        if (!Util.IS_ANON) {
            saveArticleImmediatelyToCloud(article);
        }
    }

    public static void saveArticleImmediatelyLocally(final ParseArticle article) {
        try {
            article.pin();
        } catch (ParseException e) {
        }
    }

    public static void saveArticleImmediatelyToCloud(final ParseArticle article) {
        try {
            article.save();
        } catch (ParseException e) {
            article.setContent("<br> <h2> The article could not be saved to the cloud. It will " +
                    " not show up if you refresh or login from another device. We are working hard" +
                    " to fix this problem. </h2>");
        }
    }

    public static void saveArticle(final ParseArticle article) {
        saveArticleLocally(article);
        if (!Util.IS_ANON) {
            saveArticleToCloud(article);
        }
    }


    private static void saveArticleToCloud(final ParseArticle article) {
        ParseQuery query = ParseQuery.getQuery(DB.ARTICLE);
        query.whereEqualTo(ParseArticle.ID, article.getId());
        query.getFirstInBackground(new GetCallback() {
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                }
                if (object == null) {
                    article.saveEventually();
                } else {
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
                }
                if (object == null) {
                    article.pinInBackground();
                } else {
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
        saveNoteLocally(note);
        if (!Util.IS_ANON) {
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
                }
                if (object != null) {
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
                }
                if (object == null) {
                    note.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                            } else {
                            }
                        }
                    });
                }
            }
        });
    }

    //--------------------------------DELETING ARTICLES AND ASSOCIATED NOTES------------------------

    public static void clearLocalArticles() {
        List<ParseArticle> articles = DB.getArticlesLocally();
        for (ParseArticle a : articles) {
            deleteArticleLocally(a);
        }
    }


    public static void clearLocalNotes() {
        ParseQuery query = ParseQuery.getQuery(DB.NOTE);
        query.fromLocalDatastore();
        try {
            List<ParseObject> localNotes = query.find();
            for (ParseObject note : localNotes) {
                note.unpin();
            }
        } catch (ParseException e) {
        }
    }


    private static void deleteLocalNotesForArticleInBackground(final String articleId) {
        ParseQuery query = ParseQuery.getQuery(NOTE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        query.findInBackground(new FindCallback<ParseNote>() {
            @Override
            public void done(List<ParseNote> list, ParseException e) {
                for (ParseNote obj : list) {
                    // unpin in background:
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
                    obj.deleteEventually();
                }
            }
        });
    }

    public static void deleteLocalNotesForArticle(final String articleId) {
        final String tag = TAG + ".deleteLocalNotesForArticle(): ";
        ParseQuery query = ParseQuery.getQuery(NOTE);
        query.fromLocalDatastore();
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        List<ParseObject> list = null;
        try {
            list = query.find();
        } catch(ParseException e) {
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
        }
    }

    private static void deleteCloudNotesForArticle(final String articleId) {
        ParseQuery query = ParseQuery.getQuery(NOTE);
        query.whereEqualTo(ParseNote.ARTICLE_ID, articleId);
        List<ParseObject> list = null;
        try {
            list = query.find();
        } catch(ParseException e) {
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
        }
    }

    public static void deleteArticle(final ParseArticle article) {
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
        }
        deleteLocalNotesForArticle(article.getId());
    }

    private static void deleteArticleInCloud(final ParseArticle article) {
        try {
            article.delete();
        } catch (ParseException e) {
        }
        deleteCloudNotesForArticle(article.getId());
    }


    public static void deleteArticleInBackground(final ParseArticle article) {
        article.unpinInBackground();
        deleteLocalNotesForArticleInBackground(article.getId());
        if (!Util.IS_ANON) {
            article.deleteInBackground();
            deleteCloudNotesForArticleInBackground(article.getId());
        }
    }


    public static void deleteArticle(final String articleId) {
        deleteArticleLocally(articleId);
        deleteLocalNotesForArticle(articleId);
        if (!Util.IS_ANON) {
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
        }
        if (object == null) {
        } else {
            try {
                object.unpin();
                deleteLocalNotesForArticle(articleId);
            } catch(ParseException e2) {
            }
        }
    }

    private static void deleteArticleInCloud(final String articleId) {
        ParseQuery query = ParseQuery.getQuery(DB.ARTICLE);
        query.whereEqualTo(ParseArticle.ID, articleId);
        ParseObject object = null;
        try {
            object = query.getFirst();
        } catch (ParseException e) {
        }
        if (object == null) {
        } else {
            try {
                object.delete();
                deleteCloudNotesForArticle(articleId);
            } catch (ParseException e2) {
            }
        }
    }
    //-----------------------------------DELETE NOTES-----------------------------------------------


    private static void deleteNoteLocallyInBackground(final ParseNote note) {
        note.unpinInBackground();
    }

    private static void deleteNoteInCloudInBackground(final ParseNote note) {
        note.deleteEventually();
    }

    public static void deleteNote(final String noteId) {
        deleteNoteLocally(noteId);
        if (!Util.IS_ANON) {
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
                } else {
                    if (parseObject == null) {
                    } else {
                        parseObject.unpinInBackground();
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
                } else {
                    if (parseObject == null) {
                    } else {
                        parseObject.deleteEventually();
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
        }
    }

    public static void saveImageToCloud(ParseImage image) {
        try {
            image.save();
        } catch(ParseException e) {
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
                if(!Util.IS_ANON) {
                    return getImageFromCloud(imageUrlString);
                } else {
                    return null;
                }
            } else {
                ParseImage img = (ParseImage) out.get(0);
                //Check to see if the image has an error before returning.
                if(!img.getError()) {
                    return img;
                } else {
                    return null;
                }
            }
        } catch (ParseException e) {
            return null;
        }
    }

    public static ParseImage getImageFromCloud(String imageUrlString) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(IMAGE);
        query.whereEqualTo(ParseImage.URL, imageUrlString);
        List<ParseObject> out;
        try {
            out = query.find();
            if (out.size() == 0) {
                return null;
            } else {
                ParseImage img = (ParseImage) out.get(0);
                DB.saveImageLocally(img);
                //Check to see if the image has an error before returning.
                if(!img.getError()) {
                    return img;
                } else {
                    return null;
                }
            }
        } catch (ParseException e) {
            return null;
        }
    }

    //---------------------------FEEDBACK STUFF---------------------------
    public static void saveFeedbackInBackground(ParseFeedback feedback) {
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
        }
    }

    public static void saveConstantLocally(String token) {
        ParseConstant constant = new ParseConstant();
        constant.setDiffbotToken(token);
        try {
            constant.pin();
        } catch(ParseException e) {
        }
    }
}


