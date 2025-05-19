# 算法概述

## <span style="color:red">**1.   鼾声识别**</span>

基于**卷积神经网络**（CNN）构建鼾声识别的算法模型，使用tensorflow学习框架，语言Python。

### （1）数据集

收集两个公开数据集进行对模型的训练和性能验证。

+ [Snoring Dataset](https://www.kaggle.com/datasets/tareqkhanemu/snoring/data):作为一个训练集，该数据集包含鼾声和非鼾声样本各500个，是一个二分类数据集。

+ [ESC-50](https://github.com/karolpiczak/ESC-50)：作为一个验证集，该数据集含有5大类，50小类，是一个50分类数据集。

### （2）特征处理与CNN模型的创建

对每段音频进行短时傅里叶变换，依此作为模型的输入部分。构建两层卷积。

参考：[Deep-learning Snoring Detector - version 2.0](https://www.kaggle.com/code/orannahum/deep-learning-snoring-detector-version-2-0)(author：oran nahum)

主要做出改进：（1） 将短时傅里叶变换融入到CNN模型中，使之作为单独一层，即模型的输入为一维的音频数据，不在为二维的频谱图。（短时傅里叶变换在模型内部完成）

                            （2） 模型加入<span style="color:blue">**池化层**</span>，这将极大的减少模型参数数量，在<span style="color:blue">移动端部署中十分重要。</span>

                            （3） 加入L2正则化。

<span style="color:red">**训练代码：**</span>[modelTrain.py](modelTrain.py)

### （3） [模型压缩](https://zhuanlan.zhihu.com/p/608915925)

<span  style="color:blue">模型压缩：在不影响模型性能的基础上，尽可能的减少模型的数据量，过大的模型数据量将会导致内存泄漏等一系列问题。</span>

具体的方法：[模型剪枝](https://zhuanlan.zhihu.com/p/622519997)

<span style="color:red">**训练代码：**</span>[modelPruning.py](modelPruning.py)

### （4）转换为tflite模型

[TensorFlow Lite模型](https://www.tensorflow.org/lite/guide?hl=zh-cn#get_started):可以帮助开发者在移动设备、嵌入式设备和IoT设备上运行算法模型，以实现移动端部署。

（<span style="color:red">注：该模型其提供了诸多压缩方案，转换的本质也是在压缩，不一定非要对模型进行压缩，该模型官方提供了几个示例程序。</span>）

<span style="color:red">**转换代码：**</span>[convertTF.py](convertTF.py) 

### 性能评估

[查看详情](model_perfermanceEvaluation.md)

<span style="color:red">**转换代码：**</span>[modelTest.py](modelTrain.py)

## <span style="color:blue">**不足及可能解决方案**</span>

+ 模型的泛化能力可以进一步的提升。可以通过提高原模型的通道数量、层数，在压缩中使用**剪枝和量化**。
+ ESC50数据集是一个非平衡的数据集，鼾声仅占比2%，准确率和P-R曲线表现差。可以进一步补充鼾声数据

[audioSet](https://research.google.com/audioset/dataset/snoring.html)有专门的鼾声类，但提取较为困难，采样率模糊

[男女鼾声数据集](https://www.kaggle.com/datasets/orannahum/female-and-male-snoring/data)

## <span style="color:red">2.  睡眠分期算法</span>

**简述**：该方法不能求解出完整的睡眠分期（即快速眼动期、非快速眼动期），无法得到完整的睡眠周期图。

**参考方法**：[github项目 SleepCycleAnalysis-AndroidApp]([GitHub - lerota/SleepCycleAnalysis-AndroidApp: Android smart wake-up app for sleep pattern detection and analysis.](https://github.com/lerota/SleepCycleAnalysis-AndroidApp?tab=readme-ov-file#sleepcycleanalysis-androidapp)),该方法主要用到<span style="color:red">*差分变换*</span>和<span style="color:red">*多项式拟合*</span>

**做出的修改：**修改其多项式拟合部分，使用<span style="color:red">*RMS窗口监测*</span>

[具体过程](sleepStage/method_sleepStage.md)

### 缺陷

这种方法具有明显的缺陷性，受限于条件和时间限制，采用这种方法。

（1） 这种方法只根据体动的强度情况判定，判定条件单一

（2） 只能大致分清觉醒、浅睡和深睡情况。

（3） 个人认为手机的摆放位置、床的材质等因素会影响其判断。

### 其他方法

睡眠分期应该结合其他生理参数判断，加入多种传感器去判断（结合翻身、呼吸、光照等因素）。

[研究到的其他方法](sleepStage/otherMethod_sleepStage.md)
