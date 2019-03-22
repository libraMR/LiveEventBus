# LiveEventBus
## 好用的事件传递工具.
- [x]  生命周期感知，自动取消订阅
- [x]  当Activity处于活跃状态即OnResume()时候,可以接收消息,当Activity销毁时不接收消息,再次进入Activity可正常接收消息.

####  根目录下build.gradle配置:
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
 
#### App目录下build.gradle配置:
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.libraMR:LiveEventBus:v2.1'
	}
---
## 普通消息observe(无需主动注销订阅,随生命周期接收消息和注销订阅)  
### 例子1(String类型):  
### 发送消息
```
LiveEventBus.getInstance().with("key_name").setValue("1111");
```
### 订阅接收(在Activity的OnResume()操作)
```
@Override
protected void onResume() {
	LiveEventBus.getInstance()
               	.with("key_name")
               	.observe(this, new Observer<String>() {
                  	 @Override
                   	public void onChanged(@Nullable String string) {
				Toast.makeText(getApplicationContext(),string,Toast.LENGTH_SHORT).show();
                   	}
               	});
}
```  
### 例子2(List类型):  
### 发送消息
```
 ArrayList<Bean> list = new ArrayList<>();
 Bean bean = new Bean();
 bean.setName("张三");
 list.add(bean);
 
 LiveEventBus.getInstance().with("key_test").setValue(list);
``` 
### 订阅接收(在Activity的OnResume()操作)
```
@Override
protected void onResume() {
	LiveEventBus.getInstance()
                .with("key_test")
                .observe(this, new Observer<List<Bean>>() {
                    	@Override
                    	public void onChanged(@Nullable List<Bean> bean) {
                            for (Bean item : bean) {
                            	Toast.makeText(getApplicationContext(),item.getName(),Toast.LENGTH_SHORT).show();
                              }
                    	  }
                  });
}
```
---
## 可手动取消订阅消息observeForever(可以自己控制取消订阅)
### 例子1(String类型):  
### 发送消息
```
LiveEventBus.getInstance().with("key").setValue("1111");
```
### 订阅接收(在Activity的OnResume()操作)
```
@Override
protected void onResume() {
	LiveEventBus.getInstance()
               	.with("key")
               	.observeForever(new Observer<String>() {
                  	 @Override
                   	public void onChanged(@Nullable String string) {
				Toast.makeText(getApplicationContext(),string,Toast.LENGTH_SHORT).show();
                   	}
               	});
}
```  
### 取消订阅
```
LiveEventBus.getInstance()
         .with("key")
         .removeObserver();
```  
### 例子2(List类型):  
### 发送消息
```
 ArrayList<Bean> list = new ArrayList<>();
 Bean bean = new Bean();
 bean.setName("张三");
 list.add(bean);
 
 LiveEventBus.getInstance().with("key_two").setValue(list);
```
### 订阅接收(在Activity的OnResume()操作)
```
@Override
protected void onResume() {
	LiveEventBus.getInstance()
               	.with("key_two")
               	.observeForever(new Observer<List<Bean>>() {
                  	 @Override
                   	public void onChanged(@Nullable List<Bean> bean) {
                            for (Bean item : bean) {
                            	Toast.makeText(getApplicationContext(),item.getName(),Toast.LENGTH_SHORT).show();
                              }
                   	}
               	});
}
```  
### 取消订阅
```
LiveEventBus.getInstance()
         .with("key_two")
         .removeObserver();
	     
```
---
