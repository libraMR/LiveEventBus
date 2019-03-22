package com.example.androidevbus;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

public class LiveEventBus<T> {

    private WeakHashMap<String, MyMutableLiveData<T>> mLiveDatas = new WeakHashMap<>();
    private static String removeKey;
    private LiveEventBus() {

    }

    private static class LiveEventBusHolder{
        private static final LiveEventBus INSTANCE = new LiveEventBus();
    }

    public static LiveEventBus getInstance() {
        return LiveEventBusHolder.INSTANCE;
    }

    public <T> MyMutableLiveData<T> with(String key){
        if (!mLiveDatas.containsKey(key)){
            this.removeKey = key;
            mLiveDatas.put(key,new MyMutableLiveData());
        }
        return (MyMutableLiveData<T>) mLiveDatas.get(key);
    }

    public <T> void post(String key, T t){
        this.removeKey = key;
        if(Looper.getMainLooper() == Looper.myLooper()){
            with(key).setValue(t);
        }else {
            with(key).postValue(t);
        }
    }

    private static class ObserverWrapper<T> implements Observer<T> {

        private Observer<T> observer;

        public ObserverWrapper(Observer<T> observer) {
            this.observer = observer;
        }

        @Override
        public void onChanged(@Nullable T t) {
            if (observer != null) {
                if (isCallOnObserve()) {
                    return;
                }
                observer.onChanged(t);
            }
        }

        private boolean isCallOnObserve() {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace != null && stackTrace.length > 0) {
                for (StackTraceElement element : stackTrace) {
                    if ("android.arch.lifecycle.LiveData".equals(element.getClassName()) &&
                            "observeForever".equals(element.getMethodName())) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static class MyMutableLiveData<T> extends MutableLiveData<T> {
        private WeakHashMap<String, Observer> observerMap = new WeakHashMap<>();
        private String mKey;
        private void setKey(String key){
            this.mKey = key;
        }

        private String getKey(){
            return mKey;
        }

        /*
        * 可取消的消息
        */
        public void observeForever(Observer<T> observer) {
            if (!observerMap.containsKey(removeKey)) {
                setKey(removeKey);
                observerMap.put(removeKey, new ObserverWrapper(observer));
            }
            super.observeForever(observerMap.get(removeKey));

        }

        /*
        * 取消订阅
        */
        public void removeObserver() {
            Observer realObserver = null;
            if (observerMap.containsKey(getKey())) {
                realObserver = observerMap.get(getKey());
                if(realObserver != null){
                    observerMap.remove(getKey());
                }
            } else {
                realObserver = new Observer() {
                    @Override
                    public void onChanged(@Nullable Object o) {

                    }
                };
            }
            super.removeObserver(realObserver);
        }


        /*
        * 普通消息
        */

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
            super.observe(owner, observer);
            try {
                hook(observer);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private void hook(@NonNull Observer<T> observer) throws Exception {
            Class<LiveData> classLiveData = LiveData.class;
            Field fieldObservers = classLiveData.getDeclaredField("mObservers");
            fieldObservers.setAccessible(true);
            Object objectObservers = fieldObservers.get(this);
            Class<?> classObservers = objectObservers.getClass();

            Method methodGet = classObservers.getDeclaredMethod("get", Object.class);
            methodGet.setAccessible(true);
            Object objectWrapperEntry = methodGet.invoke(objectObservers, observer);
            Object objectWrapper = null;
            if (objectWrapperEntry instanceof Map.Entry) {
                objectWrapper = ((Map.Entry) objectWrapperEntry).getValue();
            }
            if (objectWrapper == null) {
                throw new NullPointerException("Wrapper can not be bull!");
            }
            Class<?> classObserverWrapper = objectWrapper.getClass().getSuperclass();

            Field fieldLastVersion = classObserverWrapper.getDeclaredField("mLastVersion");
            fieldLastVersion.setAccessible(true);
            Field fieldVersion = classLiveData.getDeclaredField("mVersion");
            fieldVersion.setAccessible(true);
            Object objectVersion = fieldVersion.get(this);
            fieldLastVersion.set(objectWrapper, objectVersion);
        }
    }


}
