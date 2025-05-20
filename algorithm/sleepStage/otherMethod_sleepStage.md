# 多生理参数来源的睡眠分期的方法

现在探究三篇**学位论文**（硕士）所用到的睡眠分期方法，通过提取更多的信息来进行睡眠分期的分类。三种方法本质上是<span style="color:red">**序列标注概率模型**</span>。

## （1）基于条件随机场（CRF）的睡眠监测模型

<span style="color:red">**Reference:**</span>  顾维玺. 基于移动感知技术的睡眠状态追踪研究[D]. 北京: 清华大学, 2015.

<center>
 <img src=".\images\other_CRF.png" width="800" style="margin: 0 10px;">
</center>

相比于隐式马尔科夫模型，该方法可以做到**全局最优预测**。

（CRF不会单独预测每个事件点的状态，而是考虑整个序列，找到整体最有可能的睡眠状态组合。）

### <span style="color:blue">特征提取方式</span>

+ **体动获取:**

对取模后的数据做差分变化，得到睡眠时体动的强度。

去噪：阈值法，改论文将阈值定为0.05.（是整体数据上减去0.05，这个去噪方法有疑问）

用1s作为分类阈值，大于1s为大动作，小于1s为小动作。

+ **声音获取：**

去噪方法也采取类似的阈值法，其根据的是**RMS**，即均方根。

采用的算法为<span style="color:red">**SVM(支持向量机)**</span>，提取相应的时域和频域特征。（注：论文没有使用公开数据集。）

+ **光强提取：**

注意手机被遮挡的情况。

阈值法，分为Low、Moderated、High

### <span style="color:red">注：</span>

该方法需要数据集。

## （2） 基于模糊逻辑的睡眠监测方法

<span style="color:red">**Reference:**</span> 包宇津. 基于智能手机睡眠质量监测模型系统设计与实现[D]. 北京: 北京工业大学,2018.

<center>
 <img src=".\images\other_fuzzy.png" width="800" style="margin: 0 10px;">
</center>

<span style="color:blue">这种方式需要一个可靠的呼吸频率监测方法。</span>

<span style="color:red">模糊逻辑的核心是找到适合的隶属函数，不需要依赖算法训练。</span>

### （3）基于高斯混合马尔科夫模型

<span style="color:red">**Reference:**</span>姚恒志. 基于智能手机的睡眠周期识别技术和应用研究[D]. 南京大学, 2017.

该方法。

+ 流式数据处理

+ 离散数据处理

需要训练集。

<span style="color:red">**输入（特征提取）：鼾声、翻身、光照**</span>
