package ru.johnlife.lifetools.data;

/**
 * Created by Yan Yurkin
 * 28 July 2016
 */
public abstract class IdentityOrmData<T> extends JsonOrmData {
    public abstract T getId();
}
