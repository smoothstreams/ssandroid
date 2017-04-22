package ru.johnlife.lifetools.data;

import android.util.SparseArray;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

import java.io.IOException;

/**
 *
 */
public class JsonOrmData extends JsonData {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final SparseArray<ObjectWriter> writers = new SparseArray<>();

    @DatabaseField(dataType = DataType.LONG_STRING, useGetSet = true) @JsonIgnore
    protected final String json = "";

    private ObjectWriter getObjectWriter() {
        int hash = getClass().hashCode();
        ObjectWriter writer = writers.get(hash);
        if (null == writer) {
            writer = mapper.writerFor(getClass());
            writers.put(hash, writer);
        }
        return writer;
    }

    /**
     * Do not use! Used internally by ORM
     * @hide
     */
    public final String getJson() {
        try {
            return getObjectWriter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Do not use! Used internally by ORM
     * @hide
     */
    public final void setJson(String json) {
        try {
            mapper.readerForUpdating(this).readValue(json);
            onAfterLoad();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onAfterLoad() {}

    @Override
    public String toString() {
        return getJson();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        try {
            return mapper.readerFor(getClass()).readValue(getJson());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
