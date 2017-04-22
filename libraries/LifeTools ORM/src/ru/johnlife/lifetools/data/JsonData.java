package ru.johnlife.lifetools.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Created by Yan Yurkin on 5/29/2016.
 */
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE)
public class JsonData extends AbstractData {

}
