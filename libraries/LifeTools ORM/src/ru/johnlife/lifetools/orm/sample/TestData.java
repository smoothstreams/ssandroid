package ru.johnlife.lifetools.orm.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

import ru.johnlife.lifetools.data.JsonOrmData;

/**
 * Created by Yan Yurkin on 5/29/2016.
 */
public class TestData extends JsonOrmData {
    //TODO: Mark important (searchable, index, whatever) fields as @DatabaseField
    @DatabaseField(id = true)
    private Long id;

    //all other fields will got to json
    private String name;
    private String[] values;

    // except those, marked with Jackson annotations to ignore.
    // You can use any Jackson annotations (http://www.baeldung.com/jackson-annotations)
    @JsonIgnore
    private Date temp;

    //TODO: make default constructor with at least package visibility
    /*orm*/ TestData() {
    }

    public TestData(long id, String name, String[] values) {
        this.id = id;
        this.name = name;
        this.values = values;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String[] getValues() {
        return values;
    }
}
