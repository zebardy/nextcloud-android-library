/*  Nextcloud Android Library is available under MIT license
 *   Copyright (C) 2017 Alejandro Bautista
 *
 *   @author Alejandro Bautista
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */
package com.owncloud.android.lib.resources.activities.models;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * RichSubject Parser
 */

public class RichSubjectTypeAdapter extends TypeAdapter<RichSubject> {

    @Override
    public void write(JsonWriter out, RichSubject value) throws IOException {

    }

    @Override
    public RichSubject read(JsonReader in) throws IOException {
        RichSubject richSubject = new RichSubject();
        in.beginArray();
        int count = 0;
        while (in.hasNext()) {
            if (count == 0) {
                richSubject.setRichSubject(in.nextString());
            } else {
                in.beginObject();

                read(richSubject, in);

                in.endObject();
            }
            count++;
        }


        in.endArray();

        return richSubject;
    }

    private void read(RichSubject richSubject, JsonReader in) throws IOException {
        while (in.hasNext()) {
            String name = in.nextName();
            if (name != null && !name.isEmpty()) {
                richSubject.getRichObjectList().add(readObject(name,in));
            }
        }
    }



    RichObject readObject(String tag,JsonReader in) throws IOException {
        in.beginObject();
        RichObject richObject = new RichObject();
        richObject.setTag(tag);
        while (in.hasNext()) {
            String name = in.nextName();
            if ("type".contentEquals(name))
                richObject.setType(in.nextString());
            else if ("id".contentEquals(name)) {
                richObject.setId(in.nextString());
            }else if ("name".contentEquals(name))
                richObject.setName(in.nextString());
            else if ("path".contentEquals(name))
                richObject.setPath(in.nextString());
            else if ("link".contentEquals(name))
                richObject.setLink(in.nextString());
            else if ("server".contentEquals(name))
                richObject.setLink(in.nextString());
        }
        in.endObject();
        return richObject;
    }




}

