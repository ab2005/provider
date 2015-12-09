package com.seagate.alto;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Stack;

// unused for now -- meant for saving the stack state between starts

public class FragmentStack extends Stack<FragmentStack.FragmentEntry> {

    public static class FragmentEntry implements Serializable {
        public FragmentEntry(Class mClass, Bundle mArgs) {
            this.mClass = mClass;
            this.mArgs = mArgs;
        }

        Class mClass;
        Bundle mArgs;

        public JSONObject getJSON() {
            JSONObject result = new JSONObject();

            try {
                result.put("class", mClass.getName());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // put the bundle

            return result;
        }
    }

    public FragmentEntry push(FragmentEntry fragmentEntry) {
            super.push(fragmentEntry);
            save();
            return fragmentEntry;
        }

        private void save() {
            log();

            // build the json fragment array

            JSONArray fragArray = new JSONArray();

            for (FragmentEntry fe : this) {
                Log.d("cg8", "fragment " + fe.mClass.getSimpleName());
                // fe json

                fragArray.put(fe.getJSON());
            }

            Log.d("cg8", fragArray.toString());

//            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
////            writer.setIndent("  ");
//            try {
//                writer.beginArray();
//                for (Message message : messages) {
//                    writeMessage(writer, message);
//                }
//                writer.endArray();
//                writer.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


//            try {
//                FileOutputStream fos = openFileOutput("fragmentstack.bin", Context.MODE_PRIVATE);
//                ObjectOutputStream oos = new ObjectOutputStream(fos);
//                oos.writeObject(this); // write the class as an 'object'
//                oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
//                oos.close();// close the stream
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        private void log() {
            Log.d("cg8", "fragment ---");
            for (FragmentEntry fe : this) {
                Log.d("cg8", "fragment " + fe.mClass.getSimpleName());
            }
        }

        public void set(FragmentEntry fe) {
            clear();
            push(fe);
        }

        public FragmentEntry pop() {
            FragmentEntry fe = super.pop();
            save();
            return fe;
        }

    //    public FragmentStack restoreFragmentStack() {
//        // for each item on the stack
//        // instantiate the fragment and push it
//        FragmentStack result = null;
//
//        try {
//            FileInputStream fin = openFileInput("fragmentstack.bin");
//            ObjectInputStream ois = new ObjectInputStream(fin);
//            Object o = ois.readObject();
//
//            if (o instanceof FragmentStack) {
//                return (FragmentStack) o;
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (OptionalDataException e) {
//            e.printStackTrace();
//        } catch (StreamCorruptedException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }



}