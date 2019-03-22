## 1、描述
**`CeilingLayout`用来控制子View的吸顶联滑，目前支持联滑控件：`NestedScrollView`、`RecyclerView`、`SmartRefreshLayout`；只需要在xml里配置需要吸顶子View的位置索引就能自动实现吸顶联滑效果。**

<div align=center>![结合ViewGroup使用例子](https://github.com/pirrip90/CeilingLayout/blob/master/screen/screen2.gif)</div>

## 2、模型图
<img src="https://github.com/pirrip90/CeilingLayout/blob/master/screen/screen1.png" width = "283" height = "419" div align=center />

`CeilingLayout`是`LinearLayout`的子类,使用方法与竖向`LinearLayout`一致。

如模型图所示，`CeilingLayout`竖向排列子View时，吸顶子View之后有且只能再排列一个子View，一般为联动View或包裹住联动View的父容器。

## 3、xml属性
|方法名|参数|描述|
|:---:|:---:|:---:|
| ceiling_childIndex | integer | 吸顶子View的位置索引

## 4、使用
- **xml配置**
```xml
    <!-- 设置位置索引为1的子View吸顶 -->
    <com.github.xubo.statuslayout.CeilingLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:ceiling_childIndex="1">
        
        <View
            android:layout_width="match_parent"
            android:layout_height="100dp"/>
                    
        <TextView
            android:layout_width="match_parent"
            android:layout_height="100dp"
            android:text="我是吸顶View"/>
        
        <android.support.v7.widget.RecyclerView
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
            
    </com.github.xubo.statuslayout.StatusLayout>
```

## 5、gradle
add the dependency:
```gradle
dependencies {
    ...
    
    implementation 'com.github.xubo.ceilinglayout:CeilingLayout:1.0.1'
}
```

## 6、注意事项
### 1）、支持联动View表
- [x] `NestedScrollView`
- [x] `RecyclerView`
- [x] `SmartRefreshLayout`

### 2）、错误的位置索引配置
> * 不存在的子View位置索引
> * 位置索引为0，索引为0吸顶毫无意义

### 3）、SmartRefreshLayout使用注意
请使用`CeilingSmartRefreshLayout`代替`SmartRefreshLayout`，否则`SmartRefreshLayout`联动的支持将失效









