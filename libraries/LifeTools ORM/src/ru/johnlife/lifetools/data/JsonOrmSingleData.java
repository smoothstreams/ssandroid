package ru.johnlife.lifetools.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DatabaseField;

/**
 * Created by Yan Yurkin on 5/30/2016.
 */
public class JsonOrmSingleData extends JsonOrmData {
    @DatabaseField(id = true)
    @JsonIgnore
    private Long id = -1l;

}
