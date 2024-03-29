# 效果
![img](https://xkz-1252121784.cos.ap-chengdu.myqcloud.com/v5.gif)

模仿小红书双指触摸缩放图片的效果（解决了小红书一些小问题）

ps：支持任何View的缩放，并且缩放中心点是双指初始触摸的中心点（即指哪缩哪）

# APK 地址
https://xkz-1252121784.cos.ap-chengdu.myqcloud.com/TouchToScaleLayout.apk

# 思路
1. 双指触摸拦截事件 （需要拦截子布局和父布局的事件，以解决在滑动控件中事件冲突的问题）
2. 获取缩放的View，获取该View的左顶点坐标（left, top）和宽高
3. 打开一个沉浸式dialog，将上面获取的View，先从父布局removeView，再根据上面获取的坐标、宽高addView到dialog中
4. 缩放处理：以控件的左上角作为缩放中心点，缩放后再做偏移，即可做到指哪缩哪（具体实现看代码）
5. 松开手指后，做动画恢复到原始位置，然后从dialog中removeView，再addView到原来的布局中

ps：不一定要用Dialog，使用DecorView或者ViewGroupOverlay都可以

# 小红书异常问题
1. 缩放中心不是两指中心，而是图片中心
2. 松开双指，半透明背景没有随图片缩小而变透明，而是图片缩放到正常大小后突现变为透明
3. 双指缩放图片图片的过程中，背景下层还能触发触摸效果


